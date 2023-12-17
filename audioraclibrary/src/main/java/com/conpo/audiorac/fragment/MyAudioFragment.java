package com.conpo.audiorac.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.application.LoginInfo;
import com.conpo.audiorac.util.HttpUtil;
import com.conpo.audiorac.util.Utils;
import com.conpo.audiorac.widget.webview.InnerWebViewClient;

import java.net.URLEncoder;

/**
 * 마이 오디오 프래그먼트
 */
public class MyAudioFragment extends WebViewFragmentBase {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);

        setWebViewClient(new MyAudioWebViewClient());

        try {
            mUrl = LoginInfo.getSiteURL() + Common.URL_MOBILE_LOGIN;
            mUrl = mUrl.replace("{usrId}", LoginInfo.getUserId());
            mUrl = mUrl.replace("{usrName}", LoginInfo.getUserPwd()); //URLEncoder.encode(LoginInfo.getUserPwd(), "EUC-KR"));
            mUrl = mUrl.replace("{dest}", "MyAudio");
            mUrl = mUrl.replace("{appVer}", Utils.getAppVersion(mContext));

            mUrl = HttpUtil.verifyUrl(mUrl);
            mWebView.loadUrl(mUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mView;
    }

    public class MyAudioWebViewClient extends InnerWebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.toLowerCase().startsWith("http") || url.toLowerCase().startsWith("https") || url.toLowerCase().startsWith("file")) {
                view.loadUrl(url);

            } else {
                Intent intent;
                try {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } catch (ActivityNotFoundException e) {
                    if (url.indexOf("iaudienb2b") >= 0) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.app.audiobook.startup"));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.d("JSLogs", "Webview Error:" + e.getMessage());
                    ;
                }
            }

            return (true);
        }

        @Override
        public void onReceivedError(WebView view, int errorcode, String description, String fallingUrl) {
            Log.d("WEBView", "error : "+errorcode);
            Log.d("WEBView", "error : "+description);
            Log.d("WEBView", "error : "+fallingUrl);

            showNetworkError();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            MyAudioFragment.this.onPageStarted();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            MyAudioFragment.this.onPageFinished();
        }
    }

    public void gotoHome() {
        super.gotoHome();

        if (mWebView == null) {
            //mWebView.goBackOrForward(-1 * (mWebView.copyBackForwardList().getSize() - 2));
            return;
        }

        try {
            mUrl = LoginInfo.getSiteURL() + Common.URL_MOBILE_LOGIN;
            mUrl = mUrl.replace("{usrId}", LoginInfo.getUserId());
            mUrl = mUrl.replace("{usrName}", LoginInfo.getUserPwd()); //URLEncoder.encode(LoginInfo.getUserPwd(), "EUC-KR"));
            mUrl = mUrl.replace("{dest}", "MyAudio");
            mUrl = mUrl.replace("{appVer}", Utils.getAppVersion(mContext));

            mUrl = HttpUtil.verifyUrl(mUrl);
            mWebView.loadUrl(mUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFileDownload() {
        mMainActivity.onFileDownload();
    }

}
