package com.conpo.audiorac.widget.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;


/**
 * 이벤트 리스너 설정 가능한 웹뷰
 * @author iconlab
 *
 */
public class ObservableWebView extends WebView {

	private OnScrollChangeListener mOnScrollChangeListener;	
	
	public static interface OnScrollChangeListener {
		public void onScroll(int l, int t);
	}
	
	public void setOnScrollChangeListener(OnScrollChangeListener listener) {
		mOnScrollChangeListener = listener;
	}
	
	public ObservableWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		
		if (mOnScrollChangeListener != null) {
			mOnScrollChangeListener.onScroll(l, t);
		}
	}
	
	
}
