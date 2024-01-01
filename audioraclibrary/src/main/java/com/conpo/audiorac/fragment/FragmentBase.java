package com.conpo.audiorac.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.conpo.audiorac.activity.ActivityBase;
import com.conpo.audiorac.activity.MainActivity;
import com.conpo.audiorac.activity.PermissionActivity;
import com.conpo.audiorac.library.R;
import com.conpo.audiorac.widget.CustomDialog;


public class FragmentBase extends Fragment {

    private static final String LOG_TAG = "FragmentBase";

    protected MainActivity mMainActivity;

    protected Context mContext;
    protected View mView;

    protected ProgressBar mProgressBar = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_base, container, false);

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMainActivity = (MainActivity)getActivity();
        mContext = getContext();

        if (mContext != null) {
            fetchData();
        }
    }
    protected void fetchData() {

    }

    protected int getColor(@ColorRes int colorId) {
            return mContext.getColor(colorId);
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public void setProgressBar(ProgressBar view) {
        mProgressBar = view;
        hideProgress();
    }

    public void setProgressBar(int resId) {
        setProgressBar( mView.findViewById(resId));
    }

    public void showProgress() {
        if (mProgressBar != null) {
            if (mProgressBar.getParent() != null) {
                ((ViewGroup) mProgressBar.getParent()).setVisibility(View.VISIBLE);
            }

            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgress() {
        if (mProgressBar != null) {
            if (mProgressBar.getParent() != null) {
                ((ViewGroup) mProgressBar.getParent()).setVisibility(View.GONE);
            }

            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    public int Alert(int msgResId) {
        return Alert( this.getResources().getString(msgResId) );
    }

    public int Alert(CharSequence msg) {
        return Alert(msg, null);
    }

    public int Alert(int msgResId, final ActivityBase.AlertDialogCallback callback) {
        return Alert(this.getResources().getString(msgResId), callback);
    }

    public int Alert(CharSequence msg, final ActivityBase.AlertDialogCallback callback) {
        CustomDialog.Builder alert = new CustomDialog.Builder(mContext);

        // set dialog title & message
        alert.setTitle(R.string.app_name)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        if (callback != null) {
                            callback.onOKClick();
                        }
                    }
                })
                .create()
                .show();

        return 1;
    }

    public int Confirm(String title, String msg, DialogInterface.OnClickListener okListener) {
        return Confirm(title, msg, okListener, null);
    }

    public int Confirm(int titleResId, int msgResId, DialogInterface.OnClickListener okListener) {
        return Confirm(this.getResources().getString(titleResId), this.getResources().getString(msgResId), okListener, null);
    }

    public int Confirm(int titleResId, int msgResId, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        return Confirm(this.getResources().getString(titleResId), this.getResources().getString(msgResId), okListener, cancelListener);
    }

    public int Confirm(String title, String msg, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        CustomDialog.Builder confirm = new CustomDialog.Builder(mContext);

        // set dialog title & msg
        confirm.setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, okListener);

        if (cancelListener != null) {
            confirm.setNegativeButton(R.string.cancel, cancelListener);

        } else {
            confirm.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }

        confirm.create().show();

        return 1;
    }

    protected void showToast(int resId) {
        showToast(getString(resId));
    }

    protected void showToast(String text) {
        if (mContext != null) {
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * API 호출결과 반환된 데이터가 없음을 리포트함
     */
    protected void reportNoData() {
        showToast("데이터가 없습니다.");
        Log.i(LOG_TAG, "데이터가 없습니다.");
    }

    /**
     * Back key 처리
     * @return 처리 여부
     */
    public boolean onBackPressed() {
        return false;
    }
}
