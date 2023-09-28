package com.conpo.audiorac.widget.webview;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import android.util.Log;

public class WebCommand {
	private static final String LOG_TAG = "WebCommand";
	
	public static final String CMD_PREFIX = "APP_CMD>";
	protected Hashtable<String, String> mParams = new Hashtable<String, String>();

	public static WebCommand cmdFromString(String str) {		
		if (!str.startsWith(CMD_PREFIX) || str == null) return null;
		
		WebCommand cmd = new WebCommand();
		
		if (cmd.parseCommand(str)) return cmd;
		
		return null;
	}
	
	static <T> T[] append(T[] arr, T element) {
	    final int N = arr.length;
	    arr = Arrays.copyOf(arr, N + 1);
	    arr[N] = element;

	    return arr;
	}
	
	public boolean parseCommand(String str) {
		if (!str.startsWith(CMD_PREFIX)) return false;
		
		str = str.substring(CMD_PREFIX.length());
		String[] params = str.split("\\&");
		
		for (int i=0; i < params.length; ++i) {
			String[] param = params[i].split("\\=");
			
			// web command에서 value에 포함시켜 보내는 '=' 문자도 처리 가능하도록
			if (param.length > 1) {
				param[1] = params[i].substring(param[0].length() + 1);
			} else {
				append(param, "");
			}

			if (param.length < 2) {
				Log.e(LOG_TAG, String.format("parse error: %d, %s", i, param));
			} else {
				Log.e(LOG_TAG, param[0] + " ===>" + param[1]);
				mParams.put(param[0], param[1]);
			}
		}
		
		return true;
	}
	
	public boolean parseCommandForLink(String prefix, String str) {
		if (!str.startsWith(prefix)) return false;
		
		str = str.substring(CMD_PREFIX.length());
		String[] params = str.split("\\&");
		
		for (int i=0; i < params.length; ++i) {
			String[] param = params[i].split("\\=");
			
			// web command에서 value에 포함시켜 보내는 '=' 문자도 처리 가능하도록
			param[1] = params[i].substring(param[0].length() + 1);
			
			if (param.length < 2) {
				Log.e(LOG_TAG, String.format("parse error: %d, %s", i, param));
			}
			else {
				Log.e(LOG_TAG, param[0] + " ===>" + param[1]);
				mParams.put(param[0], param[1]);
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		String ret = CMD_PREFIX;
		boolean bFirst = true;
		
		Enumeration<String> enumerationKey = mParams.keys();
	    
		while (enumerationKey.hasMoreElements()) {
	        String key = enumerationKey.nextElement();
	        if (bFirst)
	        	bFirst = false;
	        else
	        	ret += "&";
	        
	        ret += String.format("%s=%s", key, mParams.get(key));
	    }
		
		return ret;
	}
	
	public String getAction() {
		return mParams.get("action");
	}
	
	public String get(String key) {
		String value = mParams.get(key);
	
		return value;
	}
}
