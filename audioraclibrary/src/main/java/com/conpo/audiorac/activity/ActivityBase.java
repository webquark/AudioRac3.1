package com.conpo.audiorac.activity;

import com.conpo.audiorac.application.AudioRacApplication;
import com.conpo.audiorac.library.R;
import com.conpo.audiorac.widget.CustomDialog;
import com.conpo.audiorac.widget.SimpleProgressDialog;
import com.google.android.material.snackbar.Snackbar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityBase extends AppCompatActivity {
	private static final String LOG_TAG = "FragmentBase";

	protected Context mContext;

	protected SimpleProgressDialog mProgressDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

	}

	/**
	 * CoAngler 어플리케이션 구하기
	 * @return
	 */
	public AudioRacApplication getAudioRacApplication() {
		return (AudioRacApplication)this.getApplication();
	}

	public boolean checkAppPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				if (!PermissionActivity.checkREAD_MEDIA_AUDIOPermission(mContext)) {
					return false;
				}

			} else {
				if (!PermissionActivity.checkREAD_STORAGEPermission(mContext)) {
					return false;
				}
			}

			return PermissionActivity.checkCAMERAPermission(mContext);

		} else {
			return true;
		}
	}

	/**
	 * 앱 권한요청 액티비티 런치
	 */
	public void launchPermission(ActivityResultCallback<ActivityResult> callback) {
		ActivityResultLauncher<Intent> permissionActivityLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				callback);

		Intent intent = new Intent(this, PermissionActivity.class);
		permissionActivityLauncher.launch(intent);
	}

	public void showProgress() {
		showProgress(null, 0);
	}

	public void showProgress(String message) {
		showProgress(message, 0);
	}

	public void showProgress(int resId) {
		showProgress(getString(resId), 0);
	}

	public void showProgress(String message, int type) {
		if (mProgressDlg == null) {
			mProgressDlg = SimpleProgressDialog.show(this, message, type);

		} else if (mProgressDlg.getProgressType() != type) {
			mProgressDlg = null;
			mProgressDlg = SimpleProgressDialog.show(this, message, type);

		} else {
			mProgressDlg.show();
		}
	}

	public void hideProgress() {
		if (mProgressDlg != null)
			mProgressDlg.dismiss();
	}

	public void setProgressMax(int max) {
		mProgressDlg.setMax(max);
	}

	public void setProgressPos(int value) {
		mProgressDlg.setProgress(value);
	}

	public void setProgressMessage(String message) {
		mProgressDlg.setMessage(message);
	}

	public void setProgressDlgListener(SimpleProgressDialog.ProgressDlgListener listener) {
		mProgressDlg.setProgressDlgListener(listener);
	}

	/**
	 * Shows a {@link Snackbar} using {@code text}.
	 *
	 * @param text The Snackbar text.
	 */
	private void showSnackbar(final String text) {
		View container = findViewById(android.R.id.content);
		if (container != null) {
			Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
		}
	}

	/**
	 * Shows a {@link Snackbar}.
	 *
	 * @param mainTextStringId The id for the string resource for the Snackbar text.
	 * @param actionStringId   The text of the action item.
	 * @param listener         The listener associated with the Snackbar action.
	 */
	private void showSnackbar(final int mainTextStringId, final int actionStringId,
							  View.OnClickListener listener) {
		Snackbar.make(findViewById(android.R.id.content),
						getString(mainTextStringId),
						Snackbar.LENGTH_INDEFINITE)
				.setAction(getString(actionStringId), listener).show();
	}

	public interface AlertDialogCallback {
		public void onOKClick();
	}

	public int Alert(int msgResId) {
		return Alert(this.getResources().getString(msgResId));
	}

	public int Alert(CharSequence msg) {
		return Alert(msg, null);
	}

	public int Alert(int msgResId, final AlertDialogCallback callback) {
		return Alert(this.getResources().getString(msgResId), callback);
	}

	public int Alert(CharSequence msg, final AlertDialogCallback callback) {
		CustomDialog.Builder alert = new CustomDialog.Builder(ActivityBase.this);

		// set dialog title & message
		alert.setTitle(R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close the dialog box and do nothing
						dialog.dismiss();

						if (callback != null) {
							callback.onOKClick();
						}
					}
				});

		CustomDialog alertDialog = alert.create();
		alertDialog.show();

		return 1;
	}

	public int Confirm(String msg, DialogInterface.OnClickListener okListener) {
		return Confirm(msg, okListener, null);
	}

	public int Confirm(int msgResId, DialogInterface.OnClickListener okListener) {
		return Confirm(this.getResources().getString(msgResId), okListener, null);
	}

	public int Confirm(int msgResId, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
		return Confirm(this.getResources().getString(msgResId), okListener, cancelListener);
	}

	public int Confirm(String msg, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
		CustomDialog.Builder alert = new CustomDialog.Builder(ActivityBase.this);

		// set dialog title & message
		alert.setTitle(R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(R.string.ok, okListener);

		if (cancelListener != null) {
			alert.setNegativeButton(R.string.cancel, cancelListener);
		} else {
			alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
		}

		CustomDialog alertDialog = alert.create();
		alertDialog.show();

		return 1;
	}


	public void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	public void showToast(int resId) {
		showToast(this.getString(resId));
	}

	public void reportServerError(String errorMessage) {
		showToast(errorMessage);
	}

	/**
	 * API 호출결과 반환된 데이터가 없음을 리포트함
	 */
	protected void reportNoData() {
		showToast("데이터가 없습니다.");
		Log.i(LOG_TAG, "데이터가 없습니다.");
	}
}