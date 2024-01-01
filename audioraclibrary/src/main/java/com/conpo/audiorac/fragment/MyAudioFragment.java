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
import com.conpo.audiorac.library.R;
import com.conpo.audiorac.util.HttpUtil;
import com.conpo.audiorac.util.Utils;
import com.conpo.audiorac.widget.webview.InnerWebViewClient;

import java.net.URLEncoder;

/**
 * 마이 오디오 프래그먼트
 */
public class MyAudioFragment extends WebViewFragmentBase {

    private static final String LOG_TAG = "MyView";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);

        Log.d(LOG_TAG, "MyView1");
        LoginInfo.loadPreferences(mContext);

        gotoHome();

        Log.d(LOG_TAG, "MyView2");

        return mView;
    }

    public void gotoHome() {
        if (!Utils.checkNetworkState(mContext)) {
            showNetworkError();
            return;
        }

        super.gotoHome();

        if (mWebView == null) {
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
