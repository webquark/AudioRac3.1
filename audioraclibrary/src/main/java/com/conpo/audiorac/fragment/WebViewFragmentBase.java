package com.conpo.audiorac.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.conpo.audiorac.library.R;
import com.conpo.audiorac.util.Utils;
import com.conpo.audiorac.widget.webview.InnerWebViewClient;
import com.conpo.audiorac.widget.webview.ObservableWebView;
import com.conpo.audiorac.widget.webview.WebChromeClientBase;
import com.conpo.audiorac.widget.webview.WebCommand;

public class WebViewFragmentBase extends FragmentBase
        implements WebChromeClientBase.WebCommandListener, ObservableWebView.OnScrollChangeListener,
        View.OnClickListener {

    private static final String LOG_TAG = "WebView";

    protected ProgressBar mProgressBar;
    protected ObservableWebView mWebView;
    protected WebChromeClientBase mWebChromeClient;
    protected String mUrl;

    protected Button mBtnGotoTop;
    protected ImageButton mBtnGoBack;

    protected RelativeLayout mWebContainer;

    protected ViewGroup mErrorContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_webview, container, false);

        if (mContext == null) {
            mContext = getContext();
        }

        mWebContainer = mView.findViewById(R.id.web_container);
        mWebContainer.setOnClickListener(this);
        mWebContainer.setVisibility(View.VISIBLE);

        mProgressBar = mView.findViewById(R.id.progress);
        mProgressBar.setVisibility(View.GONE);

        mBtnGoBack = mView.findViewById(R.id.btn_go_back);
        mBtnGoBack.setOnClickListener(this);
        mBtnGoBack.setVisibility(View.GONE);

        mBtnGotoTop = mView.findViewById(R.id.btn_gototop);
        mBtnGotoTop.setOnClickListener(this);


        mWebView = mView.findViewById(R.id.webview);

        WebSettings settings = mWebView.getSettings();

        settings.setDefaultTextEncodingName("EUC-KR");
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath("/data/data/" + mContext.getPackageName() + "/databases/");
        settings.setUserAgentString(settings.getUserAgentString() + " " + Utils.getUserAgentString(mContext));

        mWebChromeClient = new WebChromeClientBase(mContext);
        mWebChromeClient.addWebCommandHandler(this);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setOnScrollChangeListener(this);


        initializeErrorContainer();

        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString("url");
        }

        if (mUrl != null)
            mWebView.loadUrl(mUrl);

        return mView;
    }

    /**
     * 웹뷰 클라이언트
     */
    public class MyWebViewClient extends InnerWebViewClient {
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
                    Log.d(LOG_TAG, "Webview Error:" + e.getMessage());
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

            onMyPageStarted();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Log.d(LOG_TAG, "URL: " + url);
            onMyPageFinished();
        }
    }

    /**
     * 새로운 페이지로 이동할때 실행
     */
    public void onMyPageStarted() {
        this.showProgress();

        hideBackButton();
    }

    /**
     * 새로운 페이지가 불려지면 실행
     */
    public void onMyPageFinished() {
        this.hideProgress();

        if (mWebView.canGoBack()) {
            showBackButton();
        } else {
            hideBackButton();
        }
    }

    public void hideBackButton() {
        mBtnGoBack.setVisibility(View.GONE);
    }

    public void showBackButton() {
        mBtnGoBack.setVisibility(View.VISIBLE);
    }

    /**
     * 에러 출력을 위한 컨테이너 뷰 초기화
     */
    private void initializeErrorContainer() {
        mErrorContainer = mView.findViewById(R.id.err_container);
        mView.findViewById(R.id.btn_goto_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivity.setNavigationTab(1);  // 다운로드 탭으로 이동
            }
        });

        mView.findViewById(R.id.btn_retry_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                if (Utils.checkNetworkState(mContext)) {
                    gotoHome();

                } else {
                    showToast(R.string.error_bad_network_state);
                }
            }
        });

        mErrorContainer.setVisibility(View.GONE);
    }

    public void showNetworkError() {
        mWebContainer.setVisibility(View.GONE);
        mErrorContainer.setVisibility(View.VISIBLE);
    }

    public void setWebViewClient(InnerWebViewClient client) {
        mWebView.setWebViewClient(client);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("url", mUrl);

        super.onSaveInstanceState(outState);
    }

    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    public void setProgress1(int progress) {
        mProgressBar.setProgress(progress);
    }

    @Override
    public void onScroll(int left, int top) {
        // scroll height가 50dp를 넘으면 [TOP] 버튼 표시
        int topBarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

        if (top > topBarHeight) {
            mBtnGotoTop.setVisibility(View.VISIBLE);
        } else {
            mBtnGotoTop.setVisibility(View.GONE);
        }
    }

    @Override
    public void actWebCommand(String message, WebCommand webCmd) {
        String action = webCmd.getAction();

        if (action.equals("download")) {
            /*
             * 파일 다운로드
             */
            this.onFileDownload();
        }
    }

    public void gotoHome() {
        mErrorContainer.setVisibility(View.GONE);
        mWebContainer.setVisibility(View.VISIBLE);
    }

    /**
     * 웹뷰의 미디어 플레이어 멈추기
     */
    public void stopAudioPlayer() {
        if (mWebView != null) {
            mWebView.loadUrl("javascript:playerStop();");
        }
    }

    /**
     * AudioRac 웹사이트의 플레이어에서 다운로드를 시작했을 때 호출됨
     */
    public void onFileDownload() {
        return;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_gototop) {
            mWebView.scrollTo(0, 0);

        } else if (id == R.id.btn_go_back) {
            onBackPressed();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;

        } else {
            return false;
        }
    }
}
