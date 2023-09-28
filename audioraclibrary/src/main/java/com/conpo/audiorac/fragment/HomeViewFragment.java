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
 * 오디오락 모바일 홈 액티비티
 */
public class HomeViewFragment extends WebViewFragmentBase {

    private static final String LOG_TAG = "HomeView";

    private String mCsCode;
    private String mChCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);

        setWebViewClient(new HomeWebViewClient());

        try {
            if (mCsCode != null && mCsCode.length() > 0) {
                mUrl = LoginInfo.getSiteURL() + Common.URL_MOBILE_LOGIN;
                mUrl = mUrl.replace("{usrId}", LoginInfo.getUserId());
                mUrl = mUrl.replace("{usrName}", LoginInfo.getUserPwd()); //URLEncoder.encode(LoginInfo.getUserPwd(), "EUC-KR"));
                mUrl = mUrl.replace("{dest}", "Course");
                mUrl = mUrl.replace("{csCode}", mCsCode);
                mUrl = mUrl.replace("{chCode}", mChCode);
                mUrl = mUrl.replace("{appVer}", Utils.getAppVersion(mContext));

            } else {
                mUrl = LoginInfo.getSiteURL() + Common.URL_MOBILE_LOGIN;
                mUrl = mUrl.replace("{usrId}", LoginInfo.getUserId());
                mUrl = mUrl.replace("{usrName}", LoginInfo.getUserPwd());   //URLEncoder.encode(LoginInfo.getUserPwd(), "EUC-KR"));
                mUrl = mUrl.replace("{dest}", "Main");
                mUrl = mUrl.replace("{appVer}", Utils.getAppVersion(mContext));
            }

            mUrl = HttpUtil.verifyUrl(mUrl);
            mWebView.loadUrl(mUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mView;
    }

    public class HomeWebViewClient extends InnerWebViewClient {
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

            HomeViewFragment.this.onPageStarted();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Log.d(LOG_TAG, "URL: " + url);
            HomeViewFragment.this.onPageFinished();
        }
    }

    @Override
    public void gotoHome() {
        super.gotoHome();

        if (mWebView == null)
            return;

        if (mCsCode != null) {
            try {
                mWebView.goBackOrForward(Integer.MIN_VALUE);
                mWebView.clearHistory();
//                mUrl = LoginInfo.getSiteURL() + "/mobile/login_proc_app.php?" +
//                        "user_id=" + LoginInfo.getUserId() +
//                        "&user_name=" + URLEncoder.encode(LoginInfo.getUserPwd(), "EUC-KR") +
//                        "&dest=Main";

                mUrl = LoginInfo.getSiteURL() + Common.URL_MOBILE_LOGIN;
                mUrl = mUrl.replace("{usrId}", LoginInfo.getUserId());
                mUrl = mUrl.replace("{usrName}", LoginInfo.getUserPwd());   //URLEncoder.encode(LoginInfo.getUserPwd(), "EUC-KR"));
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
                mUrl = LoginInfo.getSiteURL() + "/mobile/";
                mUrl = HttpUtil.verifyUrl(mUrl);
                mWebView.loadUrl(mUrl);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void gotoCourse(String csCode) {
        mUrl = LoginInfo.getSiteURL() + "/mobile/course/content.php?" +
                "cs_code=" + csCode;

        mUrl = HttpUtil.verifyUrl(mUrl);
        mWebView.loadUrl(mUrl);
    }

    /**
     * 웹뷰의 미디어 플레이어 멈추기
     */
    public void stopAudioPlayer() {
        mWebView.loadUrl("javascript:playerStop();");
    }

    @Override
    public void onFileDownload() {
        mMainActivity.onFileDownload();
    }

}
