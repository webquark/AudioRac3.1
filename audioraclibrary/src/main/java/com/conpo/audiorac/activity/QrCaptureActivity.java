package com.conpo.audiorac.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.client.android.CaptureActivity;

/**
 * <h2>QR코드 스캔</h2>
 * <p>이 기능을 사용하려면 AndroidManifest에 CAMERA 및 FLASHLIGHT permission이 추가되어 있어야 함</p>  
 * @author hansolo
 *
 */
public class QrCaptureActivity extends ActivityBase {
	private int REQUEST_QRCODE = 1000;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		/*
		 * ZXing Library의 QR코드 캡처 액티비티 실행하기
		 */
		Intent intent = new Intent(this, CaptureActivity.class);
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, REQUEST_QRCODE);

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK) {
			// QR코드/바코드를 스캔한 결과 값을 가져옵니다.
			String barcode = data.getStringExtra("SCAN_RESULT");
			Intent intent = new Intent();
			intent.putExtra("BARCODE", barcode);

			setResult(Activity.RESULT_OK, intent);
		}

		finish();
	}


}
