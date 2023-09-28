package com.conpo.audiorac.widget.webview;

import com.conpo.audiorac.fragment.MyAudioFragment;
import com.conpo.audiorac.library.R;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class InnerWebViewClient extends WebViewClient {
	@Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override     
    public void onReceivedError(WebView view, int errorcode,String description, String fallingUrl) {   
    	Log.d("WEBView", "error : "+errorcode);
    	Log.d("WEBView", "error : "+description);    	
    	Log.d("WEBView", "error : "+fallingUrl);

    	new AlertDialog.Builder(view.getContext())
        .setTitle(R.string.alert)
        .setMessage(R.string.error_bad_network_state)
        .setPositiveButton(android.R.string.ok,
                new AlertDialog.OnClickListener()
                {
                    @Override
					public void onClick(DialogInterface dialog, int which) {
                    }
                })
        .setCancelable(false)
        .create()
        .show();
    }
}
