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

import androidx.appcompat.app.AppCompatDelegate;

import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.application.LoginInfo;
import com.conpo.audiorac.util.HttpUtil;
import com.conpo.audiorac.util.Utils;
import com.conpo.audiorac.widget.webview.InnerWebViewClient;
import com.conpo.audiorac.widget.webview.WebCommand;

import java.net.URLEncoder;

/**
 * 오디오락 모바일 홈 액티비티
 */
public class HomeViewFragment extends WebViewFragmentBase {

    private static final String LOG_TAG = "HomeView";

    private String mCsCode;
    private String mChCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);

        LoginInfo.loadPreferences(mContext);

        gotoHome();

        return mView;
    }

    @Override
    public void gotoHome() {
        if (!Utils.checkNetworkState(mContext)) {
            showNetworkError();
            return;
        }

        super.gotoHome();

        if (mWebView == null)
            return;

        if (mCsCode != null) {
            try {
                mWebView.goBackOrForward(Integer.MIN_VALUE);
                mWebView.clearHistory();

                mUrl = LoginInfo.getSiteURL() + Common.URL_MOBILE_LOGIN;
                mUrl = mUrl.replace("{usrId}", LoginInfo.getUserId());
                mUrl = mUrl.replace("{usrName}", LoginInfo.getUserPwd());   //URLEncoder.encode(LoginInfo.getUserPwd(), "EUC-KR"));
                mUrl = mUrl.replace("{mode}", (LoginInfo.getUIMode() == AppCompatDelegate.MODE_NIGHT_NO) ? "Light" : "Dark");
                mUrl = mUrl.replace("{dest}", "Main");
                mUrl = mUrl.replace("{appVer}", Utils.getAppVersion(mContext));

                mUrl = HttpUtil.verifyUrl(mUrl);
                mWebView.loadUrl(mUrl);
                mCsCode = null;

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            //mWebView.goBackOrForward(-1 * (backwordSize - 2));
            try {
                mWebView.goBackOrForward(Integer.MIN_VALUE);
                mWebView.clearHistory();

                mUrl = LoginInfo.getSiteURL() + Common.URL_MOBILE_LOGIN;
                mUrl = mUrl.replace("{usrId}", LoginInfo.getUserId());
                mUrl = mUrl.replace("{usrName}", LoginInfo.getUserPwd());   //URLEncoder.encode(LoginInfo.getUserPwd(), "EUC-KR"));
                mUrl = mUrl.replace("{mode}", (LoginInfo.getUIMode() == AppCompatDelegate.MODE_NIGHT_NO) ? "Light" : "Dark");
                mUrl = mUrl.replace("{dest}", "Main");
                mUrl = mUrl.replace("{appVer}", Utils.getAppVersion(mContext));

                mUrl = HttpUtil.verifyUrl(mUrl);
                mWebView.loadUrl(mUrl);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void gotoCourse(String csCode) {
        mUrl = LoginInfo.getSiteURL() + "/audio/course/content.html?" + "cs=" + csCode;

        mUrl = HttpUtil.verifyUrl(mUrl);
        mWebView.loadUrl(mUrl);
    }

    @Override
    public void onFileDownload() {
        mMainActivity.onFileDownload();
    }

    @Override
    public void actWebCommand(String message, WebCommand webCmd) {
        super.actWebCommand(message, webCmd);

        String action = webCmd.getAction();

        if (action.equals("goMyAudio")) {
            /*
             * 마이 오디오로 이동
             */
            mMainActivity.setNavigationTab(2);

        }
    }
}
