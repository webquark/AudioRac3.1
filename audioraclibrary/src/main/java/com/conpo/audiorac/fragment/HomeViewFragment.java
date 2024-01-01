package com.conpo.audiorac.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDelegate;

import com.conpo.audiorac.application.Common;
import com.conpo.audiorac.application.LoginInfo;
import com.conpo.audiorac.util.HttpUtil;
import com.conpo.audiorac.util.Utils;
import com.conpo.audiorac.widget.webview.WebCommand;

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

        Log.d(LOG_TAG, "HomeView1");

        LoginInfo.loadPreferences(mContext);

        gotoHome();

        Log.d(LOG_TAG, "HomeView2");

        return mView;
    }

    /**
     * AudioRac 홈으로 가기
     */
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

    /**
     * URL을 체크하여 필요한 경우 조치를 함
     * @return
     */
    public boolean checkHomeUrl() {
        if (mUrl != null) {
            if (mUrl.indexOf("mobileDownload.php") >= 0 || mUrl.indexOf("login_proc_app.php") >= 0) {
                // 이전에 다운로드를 했으면 빈화면에 멈춰있을 것이므로 홈으로 이동
                gotoHome();

                return true;
            }
        }

        return false;
    }

    @Override
    public void onMyPageFinished() {
        super.onMyPageFinished();

        mUrl = mWebView.getUrl();
        if (mUrl.indexOf("dest=Main") >= 0) {
            // gotoHome()을 통해 홈으로 이동한 경우
            hideBackButton();
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
