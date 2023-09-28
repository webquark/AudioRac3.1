package com.conpo.audiorac.widget;

import com.conpo.audiorac.library.R;
import com.conpo.audiorac.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SimpleProgressDialog extends CustomDialog {
	
	private static ProgressBar mProgressBar = null;
	private static TextView mTvMessage = null;

	/**
	 * 프로그레스 바 타입
	 * 0: progressBarStyleLarge, 1: progressBarStyleHorizontal
	 */
	private static int mProgressStyleType = 0;
	
	public SimpleProgressDialog(Context context, int theme) {
		super(context, theme);
	}
	
	public SimpleProgressDialog(Context context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setBackKeyEnabled(false);
	}
	
	public static class Builder extends CustomDialog.Builder { 
		
		public Builder(Context context) {
            super(context);
        }
	    
		@Override
		public SimpleProgressDialog create() {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			// instantiate the dialog with the custom Theme
	        final SimpleProgressDialog dialog = new SimpleProgressDialog(mContext, R.style.CustomDialog);
	        
	        View layout;
	        if (mProgressStyleType == 0) {
	        	layout = inflater.inflate(R.layout.dialog_simple_progress, null);

			} else {
	        	layout = inflater.inflate(R.layout.dialog_horizontal_progress, null);
	        }
	        
	        //dialog.setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
	        // set the dialog message
	        mTvMessage = (TextView)layout.findViewById(R.id.message);
	        mProgressBar = (ProgressBar)layout.findViewById(R.id.progressBar);
	        mTvMessage.setVisibility(View.GONE);
	        
	        if (mProgressStyleType == 1) {
	        	DisplayMetrics displayMetrics = new DisplayMetrics();
	        	((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	        	int width = (int)(displayMetrics.widthPixels * 0.8);
	        	
	        	mProgressBar.setMinimumWidth(width);
	        }

			Button btn = layout.findViewById(R.id.btn_stop_download);
			if (btn != null) {
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mProgressDlgListener != null) {
							mProgressDlgListener.onProgressStop();
						}
					}
				});
			}

	        dialog.setContentView(layout);
	        
	        return (SimpleProgressDialog)dialog;
		}
	}

	public static SimpleProgressDialog show(Context context) {
		return show(context, null, 0);
	}
	
	public static SimpleProgressDialog show(Context context, String message, int type) {
		mProgressStyleType = type;
		
		SimpleProgressDialog.Builder progress = new SimpleProgressDialog.Builder(context);
		 
		SimpleProgressDialog progressDialog = progress.create();
		progressDialog.setMessage(message);
		progressDialog.show();
		
		return progressDialog;
	}

	public int getProgressType() {
		return mProgressStyleType;
	}
	
	public void setMax(int max) {
		mProgressBar.setMax(max);
	}
	
	public void setProgress(int value) {
		mProgressBar.setProgress(value);
	}
	
	public void setMessage(String message) {
		if (message != null) {
			mTvMessage.setText(message);
			mTvMessage.setVisibility(View.VISIBLE);
		}
	}

	private static ProgressDlgListener mProgressDlgListener;

	public interface ProgressDlgListener {
		public void onProgressStop();
	}

	public void setProgressDlgListener(ProgressDlgListener listener) {
		mProgressDlgListener = listener;
	}
}
