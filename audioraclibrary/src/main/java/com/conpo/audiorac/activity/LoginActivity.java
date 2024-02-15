package com.conpo.audiorac.activity;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.conpo.audiorac.library.R;
import com.conpo.audiorac.adapter.SpinnerAdapter;
import com.conpo.audiorac.application.AudioRacApplication;
import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.application.LoginInfo;
import com.conpo.audiorac.model.APIData;
import com.conpo.audiorac.model.APIResponse;
import com.conpo.audiorac.model.LibraryList;
import com.conpo.audiorac.model.Record;
import com.conpo.audiorac.player.CPDRMCloseEvent;
import com.conpo.audiorac.server.RetrofitClient;
import com.conpo.audiorac.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 로그인 화면
 */
public class LoginActivity extends ActivityBase 
					implements View.OnClickListener, AdapterView.OnItemSelectedListener {

	private final String LOG_TAG = "Login";

	private String mSiteURL = "";
	private String mSiteName = "";
	private String mAppType = "";
	private String mUseName = "N";
	
	// UI references.
	private EditText mEtKeyword;
	private EditText mEtUserId;
	private EditText mEtPassword;
	private Spinner mSpOrganType;
	private Spinner mSpOrganSchool;
	private Spinner mSpOrganLibrary;
	
	// Login parameters from custom url 
	private Record mUserRec = new Record();
	
	private SpinnerAdapter mOrganAdapter;
	private SpinnerAdapter mSchoolAdapter;
	private SpinnerAdapter mLibraryAdapter;

	@SuppressWarnings("unused")
	private boolean mIsAppInstalled = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);

		Intent intent = this.getIntent();
		
		mUserRec.put("lib", intent.getStringExtra("com.conpo.audiorac.login.lib"));
		mUserRec.put("url", intent.getStringExtra("com.conpo.audiorac.login.url"));
		mUserRec.put("app_type", intent.getStringExtra("com.conpo.audiorac.login.app_type"));
		mUserRec.put("id", intent.getStringExtra("com.conpo.audiorac.login.id"));
		mUserRec.put("pw", intent.getStringExtra("com.conpo.audiorac.login.pw"));

		
		if (mUserRec != null && mUserRec.get("id") != null) {
			/*
			 * custom url로부터 전달된 로그인 정보로 로그인
			 */
			saveLoginInfo(mUserRec);
			
			launchMainActivity();
			
			finish();
			return;

		} else if (LoginInfo.isLogin()) {
			/*
			 * 로그인한 사용자는 메인으로 바로 이동
			 */
			launchMainActivity();
			
			finish();
			return;
		}
		
		
        // 기관목록
		mSpOrganType = (Spinner) this.findViewById(R.id.sp_organ_type);
		mSpOrganSchool = (Spinner) this.findViewById(R.id.sp_organ_school);
		mSpOrganLibrary = (Spinner) this.findViewById(R.id.sp_organ_library);
		
		ArrayList<Record> arrOrganType = new ArrayList<>();
		String[] types = this.getResources().getStringArray(R.array.OrganizationType);
		for (String type: types) {
			Record rec = new Record();
			rec.put("name", type);
			arrOrganType.add(rec);
		}
		SpinnerAdapter adapter = new SpinnerAdapter(this, arrOrganType);
		mSpOrganType.setAdapter(adapter);
		
		mSpOrganType.setOnItemSelectedListener(this);
		mSpOrganSchool.setOnItemSelectedListener(this);
		mSpOrganLibrary.setOnItemSelectedListener(this);
		
		/*
		 * 사이트 목록 로드
		 */
		loadAudioRacSiteList();

		mEtKeyword = (EditText) findViewById(R.id.keyword);
		mEtUserId = (EditText) findViewById(R.id.userid);
		mEtPassword = (EditText) findViewById(R.id.password);
		
		mEtUserId.setText(LoginInfo.getUserId());
		
		mEtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
						if (id == R.id.password || id == EditorInfo.IME_NULL) {
							return true;
						}
						return false;
					}
				});

		// 검색 버튼
		findViewById(R.id.btn_search).setOnClickListener(this);
		
		// 로그인 버튼
		findViewById(R.id.btn_login).setOnClickListener(this);
	}

	/**
	 * 오디오락 사이트 리스트
	 */
	private void loadAudioRacSiteList() {
		RetrofitClient.getAudioRacSiteList("", Utils.getAppVersion(mContext),
				new Callback<APIResponse>() {
					@Override
					public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
						if (response.isSuccessful()) {
							APIResponse api = response.body();

							APIData data = api.getData();
							if (data == null) {
								reportNoData();
								return;
							}

							Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
							Type typeLibraryList = new TypeToken<LibraryList>(){}.getType();

							// 대학교 목록
							LibraryList listSchool = (LibraryList)gson.fromJson(api.getListJson("school"), typeLibraryList);
							ArrayList<Record> schoolList = listSchool.toRecordList();
							mSchoolAdapter = new SpinnerAdapter(mContext, schoolList);
							mSpOrganSchool.setAdapter(mSchoolAdapter);

							Record rec = mSchoolAdapter.getItem(0);

							if (rec.safeGet("use_name").equals("Y")) {
								mEtPassword.setHint(R.string.prompt_password);			// 성명
							} else {
								mEtPassword.setHint(R.string.prompt_password2);	// 비밀번호
							}

							// 도서관 목록
							LibraryList listLibrary = (LibraryList)gson.fromJson(api.getListJson("library"), typeLibraryList);
							ArrayList<Record> libList = listLibrary.toRecordList();
							mLibraryAdapter = new SpinnerAdapter(mContext, libList);
							mSpOrganLibrary.setAdapter(mLibraryAdapter);
						}
					}

					@Override
					public void onFailure(Call<APIResponse> call, Throwable t) {
						Log.d(LOG_TAG,"onFailure : " + t.getMessage());
					}
				});
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if (id == R.id.btn_login) {
			attemptLogin();
		}
		else if (id == R.id.btn_search) {
			searchOrgan();
		}
	}

	private void searchOrgan() {
		String keyword = mEtKeyword.getText().toString().replace(" ", "");
		
		if ( !(keyword != null && keyword.length() > 0)) {
			return;
		}
	
		int position = mSpOrganType.getSelectedItemPosition();
		
		if (position == 0) {
			for (int i = 0; i < mSchoolAdapter.getCount(); i++) {
				Record rec = (Record) mSchoolAdapter.getItem(i);
				if (rec.safeGet("name").contains(keyword)) {
					mSpOrganSchool.setSelection(i, true);
					return;
				}
			}

		} else if (position == 1) {
			for (int i = 0; i < mLibraryAdapter.getCount(); i++) {
				Record rec = (Record) mLibraryAdapter.getItem(i);
				if (rec.safeGet("name").contains(keyword)) {
					mSpOrganLibrary.setSelection(i, true);
					return;
				}
			}
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long rowId) {
		int id = parent.getId();
		Record rec = null;
		
		if (id == R.id.sp_organ_type) {	// 기관종류 선택
			if (position == 0) {	// 대학교
				mSpOrganLibrary.setVisibility(View.GONE);
				mSpOrganSchool.setVisibility(View.VISIBLE);
				mEtKeyword.setHint("대학교명 검색");
				
				mOrganAdapter = (SpinnerAdapter)mSpOrganSchool.getAdapter();

			} else if (position == 1) {	// 도서관
				mSpOrganSchool.setVisibility(View.GONE);
				mSpOrganLibrary.setVisibility(View.VISIBLE);
				mEtKeyword.setHint("도서관명 검색");
				
				mOrganAdapter = (SpinnerAdapter)mSpOrganLibrary.getAdapter();
			}

			if (mOrganAdapter != null) {
				int index = mSpOrganLibrary.getSelectedItemPosition(); 
				if (index >= 0) {
					rec = mOrganAdapter.getItem(index);
				}
			}

		} else if (id == R.id.sp_organ_school || id == R.id.sp_organ_library) {
			if (mOrganAdapter == null)
				return;
			
			rec = mOrganAdapter.getItem(position);
		}
		
		if (rec != null) {
			// 아이디에 표출되는 문구(id_type) : [I:아이디, E:사번(학번), 기본값:I]
			// 패스워드에 표출되는 문구(use_name) : [N:비밀번호, Y:성명, I:아이디, S:학번(사번), 기본값:N]
			mUseName = rec.safeGet("use_name");

			if (mUseName.equals("Y")) {
				mEtPassword.setHint(R.string.prompt_password);	// 성명
				mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT);
				mEtPassword.setSelection(mEtPassword.getText().length());

			} else if (mUseName.equals("I")) {
				mEtPassword.setHint(R.string.prompt_password1);	// 아이디
				mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT);
				mEtPassword.setSelection(mEtPassword.getText().length());

			} else if (mUseName.equals("S")) {
				mEtPassword.setHint(R.string.prompt_password2);	// 학번(사번)
				mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT);
				mEtPassword.setSelection(mEtPassword.getText().length());

			} else { // "N"
				mEtPassword.setHint(R.string.prompt_password3);	// 비밀번호
				mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				mEtPassword.setSelection(mEtPassword.getText().length());
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	
	@Override
	public void onBackPressed() {

		if (CPDRMCloseEvent.Handler(this)) {
			finishAndRemoveTask();
		}
	}

	/**
	 * 로그인 입력 정보 체크
	 */
	public void attemptLogin() {
		
		if (!Utils.checkNetworkState(this)) {
			showToast(R.string.error_bad_network_state);
			return;
		}

		// Reset errors.
		mEtUserId.setError(null);
		mEtPassword.setError(null);

		String email = mEtUserId.getText().toString();
		String password = mEtPassword.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// 이름/ 이메일 체크
		if (TextUtils.isEmpty(email)) {
			mEtUserId.setError(getString(R.string.error_invalid_email));
			focusView = mEtUserId;
			cancel = true;
		}

		if (!cancel) {
			// 비밀번호 체크
			if (TextUtils.isEmpty(password)) {
				mEtPassword.setError(getString(R.string.error_invalid_password));
				focusView = mEtPassword;
				cancel = true;
			}
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			int position = mSpOrganType.getSelectedItemPosition();
			int nCnt = 0;
			Record rec = new Record();

			if (position == 0) {
				nCnt = mSpOrganSchool.getCount();
				rec = (Record) mSpOrganSchool.getSelectedItem();

			} else if (position == 1) {
				nCnt = mSpOrganLibrary.getCount();
				rec = (Record) mSpOrganLibrary.getSelectedItem();
			}

			if (nCnt <= 0 || rec == null) {
				showToast(R.string.msg_login_network_err);
				return;
			}

			mSiteURL = rec.safeGet("url");
			mSiteName = rec.safeGet("name");
			mAppType = rec.safeGet("app_type");
			mUseName = rec.safeGet("use_name");

			Confirm(R.string.msg_login_confirm_organ,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

							String email = mEtUserId.getText().toString();
							String password = mEtPassword.getText().toString();

							/*
							 * 로그인 처리
							 */
							findViewById(R.id.progress).setVisibility(View.VISIBLE);
							findViewById(R.id.btn_login).setClickable(false);
							//new UserLoginTask().execute(mSiteURL, email, password);
							requestUserLogin(email, password);
						}
					});
		}
	}

	/**
	 * 회원 모바일 로그인 요청
	 * @param email 아이디, 이메일..
	 * @param password 비밀번호, 성명, 학번..
	 */
	private void requestUserLogin(final String email, final String password) {
		RetrofitClient.requestUserLogin(mSiteURL, mUseName, email, password, Utils.getAppVersion(mContext),
				new Callback<APIResponse>() {
					@Override
					public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
						if (response.isSuccessful()) {
							APIResponse api = response.body();

							if (api.success()) {
								/*
								 * 로그인 성공 - 정보 저장
								 */
								saveLoginInfo(mSiteName, mSiteURL, mAppType, email, password);

								/*
								 * 메인 창 띄우기
								 */
								launchMainActivity();
								finish();

							} else {
								/*
								 * 로그인 실패
								 */
								mEtPassword.requestFocus();
								findViewById(R.id.progress).setVisibility(View.GONE);
								findViewById(R.id.btn_login).setClickable(true);

								Alert(api.getMessage());
							}

						} else {
							/*
							 * 로그인 실패
							 */
							mEtPassword.requestFocus();
							findViewById(R.id.progress).setVisibility(View.GONE);
							findViewById(R.id.btn_login).setClickable(true);

							Alert(response.message());
						}
					}

					@Override
					public void onFailure(Call<APIResponse> call, Throwable t) {
						Log.d(LOG_TAG,"onFailure : " + t.getMessage());

						mEtPassword.requestFocus();
						findViewById(R.id.progress).setVisibility(View.GONE);
						findViewById(R.id.btn_login).setClickable(true);
					}
				});
	}

	/**
	 * 사용자 로그인 정보 저장
	 * @param siteURL
	 * @param siteName
	 * @param userId
	 */
	private void saveLoginInfo(String siteName, String siteURL, String appType, String userId, String userPwd) {
		LoginInfo.setSiteName(siteName);
		LoginInfo.setSiteURL(siteURL);
		LoginInfo.setAppType(appType);
		LoginInfo.setUserId(userId);
		LoginInfo.setUserPwd(userPwd);
		
		LoginInfo.savePreferences(this);
	}
	
	private void saveLoginInfo(Record rec) {
		LoginInfo.setSiteName(rec.safeGet("lib"));
		LoginInfo.setSiteURL(rec.safeGet("url"));
		LoginInfo.setAppType(rec.safeGet("app_type"));
		LoginInfo.setUserId(rec.safeGet("id"));
		LoginInfo.setUserPwd(rec.safeGet("pw"));
		
		LoginInfo.savePreferences(this);
	}
	
	/**
	 * 메인 창으로 이동
	 */
	private void launchMainActivity() {
		Intent intent = null;

		if (LoginInfo.getAppType().equals(LoginInfo.APP_TYPE_AUDIORAC)) {
			intent = new Intent(LoginActivity.this, MainActivity.class);

			String csCode = getIntent().getStringExtra("com.conpo.audiorac.login.cs_code");
			String chCode = getIntent().getStringExtra("com.conpo.audiorac.login.ch_code");

			intent.putExtra("com.conpo.audiorac.login.cs_code", csCode);
			intent.putExtra("com.conpo.audiorac.login.ch_code", chCode);

		}
		// 오디오락 플레이어는 더이상 지원하지 않음
//		else if (LoginInfo.getAppType().equals(LoginInfo.APP_TYPE_PLAYER)) {
//			intent = new Intent(LoginActivity.this, CPDRMMainActivity.class);
//		}

		String fileUrl = getIntent().getStringExtra(Common.MSG_LOGININFO_DNFILE);
		intent.putExtra(Common.MSG_LOGININFO_DNFILE, fileUrl);

		startActivity(intent);
	}
	
	/**
	 * 회원 로그인 요청 태스크
	 * @author hansolo
	 *
	 */
	private class UserLoginTask extends AsyncTask<String, Void, String> {
		private Context mContext = LoginActivity.this;
		
		private String mUserId;
		private String mUserPwd;
		
		protected String doInBackground(String... params) {
			String siteUrl = params[0];
			mUserId = params[1];
			mUserPwd = params[2];
			
			return AudioRacApplication.requestUserLogin(mContext, siteUrl, mUserId, mUserPwd);
		}
		
		protected void onPostExecute(String result) {
			if (result == null || !result.contains("\"code\":\"0\"")) {
				/*
				 * 로그인 실패
				 */
				showToast(R.string.failed_login);
				mEtPassword.requestFocus();
				findViewById(R.id.progress).setVisibility(View.GONE);

			} else {
				/*
				 * 로그인 성공 - 정보 저장
				 */
				saveLoginInfo(mSiteName, mSiteURL, mAppType, mUserId, mUserPwd);
				
				/*
				 * 메인 창 띄우기
				 */
				launchMainActivity();
				
				finish();
			}
			
			hideProgress();
		}
	}
	
}
