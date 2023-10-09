package com.conpo.audiorac.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.bumptech.glide.Glide;
import com.conpo.audiorac.library.R;
import com.conpo.audiorac.application.AudioRacApplication;
import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.application.PlayList;
import com.conpo.audiorac.application.LoginInfo;
import com.conpo.audiorac.model.APIData;
import com.conpo.audiorac.model.APIResponse;
import com.conpo.audiorac.model.DrmFile;
import com.conpo.audiorac.player.CPDRMPlayer;
import com.conpo.audiorac.player.MediaFile;
import com.conpo.audiorac.player.PlayerCloseEvent;
import com.conpo.audiorac.server.RetrofitClient;
import com.conpo.audiorac.util.CPDRMUtil;
import com.conpo.audiorac.util.HttpUtil;
import com.conpo.audiorac.util.StringUtil;
import com.conpo.audiorac.util.Utils;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 오디오락 플레이어
 */
public class PlayerActivity extends ActivityBase
						implements MediaPlayer.OnCompletionListener,
									OnClickListener, OnTouchListener {

	private static final String LOG_TAG = "AudioRacPlayer";
	
	/* Global Variant */
	protected CPDRMPlayer mCPDrmPlayer = null;	// 미디어 플레이어
	protected MediaFile mMediaFile =  null;

	private SeekBar mSbMediaProgress;
	private Handler mHandlerDuration;
	private Handler mHandlerProgress;

	private ImageView mIvArtwork;
	private TextView mTvMediaTitle;
	private TextView mTvMediaCDuration;
	private TextView mTvMediaTDuration;

	protected ImageButton mBtnFilePlay;
	protected ImageButton mBtnFileStop;
	protected ImageButton mBtnFilePause;

	protected boolean mUsePlayList = false;

	@SuppressLint({ "NewApi", "HandlerLeak" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_player);

		/*
		 * DRM Player
		 */
		mCPDrmPlayer = new CPDRMPlayer(this);
		mCPDrmPlayer.getMediaPlayer().setOnCompletionListener(this);

		/*
		 * Player UI
		 */
		mIvArtwork = findViewById(R.id.iv_media_artwork);
		mTvMediaTitle = findViewById(R.id.txt_media_title);

		mSbMediaProgress = findViewById(R.id.sb_media_progress);	// Seek bar
		mTvMediaCDuration = findViewById(R.id.txt_media_cduration);
		mTvMediaTDuration = findViewById(R.id.txt_media_tduration);

		mTvMediaTitle.setText("");
		mSbMediaProgress.setMax(100);
		mSbMediaProgress.setOnTouchListener(this);
		mTvMediaCDuration.setText("00:00");
		mTvMediaTDuration.setText("00:00");

		initializeControlButtons();
		// Control buttons

		// Duration Handler
		mHandlerDuration = new Handler() {
			public void handleMessage(Message msg) {
				if (mCPDrmPlayer != null && mCPDrmPlayer.getPlayState() == 1) {
					// set current duration string
					int duration = mCPDrmPlayer.getCurrentPosition();
					mTvMediaCDuration.setText(CPDRMUtil.DurationToTimeString(duration));
					mTvMediaCDuration.invalidate();
					mHandlerDuration.sendEmptyMessageDelayed(0, 1000);
				}
			}
		};

		// Progress Handler
		mHandlerProgress = new Handler() {
			public void handleMessage(Message msg) {
				if (mCPDrmPlayer != null && mCPDrmPlayer.getPlayState() == 1) {
					// set current progress
					mSbMediaProgress.setProgress((int) (((float) mCPDrmPlayer.getCurrentPosition() / mCPDrmPlayer.getDuration()) * 100));
					mHandlerProgress.sendEmptyMessageDelayed(0, 1000);
				}
			}
		};

		/*
		 * 미디어 준비 및 플레이
		 */
		prepareMediaAndPlay();

		onCreated();
	}

	protected void onCreated() {

	}

	private void initializeControlButtons() {
		// Control buttons
		mBtnFilePlay = findViewById(R.id.btn_file_play);
		mBtnFileStop = findViewById(R.id.btn_file_stop);
		mBtnFilePause = findViewById(R.id.btn_file_pause);

		mBtnFilePlay.setOnClickListener(this);
		mBtnFileStop.setOnClickListener(this);
		mBtnFilePause.setOnClickListener(this);

		onChangeMediaBtnState(true, false, false, false);
	}

	/**
	 * 준비된 미디어가 있는지 확인하여 다음파일을 플레이 시킨다
	 */
	public void prepareMediaAndPlay() {

		showProgress(R.string.msg_player_reading_file);
		Log.i(LOG_TAG, "@@prepareMediaAndPlay");

		DrmFile drmFile = PlayList.getCurrentFile();
		if (drmFile == null) {
			//showToast("선택한 파일이 없습니다.");
			hideProgress();
			return;
		}

		String path = drmFile.path;
		String filename = drmFile.name;

		mMediaFile = new MediaFile(mContext);
		mMediaFile.loadFile(path, filename);

		if (mMediaFile.getFileLength() <= 0) {
			showToast(R.string.msg_player_invalid_file_format);
			hideProgress();
			return;
		}

		mMediaFile.fileToBytes(this);

		/*
		 * Conpo DRM 파일 체크
		 */

		if (mMediaFile.isCPDRM()) {
			String drmPrefix = mMediaFile.getDRMPrefix();

			/*
			 * DRM 형식 체크
			 */
			String[] arrEncPrefix = drmPrefix.split("[|]");
			if (arrEncPrefix.length != 2) {
				showToast(getString(R.string.msg_player_invalid_file_format, drmPrefix));
				hideProgress();
				return;
			}

			/*
			 * 현재 플레이 시점이 제한시간(초)을 지났는지 확인
			 */
			if (mMediaFile.getRemainTime() == 0) {
				showToast(R.string.msg_player_expired_license);
				onMediaStop();
				hideProgress();
				return;
			}

			/*
			 * 네트워크 체크
			 */
			if (!Utils.checkNetworkState(PlayerActivity.this)) {
				showToast(R.string.msg_player_check_network);
				// 인터넷 안되도 실행 되도록 주석처리 : 2014-10-16
				/*
				onMediaStop();
				hideProgress();
				return;
				*/
			}

			long reverseLen = mMediaFile.reverseBytes(this);

			if (reverseLen <= 0) {
				showToast(R.string.msg_player_invalid_file_format);
				onMediaStop();
				hideProgress();
				return;
			}

			/*
			 * DRM 파일 최초실행인지 판단
			 */
			if (mMediaFile.isFirstDrmPlay()) {
				// 서버에 제한시간 요청
				new RequestGetLimitTask().execute(mMediaFile.getDRMPrefixAt(1), mMediaFile.getDRMPrefixAt(2));

			} else {
				// 최초 실행이 아닌 경우 최종 재생시간만 기록
				mMediaFile.setLastPlayTime();
			}

			/*
			 * 플레이 통계를 보내기
			 */
			saveStreamLog();

		}

		/*
		 * Player 시작 및 화면에 앨범정보 표시
		 */
		displayAlbumInfo();

		if (!mMediaFile.isCPDRM()) {
			setPlaybackFile();
		}

		hideProgress();
	}

	private void saveStreamLog() {
		String siteURL = LoginInfo.getSiteURL();
		String userId = LoginInfo.getUserId();
		String volumeId = mMediaFile.getDRMPrefixAt(1);
		String installmentId = mMediaFile.getDRMPrefixAt(2);

		try {
			userId = CPDRMUtil.makeUTF8( userId );
			volumeId = CPDRMUtil.makeUTF8( volumeId );
			installmentId = CPDRMUtil.makeUTF8( installmentId );

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		RetrofitClient.saveStreamLog(siteURL, userId, volumeId,
				new Callback<APIResponse>() {
					@Override
					public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
						if (response.isSuccessful()) {
							Log.d(LOG_TAG, "Save stream log OK");
						}
					}

					@Override
					public void onFailure(Call<APIResponse> call, Throwable t) {
						Log.d(LOG_TAG, "onFailure : " + t.getMessage());
					}
				});
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		if (mCPDrmPlayer != null && mCPDrmPlayer.getPlayState() == 1) {
			initPlayerControls();
		}

		if (PlayList.isNextAvailable()) {
			PlayList.moveNext();

			prepareMediaAndPlay();
		}
	}

	/* OnDestroy */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mCPDrmPlayer != null) {
			mCPDrmPlayer.release();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mCPDrmPlayer.isPlaying()) {
			if (PlayerCloseEvent.Handler(this)) {
				setResult(RESULT_OK);
				finish();
			}

		} else {
			setResult(RESULT_OK);
			finish();
		}
	}
	
	@Override
	public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btn_file_play) {
            onMediaStart();

        } else if (id == R.id.btn_file_stop) {
            onMediaStop();

        } else if (id == R.id.btn_file_pause) {
            onMediaPause();

        }
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (view.getId() == R.id.sb_media_progress) {
			if (mCPDrmPlayer != null && mCPDrmPlayer.isPlaying() && mCPDrmPlayer.getPlayState() == 1) {
				SeekBar sb = (SeekBar) view;
				int playPositionInMillisecconds = (mCPDrmPlayer.getDuration() / 100) * sb.getProgress();
				mCPDrmPlayer.seekTo(playPositionInMillisecconds);
			}
		}
		return false;
	}

	/**
	 * 플레이할 앨범 정보 표시
	 */
	private void displayAlbumInfo() {
		
		// 파일경로로 쿼리해오는 정보
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] cursor_cols = { 
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.DURATION 
		};
		
		String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND " +
				       MediaStore.Audio.Media.DATA + " LIKE '%" + mMediaFile.getFilename().replaceAll("'", "''") + "%' "; // 파일경로
		Log.i(Common.TAG, "##########" + where);
		
		Cursor cursor = getContentResolver().query(uri, cursor_cols, where, null, null);

		long songId = -1;
		long albumId = -1;
		String title = "";
		
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				songId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
				albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				title  = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			}
		}
		
		cursor.close();
		
		if (title == null || title.equals("")) {
			// get mp3 metadata
			try {
			  	MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
			  	metaRetriever.setDataSource(mMediaFile.getPath() + mMediaFile.getFilename());
			 
			 	title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

			} catch(RuntimeException e) {
				Log.i(Common.TAG, "META_E : " + e.getMessage());
			}
		}
		
		if (StringUtil.isEmptyOrNull(title)) {
			// get mp3 filename
			title = mMediaFile.getFilename();
		}
		
		// 앨범 타이틀
		mTvMediaTitle.setText(title);
		
		// 앨범자켓
		if (mMediaFile.isCPDRM()) {
			String siteURL = LoginInfo.getSiteURL();
			String volumeId = mMediaFile.getDRMPrefixAt(1);

			try {
				volumeId = CPDRMUtil.makeUTF8( volumeId );

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			RetrofitClient.getAlbumArtUrl(siteURL, volumeId,
					new Callback<APIResponse>() {
						@Override
						public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
							if (response.isSuccessful()) {
								APIResponse api = response.body();

								APIData data = api.getData();
								String imageUrl = "";
								if (!imageUrl.contains("http")) {
									imageUrl = HttpUtil.verifyUrl(LoginInfo.getSiteURL() + imageUrl);
								}

								Glide.with(mContext).load(imageUrl).into(mIvArtwork);
								setPlaybackFile();

							}
						}

						@Override
						public void onFailure(Call<APIResponse> call, Throwable t) {
							Log.d(LOG_TAG,"onFailure : " + t.getMessage());
						}
					});


		} else {
			Bitmap bm = null;
			bm = CPDRMUtil.getArtwork(this, songId, albumId, true);

			if (bm == null) {
				//g_ivArtwork.setImageResource(R.drawable.albumart);
				mIvArtwork.setImageBitmap(bm);

			} else {
				mIvArtwork.setImageBitmap(bm);
			}
		}
	}
	
	/**
	 * 임시파일을 이용하여 플레이
	 */
	private void setPlaybackFile() {
		try {
			String tempName = getExternalCacheDir() + "/" + Common.DRM_NAME +  Common.DRM_EXTENSION;
			File tempMp3 = new File(tempName);
			
			FileInputStream fis = new FileInputStream(tempMp3);
			mCPDrmPlayer.reset();
			mCPDrmPlayer.setDataSource(fis.getFD());
			mCPDrmPlayer.prepare();
			
			onMediaStart();

			tempMp3.delete();

		} catch (IOException ex) {
			Log.i(Common.TAG, "EXCEPTION: " + ex.getMessage());
		}
	}
	
	private void onMediaStart() {
		int duration = mCPDrmPlayer.start();
		mTvMediaTDuration.setText(CPDRMUtil.DurationToTimeString(duration));
		onChangeMediaBtnState(false, false, true, true);
		
		mCPDrmPlayer.setPlayState(1);
		mHandlerDuration.sendEmptyMessage(0);
		mHandlerProgress.sendEmptyMessage(1);
	}
	
	private void onMediaStop() {
		mCPDrmPlayer.stop();

		initPlayerControls();
	}

	private void initPlayerControls() {
		// progress 초기화
		mSbMediaProgress.setProgress(0);
		mTvMediaCDuration.setText("00:00");
		mCPDrmPlayer.setPlayState(0);
		
		onChangeMediaBtnState(true, true, false, false);
	}
	
	private void onMediaPause() {
		mCPDrmPlayer.pause();
		onChangeMediaBtnState(false, true, true, false);
		mCPDrmPlayer.setPlayState(2);
	}

	protected void onChangeMediaBtnState(boolean open, boolean play, boolean stop, boolean pause) {
		mBtnFilePlay.setEnabled(play);
		mBtnFileStop.setEnabled(stop);
		mBtnFilePause.setEnabled(pause);
	}

	/**
	 * 앨범 제한시간 요청
	 * @author hansolo
	 *
	 */
	public class RequestGetLimitTask extends AsyncTask<String, Void, String> {
		
		protected String doInBackground(String...params) {
			return AudioRacApplication.requestLimit(params[0], params[1]);
		}
		
		protected void onPostExecute(String result) {
			mMediaFile.doFirstDrmPlay(LoginInfo.getSiteURL(), LoginInfo.getUserId(), result);
		}
	}


}
