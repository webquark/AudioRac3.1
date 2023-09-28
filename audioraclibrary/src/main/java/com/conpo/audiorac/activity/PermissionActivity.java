package com.conpo.audiorac.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.conpo.audiorac.library.R;


public class PermissionActivity extends ActivityBase {
    private static final String LOG_TAG = "Permission";

    public static final int PERMISSION_REQUEST_READ_STORAGE = 1001;
    public static final int PERMISSION_REQUEST_CAMERA = 2001;
    public static final int PERMISSION_READ_MEDIA_AUDIO = 3001;

    private static Context mStaticContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStaticContext = this;

        setContentView(R.layout.activity_permission);

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAppPermission();
            }
        });
    }

    private void returnResult(int resultCode) {
        setResult(resultCode);
        finish();
    }

    public void requestAppPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!checkREAD_MEDIA_AUDIOPermission(mContext)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                            PERMISSION_READ_MEDIA_AUDIO);
                } else if (!checkCAMERAPermission(mContext)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                } else {
                    returnResult(RESULT_OK);
                }

            } else {
                if (!checkREAD_STORAGEPermission(mContext)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_READ_STORAGE);
                } else if (!checkCAMERAPermission(mContext)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                } else {
                    returnResult(RESULT_OK);
                }
            }

        } else {
            returnResult(RESULT_OK);
        }
    }

    public static boolean checkREAD_MEDIA_AUDIOPermission(Context context) {
        int result = context.checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkREAD_STORAGEPermission(Context context) {
        int result = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkWRITE_STORAGEPermission(Context context) {
        int result = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkCAMERAPermission(Context context) {
        int result = context.checkSelfPermission(Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkFINE_LOCATIONPermission(Context context) {
        int result = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * 퍼미션 요청결과 처리
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_READ_MEDIA_AUDIO ||
            requestCode == PERMISSION_REQUEST_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!checkCAMERAPermission(mContext)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }

            } else {
                returnResult(RESULT_CANCELED);
            }

        } else if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                returnResult(RESULT_OK);

            } else {
                returnResult(RESULT_CANCELED);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
