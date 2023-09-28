package com.conpo.audiorac.application;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.conpo.audiorac.activity.MainActivity;
import com.conpo.audiorac.model.Record;

/**
 * 어플리케이션 및 개인설정 저장/로드
 * @author Hansolo
 *
 */
public class LoginInfo {
	private static final String LOG_TAG = "Preferences";
	
	protected static final String PREFS_FILE_NAME = "AppPreferences";
	protected static final String PREFS_APP_VERSION = "app_version";
	protected static final String PREFS_USER_ID = "user_id";
	protected static final String PREFS_USER_PWD = "user_pwd";
	protected static final String PREFS_SITE_URL = "site_url";
	protected static final String PREFS_SITE_NAME = "site_name";
	protected static final String PREFS_APP_TYPE = "app_type";

	protected static final String PREFS_REG_ID = "reg_id";		// GCM registration id
	
	protected SharedPreferences mPreferences;
	
	private static String  mAppVersion = "0.1";		// app version
	private static String  mUserId = "";				// 사용자 아이디
	private static String  mUserPwd = "";				// 사용자 비밀번호
	private static String  mSiteURL = "";				// 사이트 URL
	private static String  mSiteName = "";			// 사이트 이름
	private static String  mAppType = "1";				// 앱 유형(0: 구버전 호환용 player only, 1:오디오락 정규 버전)
	private static String  mGCMRegId = "";			// GCM registration id
	private static boolean  mUseAlarm = false;
	private static int mAlarmHour = 0;
	private static int mAlarmMinute = 0;

	/**
	 * 앱 실행 유형 - 구버전 호환용 player only
	 */
	public static final String APP_TYPE_PLAYER = "0";

	/**
	 * 앱 실행 유형 - 오디오락 정규 버전
	 */
	public static final String APP_TYPE_AUDIORAC = "1";

	private static MainActivity mMainActivity = null;

	public static void setMainActivity(MainActivity activity) {
		mMainActivity = activity;
	}

	public static MainActivity getMainActivity() {
		return mMainActivity;
	}

	public static void loadPreferences(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
		
		mAppVersion = prefs.getString(PREFS_APP_VERSION, "0.1");
		mUserId = prefs.getString(PREFS_USER_ID, "");
		mUserPwd = prefs.getString(PREFS_USER_PWD, "");
		mSiteURL = prefs.getString(PREFS_SITE_URL, "");
		mSiteName = prefs.getString(PREFS_SITE_NAME, "");
		mAppType = prefs.getString(PREFS_APP_TYPE, "1");
		mGCMRegId = prefs.getString(PREFS_REG_ID, "");
	}
	
	public static void savePreferences(Context ctx) {
		
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
		
		Editor editor = prefs.edit();
		editor.putString(PREFS_APP_VERSION, mAppVersion);
		editor.putString(PREFS_USER_ID, mUserId);
		editor.putString(PREFS_USER_PWD, mUserPwd);
		editor.putString(PREFS_SITE_URL, mSiteURL);
		editor.putString(PREFS_SITE_NAME, mSiteName);
		editor.putString(PREFS_APP_TYPE, mAppType);
		editor.putString(PREFS_REG_ID, mGCMRegId);
		
		editor.commit();
	}
	
	public static void setInstalled(Context ctx) {
		
		SharedPreferences prefs = ctx.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
		
		Editor editor = prefs.edit();
		editor.putBoolean("isAppInstalled", true);
		editor.commit();
	}
	
	/**
	 *  Application 버전 (Preferences.getVersion())
	 * @param ctx
	 * @return
	 */
	public static String getAppVersion(Context ctx) {
		String versionName = ""; 

		try {
			versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		}
		catch (Exception e) {
			Log.d(LOG_TAG, "Getting versionName from Manifest failed: " + e.getMessage());
		}
		
		return versionName;
	}
	
	/**
	 * Preferences에 저장된 버전 (Old 버전 번호)
	 * @return
	 */
	public static String getPrefAppVersion() {
		return mAppVersion;
	}
	
	public static void setPrefAppVersion(Context context) {
		mAppVersion = getAppVersion(context);
	}
	
	/**
	 * 사용자의 사이트 로그인 여부
	 * @return
	 */
	public static boolean isLogin() {
		if (!getUserId().equals("") && !getSiteURL().equals("")) {
			return true;

		} else {
			return false;
		}
	}

	/**
	 * 사용자 아이디
	 * @return
	 */
	public static String getUserId() {
		return mUserId;
	}
	
	public static void setUserId(String userId) {
		mUserId = userId;
	}
	
	/**
	 * 사용자 비밀번호
	 * @return
	 */
	public static String getUserPwd() {
		return mUserPwd;
	}
	
	public static void setUserPwd(String userPwd) {
		mUserPwd = userPwd;
	}
	
	/**
	 * 로그인한 사이트 URL
	 * @return
	 */
	public static String getSiteURL() {
		return mSiteURL;
	}

	public static void setSiteURL(String url) {
		mSiteURL = url;
	}

	/**
	 * 앱 유형(0: 구버전 호환용 player only, 1:오디오락 정규 버전)
	 * @return
	 */
	public static String getAppType() {
		return mAppType;
	}

	public static void setAppType(String type) {
		mAppType = type;
	}

	/**
	 * 로그인한 사이트 이름
	 * @return
	 */
	public static String getSiteName() {
		return mSiteName;
	}
	
	public static void setSiteName(String name) {
		mSiteName = name;
	}

	public static void setAlarm(int hourOfDay, int minite) {
		setAlarm(hourOfDay, minite, true);
	}

	public static void setAlarm(int hourOfDay, int minite, boolean useAlarm) {
		mUseAlarm = useAlarm;
		mAlarmHour = hourOfDay;
		mAlarmMinute = minite;
	}

	public static boolean isUseAlarm() {
		return mUseAlarm;
	}

	public static int getAlarmHour() {
		return mAlarmHour;
	}

	public static int getAlarmMinute() {
		return mAlarmMinute;
	}

	/**
	 * GCM registration id
	 * @return
	 */
	public static String getGCMRegId() {
		return mGCMRegId;
	}
	
	public static void setGCMRegId(String regId) {
		mGCMRegId = regId;
	}
	
	/**
	 * 로그아웃
	 */
	public static void logout(Context context) {
		LoginInfo.setSiteURL("");
		LoginInfo.setSiteName("");
		///LoginInfo.setUserId("");
		LoginInfo.setUserPwd("");
		
		LoginInfo.savePreferences(context);
	}
	
	/**
	 * 전달된 Record 의 회원정보가 로그인한 회원의 정보와 일치하는지 검사
	 * @param userRec
	 * @return
	 */
	public static boolean equals(Record userRec) {
		if (userRec == null) 
			return false;
		
		return ( mSiteName.equals(userRec.safeGet("lib")) &&
				 mSiteURL.equals(userRec.safeGet("url")) &&
				 mUserId.equals(userRec.safeGet("id")) &&
				 mUserPwd.equals(userRec.safeGet("pw")) );
	}
}
