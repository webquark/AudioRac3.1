package com.conpo.audiorac.application;

import java.util.ArrayList;

import org.json.JSONObject;

import com.conpo.audiorac.model.Record;
import com.conpo.audiorac.util.CPDRMUtil;
import com.conpo.audiorac.util.HttpUtil;
import com.conpo.audiorac.util.JSONHelper;
import com.conpo.audiorac.util.JSONUtil;
import com.conpo.audiorac.util.Utils;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

public class AudioRacApplication extends Application {

	//public static String CFG_AUDIORAC_SERVER_URL = "http://www.audiorac.kr";
	public static String CFG_AUDIORAC_SERVER_URL = "http://61.32.69.226:8082";
	//public static String CFG_AUDIORAC_SERVER_URL = "http://192.168.45.25:8084";

	protected static String CFG_AUDIORAC_SITE_PREFIX = "";
	protected  String CFG_DRM_FOLDER			= "/Music/CPDRM/";

	@Override
	public void onCreate() {
		super.onCreate();
	}

	/**
	 * 사이트 구분 prefix
	 * @return
	 */
	protected static String getSitePrefix() {
		return CFG_AUDIORAC_SITE_PREFIX;
	}

	/**
	 * 다운로드 MP3 파일 저장폴더 (full path) - 어플리케이션 별로 달라짐
	 * @return
	 */
	public String getDRMFolder() {
		String drmFolder = Environment.getExternalStorageDirectory() + CFG_DRM_FOLDER;
		return drmFolder;
	}

	/*=
	 * 서버에서 오디오락 도서관(library) 또는 대학교(school) 목록을 읽어와 리턴
	 * @return
	 */
	public static ArrayList<Record> getAudioRacSiteList(Context context, String groupType) {
		String response = "";
		
		try {
			groupType = groupType;
			String url = CFG_AUDIORAC_SERVER_URL + Common.URL_GET_SITE_LIST + "?group_type=" + CPDRMUtil.makeUTF8(groupType) +
																		"&prefix=" + getSitePrefix() +
																		"&appVer=" + Utils.getAppVersion(context) +
																		"&nocache=" + CPDRMUtil.getTimestamp();
			response = JSONHelper.get(url);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return HttpUtil.response2ItemList(response);
	}

	/**
	 * 서버에 사용자 로그인 요청
	 * @param siteUrl
	 * @param uid
	 * @param pwd
	 * @return
	 */
	public static String requestUserLogin(Context context, String siteUrl, String uid, String pwd) {
		String response = "";
		
		try {
			// http://www.audiorac.kr/api/mobile/userLogin.php?usid=conpo&usname=컨포시범&paid=2222&nocache=111111
			String loginURL = HttpUtil.verifyUrl(siteUrl + Common.URL_REQ_LOGIN);
			
			loginURL = loginURL.replace("{usid}", uid);
			loginURL = loginURL.replace("{usname}", pwd);
			loginURL += ("&appVer=" + Utils.getAppVersion(context));
			loginURL += ("&nocache=" + CPDRMUtil.getTimestamp());

			response = JSONHelper.get(loginURL);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return response.trim();
	}

	/**
	 * 서버에 다운로드 파일 리스트 요청
	 * @return
	 */
	public static String requestFileList() {
		String response = "";
		
		try {
			String fileListURL = HttpUtil.verifyUrl(LoginInfo.getSiteURL() + Common.URL_GET_FILELIST + LoginInfo.getUserId() + 
														".txt?1=1&nocache=" + CPDRMUtil.getTimestamp());

			response = JSONHelper.get(fileListURL);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return response;
	}

	/**
	 * 서버에 앨범 이미지 URL 요청
	 * @param volumnId
	 * @return
	 */
	public static String requestAlbumArtURL(String volumnId) {
		String response = "";
		
		try {
			String albumartURL = HttpUtil.verifyUrl(LoginInfo.getSiteURL() + Common.URL_GET_ALBUMART + "&nocache=" + CPDRMUtil.getTimestamp());
			
			albumartURL = albumartURL.replace("{volumeid}", CPDRMUtil.makeUTF8(volumnId));
			
			response = JSONHelper.get(albumartURL);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return response;
	}
	
	/**
	 * 앨범 제한시간 요청
	 * @param volumnId
	 * @param installmentId
	 * @return
	 */
	public static String requestLimit(String volumnId, String installmentId) {
		String response = "";
		
		try {
			String limitURL = HttpUtil.verifyUrl(LoginInfo.getSiteURL() + Common.URL_GET_LIMIT + "&nocache=" + CPDRMUtil.getTimestamp());
			
			limitURL = limitURL.replace("{volumeid}", CPDRMUtil.makeUTF8(volumnId));
			limitURL = limitURL.replace("{installmentid}", CPDRMUtil.makeUTF8(installmentId));
			limitURL = limitURL.replace("{userid}", CPDRMUtil.makeUTF8(LoginInfo.getUserId()));
			
			response = JSONHelper.get(limitURL);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return response;
	}
	
	/**
	 * 서버에 앨범 트랙들의 Play Time을 요청
	 * @param volumnId
	 * @return
	 */
	public static Record requestDuration(String volumnId) {
		Record rec = new Record();
		String response = "";
		
		try {
			String durationURL = HttpUtil.verifyUrl(LoginInfo.getSiteURL() + "/audio/api/volumnDuration.php?paid=2222&void={volumeid}" + "&nocache=" + CPDRMUtil.getTimestamp());
			
			durationURL = durationURL.replace("{volumeid}", CPDRMUtil.makeUTF8(volumnId));
			
			response = JSONHelper.get(durationURL);
			
			JSONObject obj = new JSONObject(response);
		    rec = JSONUtil.toRecord(obj);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return rec;
	}

	public static Record searchCourseByBarcode(String barcode, String type) {
		Record rec = new Record();
		String response = "";

		try {
			String searchURL = HttpUtil.verifyUrl(LoginInfo.getSiteURL() + "/audio/api/search_barcode.php?barcode={barcode}&type={type}" + "&nocache=" + CPDRMUtil.getTimestamp());

			searchURL = searchURL.replace("{barcode}", barcode);
			searchURL = searchURL.replace("{type}", type);

			response = JSONHelper.get(searchURL);

			JSONObject obj = new JSONObject(response);
			rec = JSONUtil.toRecord(obj);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return rec;
	}
	

}
