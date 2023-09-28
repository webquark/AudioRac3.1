package com.conpo.audiorac.widget.webview;

import com.conpo.audiorac.activity.WebViewActivityBase;

import android.content.Context;
import android.webkit.WebView;

public class MenuWebChromeClient extends WebChromeClientBase {
	
	public MenuWebChromeClient(Context context) {
		super(context);
	}
	
	@Override
	public void onProgressChanged(WebView view, int progress) {
    	((WebViewActivityBase) mContext).setProgress1(progress);
	}
}
