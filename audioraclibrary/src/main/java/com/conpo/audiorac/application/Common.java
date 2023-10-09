package com.conpo.audiorac.application;

public class Common {

	private static Common _instance;
	
	public static final String TAG 					= "CPDRMPL";
	public static final String DRM_NAME 			= "CPDRM";
	public static final String REVERSE_NAME			= "CPDRM2";
	public static final String DRM_PREFIX			= "";
	public static final String DRM_EXTENSION		= ".mp3";
	public static final String DRM_FOLDER			= "/Music/CPDRM/";
	public static final String DRM_DUMMY			= "3J0YWx5Y3c3NzAxY29udGVudHNwb";
	public static final int DRM_HEADER_LENGTH 		= 1704801; //204801;
	public static final int DRM_PREFIX_LENGTH		= 1024;
	public static final int DRM_NAME_LENGTH 		= 5;
	public static final int DRM_PASTE_LENGTH		= 13;
	public static final int DRM_REVERSE_LENGTH		= 1024 * 16;	// 16384
	public static final int MSG_TIMER_EXPIRED 		= 1;
	public static final int BACKKEY_TIMEOUT 		= 2;
	public static final int MILLIS_IN_SEC 			= 1000;
	public static final int READ_BUFFER				= 1024 * 16;
	
	public static final String MSG_LOGININFO_DNFILE	= "com.conpo.cpdrm.message.LOGININFO.DNFILE";

	//public static final String URL_GET_VERSION	= "/audio/api/getVersion.php";
	//public static final String URL_GET_SITE_LIST	= "/audio/api/select_group.php";

	//public static final String URL_REQ_LOGIN 	  	= "/audio/api/userLogin2.php?usid={usid}&usname={usname}&paid=2222";
	//public static final String URL_MOBILE_LOGIN 	= "/mobile/login_proc_app.php?user_id={usrId}&user_name={usrName}&dest={dest}&cs_code={csCode}&ch_code={chCode}&appVer={appVer}";
	public static final String URL_GET_LIMIT		= "/audio/api/limitSec.php?volumeid={volumeid}&installmentid={installmentid}&userid={userid}&paid=2222";
	public static final String URL_SET_STATISTICS	= "/audio/api/streamLog.php?volumeid={volumeid}&installmentid={installmentid}&userid={userid}&paid=2222";
	public static final String URL_GET_ALBUMART	 	= "/audio/api/image.php?volumeid={volumeid}";
	public static final String URL_GET_FILELIST		= "/mobile/temp/";

	//--------------------------------------------------------
	public static final String URL_GET_SITE_LIST	= "/api/mobile/selectLibrary.php";
	public static final String URL_GET_VERSION		= "/api/mobile/getVersion.php";
	public static final String URL_REQ_LOGIN 	  	= "/api/mobile/userLogin.php?usid={usid}&usname={usname}&paid=2222";

	public static final String URL_MOBILE_LOGIN 	= "/api/mobile/login_proc_app.php?user_id={usrId}&user_name={usrName}&mode={mode}&dest={dest}&cs_code={csCode}&ch_code={chCode}&appVer={appVer}";

	static {
		_instance = new Common();
	}
	
	public static Common getInstance() {
		return _instance;
	}
	
	
}
