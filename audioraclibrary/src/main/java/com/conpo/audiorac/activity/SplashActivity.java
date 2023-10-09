package com.conpo.audiorac.activity;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;

import com.conpo.audiorac.library.R;
import com.conpo.audiorac.application.AudioRacApplication;
import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.application.LoginInfo;
import com.conpo.audiorac.model.APIData;
import com.conpo.audiorac.model.APIResponse;
import com.conpo.audiorac.model.LibraryList;
import com.conpo.audiorac.server.RetrofitClient;
import com.conpo.audiorac.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends ActivityBase {
	private static final String LOG_TAG = "Splash";

	private boolean mVersionCheckCompleted = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);

		/*
		 * 이전 로그인 정보 로드
		 */
		LoginInfo.loadPreferences(this);

		checkAppVersion();
		//new RequestVersionInfoTask().execute();

	}

	private void checkAppVersion() {
		RetrofitClient.getAppVersion(new Callback<APIResponse>() {
					@Override
					public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
						if (response.isSuccessful()) {
							APIResponse api = response.body();
							APIData data = api.getData();

							if (data == null) {
								return;
							}

							boolean isNewVersionExist = false;

							String currentVersion = Utils.getAppVersion(mContext);
							String serverVersion = (String)data.get("version");

							if (currentVersion.compareTo(serverVersion) < 0) {    // 서버에 새로운 버전 정보가 있으면

								Confirm(getString(R.string.msg_new_version),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
												/*
												 * 어플리케이션 업데이트
												 */
												Utils.installPackage(mContext, mContext.getPackageName(), null);

												SplashActivity.this.finish();
											}
										},
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int id) {
												dialog.dismiss();

												// 필수업데이트 : 업데이트 안하면 프로그램 종료
												SplashActivity.this.finish();
											}
										});

								// 새 버전이 있으므로 로그인 하지 말것
								isNewVersionExist = true;
							}

							// 새 버전이 있으면 자동로그인 하지 말것
							if (!isNewVersionExist) {
								mVersionCheckCompleted = true;
								goLogin();
							}

						}
					}

					@Override
					public void onFailure(Call<APIResponse> call, Throwable t) {
						Log.d(LOG_TAG,"onFailure : " + t.getMessage());
						goLogin();
					}
				});
	}

	private void goLogin() {
		handler.sendEmptyMessageDelayed(0, 10);
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Uri uri = getIntent().getData();
			Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

			if (uri != null && uri.getScheme().indexOf("contentsportal") >= 0) {
				/*
				 * Scheme URL 파라미터
				 * - audiorac 모바일 웹사이트에서 파일 다운로드를 선택하여 앱이 실행된 경우: 구버전 호환)
				 */
				String fileUrl;

				try {
					fileUrl = uri.getQueryParameter("dn");

					try {
						fileUrl = URLDecoder.decode(fileUrl, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						fileUrl = "";
					}

				} catch(NullPointerException e) {
					Log.i(Common.TAG, "getQueryParam:" + e.getMessage());
					fileUrl = "";
				}

				intent.putExtra(Common.MSG_LOGININFO_DNFILE, fileUrl);

			} else if (uri != null && uri.getScheme().indexOf("audiorac") >= 0) {
				/*
				 * Scheme URL 파라미터
				 * - 외부 사잍,에서 audiorac 모바일 앱이 실행된 경우)
				 */
				String lib = uri.getQueryParameter("lib_name").replace(" ", "+");
				String url = uri.getQueryParameter("url").replace(" ", "+");

				String appType = uri.getQueryParameter("app_type");

				if (appType != null) {
					appType.replace(" ", "+");
				} else {
					appType = LoginInfo.APP_TYPE_AUDIORAC;
				}

				String id = uri.getQueryParameter("user_id").replace(" ", "+");
				String pw = uri.getQueryParameter("user_name").replace(" ", "+");
				String cs_code = uri.getQueryParameter("cs_code");
				String ch_code = uri.getQueryParameter("ch_code");
				String down = uri.getQueryParameter("dn");

				intent.putExtra("com.conpo.audiorac.login.lib", lib);
				intent.putExtra("com.conpo.audiorac.login.url", url);
				intent.putExtra("com.conpo.audiorac.login.app_type", appType);
				intent.putExtra("com.conpo.audiorac.login.id", id);
				intent.putExtra("com.conpo.audiorac.login.pw", pw);

				if (cs_code != null && ch_code != null) {
					cs_code = cs_code.replace(" ", "+");
					ch_code = ch_code.replace(" ", "+");

					intent.putExtra("com.conpo.audiorac.login.cs_code", cs_code);
					intent.putExtra("com.conpo.audiorac.login.ch_code", ch_code);
				}

				if (down != null && down.equals("1")) {
					intent.putExtra(Common.MSG_LOGININFO_DNFILE, "down");
				}
			}

			startActivity(intent);
			finish();
		}
	};

	/**
	 * BACK 버튼을 누르면 바로 종료
	 */
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
           return false;
        }
        
        return super.onKeyDown(keyCode, event);
    }

}
