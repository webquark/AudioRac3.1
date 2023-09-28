package com.conpo.audiorac.util;

import com.conpo.audiorac.library.R;
import com.conpo.audiorac.application.LoginInfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * 일반 유틸리티 모음
 * @author hansolo
 *
 */
public class Utils {

	public static void addShortcut(Context context) {

		Intent shortcutIntent = new Intent();
	    shortcutIntent.setAction(Intent.ACTION_MAIN);
	    shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	    shortcutIntent.setClassName(context, context.getClass().getName());
	    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	       
	    Parcelable iconResource = Intent.ShortcutIconResource.fromContext( context,  R.drawable.ic_launcher);
	       
	    Intent intent = new Intent();
	    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(R.string.app_name));
	    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
	    intent.putExtra("duplicate", false);
	    intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");       
	    context.sendBroadcast(intent);
	    
	    LoginInfo.setInstalled(context);
    }
	
	public static boolean checkNetworkState(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    NetworkInfo wifi   = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    NetworkInfo lte_4g = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
	    
	    boolean isLte_4g = false;
	    if(lte_4g != null && lte_4g.isConnected()) {
	    	return true;
	    }
	    
	    if (mobile != null && mobile.isConnected()) {
	    	return true;
	    }

		if (wifi != null && wifi.isConnected()) {
			return true;
		}

	    return false;
	}

	/**
	 *  Application 버전 (AndroidManifest의 값)
	 * @param ctx
	 * @return
	 */
	public static String getAppVersion(Context ctx) {
		String versionName = ""; 
				
		try {
			versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		}
		catch (Exception e) {
			Log.d("Utils", "Getting versionName from Manifest failed: " + e.getMessage());
		}
		
		return versionName;
	}
	
	/**
	 * HTTP client 및 WebView에 적용할 User Agent String을 리턴합니다 (예: Contents Portal AudioRac Player 1.1)
	 * @param ctx
	 * @return
	 */
	public static String getUserAgentString(Context ctx) {
		String userAgentString = ctx.getResources().getString(R.string.user_agent_string);
		String appVersion = getAppVersion(ctx);
		
		return userAgentString + " " + appVersion;
	}
	
	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}

	/**
	 * 패키지 설치
	 * @param context
	 * @param packageName
	 * @param downloadUrl
	 */
	public static void installPackage(Context context, String packageName, String downloadUrl) {
		if (downloadUrl != null && !downloadUrl.equals("")) {
			/*
			 * 지정한 업데이트 URL이 있는 경우
			 */
			Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
			context.startActivity(launchBrowser);
		} else if (packageName != null && !packageName.equals("")) {
			/*
			 * 기본 마켙으로 이동
			 */
			Intent intent = new Intent(Intent.ACTION_VIEW);
			String url = "market://details?id=" + packageName;
			intent.setData(Uri.parse(url));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} else {
			Log.d("UTIL", "Invalid package install path");
		}
	}
}
