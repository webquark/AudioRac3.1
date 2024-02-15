package com.conpo.audiorac.activity;

import com.conpo.audiorac.library.R;
import com.conpo.audiorac.util.Utils;
import com.conpo.audiorac.widget.webview.ObservableWebView;
import com.conpo.audiorac.widget.webview.WebChromeClientBase;
import com.conpo.audiorac.widget.webview.WebCommand;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * 웹뷰 액티비티 Base Class
 * @author hansolo
 *
 */
public class WebViewActivityBase extends ActivityBase 
					implements WebChromeClientBase.WebCommandListener, ObservableWebView.OnScrollChangeListener,
						View.OnClickListener {
	
	private static final String LOG_TAG = "WebView";
	
	protected Context mContext;
	protected ProgressBar mProgressBar;
	protected ObservableWebView mWebView;
	protected WebChromeClientBase mWebChromeClient;
	protected String mUrl;
	
	protected Animation mSlideDown;
	protected Animation mSlideUp;
	
	protected Button mBtnGotoTop;
	protected ImageButton mBtnGoBack;

	protected RelativeLayout mContainer;
//	protected FrameLayout mBottomBar;
//	protected ImageView mBtnGoBack;
//	protected ImageView mBtnGoForward;
//	protected ImageView mBtnGoHome;
//	protected ImageView mBtnRefresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_webview);
		
		mContext = this;
		
		String url = getIntent().getStringExtra("url");
		
		if (url != null)
			mUrl = url;

		
		mContainer = (RelativeLayout) findViewById(R.id.container);
		mContainer.setOnClickListener(this);
		
		mProgressBar = (ProgressBar) findViewById(R.id.progress);
		mProgressBar.setVisibility(View.GONE);

		mBtnGoBack = (ImageButton)findViewById(R.id.btn_goback);
		mBtnGoBack.setOnClickListener(this);

		mBtnGotoTop = (Button)findViewById(R.id.btn_gototop);
		mBtnGotoTop.setOnClickListener(this);

		
		mWebView = (ObservableWebView) findViewById(R.id.webview);
		
		WebSettings settings = mWebView.getSettings();
		
		settings.setDefaultTextEncodingName("EUC-KR");
        settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDomStorageEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDatabasePath("/data/data/"+this.getPackageName()+"/databases/");
		settings.setUserAgentString(settings.getUserAgentString() + " " + Utils.getUserAgentString(mContext));
		
        mWebChromeClient = new WebChromeClientBase(mContext);
        mWebChromeClient.addWebCommandHandler(this);


        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(new InnerWebViewClient()); // forces it to open in app
        
        mWebView.setOnScrollChangeListener(this);
        
        if (savedInstanceState != null) {
        	mUrl = savedInstanceState.getString("url");
        }
        
        if (mUrl != null)
        	mWebView.loadUrl(mUrl);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("url", mUrl);
		
		super.onSaveInstanceState(outState);
		
		//mWebView.saveState(outState);
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
//		int topBarHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

//		if (top > topBarHeight) {
//			mBtnGotoTop.setVisibility(View.VISIBLE);
//		} else {
//			mBtnGotoTop.setVisibility(View.GONE);
//		}


//		int height = (int) Math.floor(mWebView.getContentHeight() * mWebView.getScale());
//		int webViewHeight = mWebView.getMeasuredHeight();
//		if (top + webViewHeight >= height){
//			if (mBottomBar.getVisibility() == View.VISIBLE)
//				mBottomBar.startAnimation(mSlideDown);
//		}
//		else {
//			if (mBottomBar.getVisibility() == View.GONE)
//				mBottomBar.startAnimation(mSlideUp);
//		}
	}

	public class InnerWebViewClient extends WebViewClient {
		@Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        ////view.loadUrl(url);
	        ////return true;
	        
	        if (url.toLowerCase().startsWith("http") || url.toLowerCase().startsWith("https") || url.toLowerCase().startsWith("file")) {
	        	view.loadUrl(url);
            }
            else {
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
                	Log.d("JSLogs", "Webview Error:" + e.getMessage());;
                }
            }
	        
	        return (true);
	    }

	    @Override     
	    public void onReceivedError(WebView view, int errorcode, String description, String fallingUrl) {   
	    	Log.d("WEBView", "error : "+errorcode);
	    	Log.d("WEBView", "error : "+description);    	
	    	Log.d("WEBView", "error : "+fallingUrl);
	    	
	    	mWebView.loadUrl("file:///android_asset/timeout.html");
	    	
	    	new AlertDialog.Builder(view.getContext())  
	        .setTitle("알림")  
	        .setMessage(R.string.error_bad_network_state)
	        .setPositiveButton(android.R.string.ok,  
	                new AlertDialog.OnClickListener() {
	                    @Override
						public void onClick(DialogInterface dialog, int which) {
	                    }  
	                })  
	        .setCancelable(false)  
	        .create()  
	        .show();
	    }
	    
        @Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	super.onPageStarted(view, url, favicon);

			WebViewActivityBase.this.onPageStarted();
		}
	    
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			
			WebViewActivityBase.this.onPageFinished();
		}
	}
	
	@Override
	public void actWebCommand(String message, WebCommand webCmd) {
		String action = webCmd.getAction();
		
		if (action.equals("go_home")) {
			onBackPressed();
    	} else if (action.equals("new_win")) {
			/*
			 * 새로운 웹뷰 창 띄우기
			 */
			
		} else if (action.equals("download")) {
			/*
			 * 파일 다운로드
			 */
			this.onFileDownload();
		}
	}
	
	/**
	 * AudioRac 웹사이트의 플레이어에서 다운로드를 시작했을 때 호출됨
	 */
	public void onFileDownload() {
		return;
	}

	public void onPageStarted() {
		this.showProgress();

		mBtnGoBack.setVisibility(View.GONE);
	}

    /**
     * 새로운 페이지가 불려지면 실행
     */
    public void onPageFinished() {
    	this.hideProgress();

    	if (mWebView.canGoBack()) {
			mBtnGoBack.setVisibility(View.VISIBLE);
		} else {
			mBtnGoBack.setVisibility(View.GONE);
		}

    	/////mBtnGoForward.setEnabled(mWebView.canGoForward());
    }
    
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if (id == R.id.container) {
			//mBottomBar.setVisibility(View.VISIBLE);
		} else if (id == R.id.btn_gototop) {
			mWebView.scrollTo(0, 0);

		} else if (id == R.id.btn_goback) {
			mWebView.goBack();
		}
//		else if (id == R.id.btn_goforward) {
//			mWebView.goForward();
//		}
//		else if (id == R.id.btn_gohome) {
//			super.onBackPressed();
//		}
//		else if (id == R.id.btn_refresh) {
//			mWebView.reload();
//		}
		
	}

	@Override
	public void onBackPressed() {
		if (mWebView.canGoBack() == true) {
            mWebView.goBack();

		} else {
			super.onBackPressed();
		}
	}
}
