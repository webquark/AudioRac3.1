package com.conpo.audiorac.widget;

import com.conpo.audiorac.library.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomDialog extends Dialog {
	
    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }
    
	public CustomDialog(Context context) {
        super(context);
    }
	
	/**
     * Helper class for creating a custom dialog
     */
	public static class Builder { 
		protected final Context mContext;
		
	    protected String  mTitle;
	    protected String  mMessage;
	    protected String  mPositiveButtonText;
	    protected String  mNeutralButtonText;
	    protected String  mNegativeButtonText;
	    protected DialogInterface.OnClickListener mPositiveButtonClickListener;
	    protected DialogInterface.OnClickListener mNeutralButtonClickListener;
	    protected DialogInterface.OnClickListener mNegativeButtonClickListener;
	    
	    protected View mContentView;

	    protected int  mLayoutResId = R.layout.dialog_custom;

	    protected OnDialogResultListner mDialogResultListener = null;
		
		public interface OnDialogResultListner {
			public void OnDialogResult(int requestCode, int resultCode, Intent data);
		}
		
		public void setOnDialogResultListner(OnDialogResultListner listener) {
			mDialogResultListener = listener;
		}
		
	    public Builder(Context context) {
            this.mContext = context;
        }
	    
		public CustomDialog create() {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			// instantiate the dialog with the custom Theme
	        final CustomDialog dialog = new CustomDialog(mContext, R.style.CustomDialog);

	        View layout = inflater.inflate(mLayoutResId, null);
	        dialog.addContentView(layout, new LayoutParams (LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));		//width,height

	        // set the dialog title
	        ((TextView)layout.findViewById(R.id.title)).setText( Html.fromHtml(mTitle) );

	        // set the content message
	        if (mMessage != null) {
	            ((TextView) layout.findViewById(R.id.message)).setText(mMessage);
	        } else if (mContentView != null) {
	            // if no message set
	            // add the contentView to the dialog body
	        	LinearLayout content = (LinearLayout) layout.findViewById(R.id.content);
	        	content.removeAllViews();
	        	content.addView(mContentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	        }

	        // set the confirm button
	        Button btnPositive = (Button) layout.findViewById(R.id.positiveButton);
            if (mPositiveButtonText != null) {
                btnPositive.setText(mPositiveButtonText);
                
                if (mPositiveButtonClickListener != null) {
                	//btnPositive.setOnClickListener(mPositiveButtonClickListener);
                	btnPositive.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    mPositiveButtonClickListener.onClick(dialog, 
                                            							 DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }

            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }
            
            // set the neutral button
	        Button btnNeutral = (Button) layout.findViewById(R.id.neutralButton);
            if (mNeutralButtonText != null) {
                btnNeutral.setText(mNeutralButtonText);
                
                if (mNeutralButtonClickListener != null) {
                	btnNeutral.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    mNeutralButtonClickListener.onClick(dialog, 
                                            							 DialogInterface.BUTTON_NEUTRAL);
                                }
                            });
                }

            } else {
                layout.findViewById(R.id.neutralButton).setVisibility(View.GONE);
            }
            
            // set the cancel button
            Button btnNegative = (Button) layout.findViewById(R.id.negativeButton);
            if (mNegativeButtonText != null) {
            	btnNegative.setText(mNegativeButtonText);
                
                if (mNegativeButtonClickListener != null) {
                	btnNegative.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    mNegativeButtonClickListener.onClick(dialog, 
                                            							DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }

            } else {
            	btnNegative.setVisibility(View.GONE);
            }
	        
	        dialog.setContentView(layout);
	        
	        return (CustomDialog)dialog;
		}
	
		public Builder setView(View v) {
			this.mContentView = v;
		    return this;
		}
		
		public View getView() {
	    	return mContentView;
	    }
		
		public Builder setPositiveButton(int resId, DialogInterface.OnClickListener listener) {
	        this.mPositiveButtonText = mContext.getString(resId);
	        this.mPositiveButtonClickListener = listener;
	        return this;
	    }
	
	    public Builder setPositiveButton(String text, DialogInterface.OnClickListener listener) {
	        this.mPositiveButtonText = text;
	        this.mPositiveButtonClickListener = listener;
	        return this;
	    }
	    
	    public Builder setNeutralButton(int resId, DialogInterface.OnClickListener listener) {
	        this.mNeutralButtonText = mContext.getString(resId);
	        this.mNeutralButtonClickListener = listener;
	        return this;
	    }
	
	    public Builder setNeutralButton(String text, DialogInterface.OnClickListener listener) {
	        this.mNeutralButtonText = text;
	        this.mNeutralButtonClickListener = listener;
	        return this;
	    }
	    
	    public Builder setNegativeButton(int resId, DialogInterface.OnClickListener listener) {
	        this.mNegativeButtonText = mContext.getString(resId);
	        this.mNegativeButtonClickListener = listener;
	        return this;
	    }
	
	    public Builder setNegativeButton(String text, DialogInterface.OnClickListener listener) {
	        this.mNegativeButtonText = text;
	        this.mNegativeButtonClickListener = listener;
	        return this;
	    }
		
	    public Builder setTitle(int textResId) {
	        mTitle = mContext.getString(textResId);
	        return this;
	    }

	    public Builder setTitle(CharSequence text) {
	        mTitle = text.toString();
	        return this;
	    }
	
	    public Builder setMessage(int textResId) {
	        mMessage = mContext.getString(textResId);
	        return this;
	    }
	
	    public Builder setMessage(CharSequence text) {
	        mMessage = text.toString();
	        return this;
	    }
	    
	    public void setResult(int requestCode, int resultCode, Intent data) {
	    	if (this.mDialogResultListener != null) {
	    		this.mDialogResultListener.OnDialogResult(requestCode, resultCode, data);
	    	}
	    }
	    
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setCanceledOnTouchOutside(false);
	}
	
	/**
	 * BACK 키를 이용해 dialog를 닫을 수 있게 할지 여부
	 * @param enabled
	 * @return
	 */
	public CustomDialog setBackKeyEnabled(boolean enabled) {
		this.setCancelable(enabled);
		
		return this;
	}
}
