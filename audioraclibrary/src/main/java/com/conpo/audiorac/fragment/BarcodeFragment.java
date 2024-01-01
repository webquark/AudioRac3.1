package com.conpo.audiorac.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.conpo.audiorac.application.AudioRacApplication;
import com.conpo.audiorac.library.R;
import com.conpo.audiorac.model.Record;
import com.conpo.audiorac.util.Utils;

/**
 * ISBN 바코드 스캔 액티비티
 */
public class BarcodeFragment extends FragmentBase
                    implements View.OnClickListener {

    private static final String LOG_TAG = "QR";

    private TextView mTvBarcode;
    private TextView mTvCourseName;

    private ImageButton mBtnBook;
    private ImageButton mBtnContent;

    private String mBarcodeType;    // 바코드 타입 (0:도서, 1:비도서)

    private OnISBNSearchListener mOnISBNSearchListener;

    public interface OnISBNSearchListener {
        public void showCourse(String csCode);
        public void scanBarcode(String type);
    }

    public void setOnISBNSearchListener(OnISBNSearchListener listener) {
        mOnISBNSearchListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_barcode, container, false);

        if (mContext == null) {
            mContext = getContext();
        }

        Log.d(LOG_TAG, "BarcodeView1");

        mTvBarcode = mView.findViewById(R.id.tv_barcode);
        mTvCourseName = mView.findViewById(R.id.tv_cs_name);

        mBtnBook = mView.findViewById(R.id.btn_book);
        mBtnContent = mView.findViewById(R.id.btn_content);

        mBtnBook.setOnClickListener(this);
        mBtnContent.setOnClickListener(this);

        Log.d(LOG_TAG, "BarcodeView2");

        return mView;
    }

    public ActivityResultLauncher<Intent> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        mMainActivity.setNavigationItem(R.id.navigation_barcode);

                    } else {
                        mMainActivity.setNavigationTab(0);
                        Alert(R.string.msg_permission_nenied);
                    }
                }
            });

    public ActivityResultLauncher<Intent> getPermissionLauncher() {
        return permissionLauncher;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_book || id == R.id.btn_content) {
            if (!Utils.checkNetworkState(mContext)) {
                showToast(R.string.msg_qr_network_err);
                return;
            }

            mBarcodeType = v.getTag().toString();
            if (mOnISBNSearchListener != null) {
                mOnISBNSearchListener.scanBarcode(mBarcodeType);
            }
        }
    }

    public void setBarcode(String barcode) {
        mTvBarcode.setText(barcode);

        new SearchISBNCodeTask().execute(barcode);
    }

    /**
     * 서버에서 ISBN 코드 조회하기
     */
    private class SearchISBNCodeTask extends AsyncTask<String, Void, Record> {

        protected Record doInBackground(String... params) {
            String barcode = params[0];

            return AudioRacApplication.searchCourseByBarcode(barcode, mBarcodeType);
        }

        protected void onPostExecute(final Record result) {
            if (result == null) {
                Alert(R.string.msg_qr_not_ready);

            } else if (result.isComplete()) {
                mTvCourseName.setText(result.safeGet("cs_name"));

                if (mOnISBNSearchListener != null) {
                    Confirm(R.string.app_name, R.string.msg_qr_book_found,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    mOnISBNSearchListener.showCourse(result.safeGet("cs_code"));
                                }
                            });
                }

            } else {
                Alert(R.string.msg_qr_book_not_found);
            }

        }
    }

}
