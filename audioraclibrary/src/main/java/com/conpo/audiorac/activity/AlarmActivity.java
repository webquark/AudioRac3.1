package com.conpo.audiorac.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.conpo.audiorac.library.R;
import com.conpo.audiorac.application.LoginInfo;

public class AlarmActivity extends ActivityBase
                    implements View.OnClickListener {

    private CountDownTimer mTimer;

    private TextView mTvCountdown;

    private Button mBtnCancel;
    private Button mBtnOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        mTvCountdown = (TextView) findViewById(R.id.tv_countdown);

        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnOK = (Button) findViewById(R.id.btn_ok);

        mBtnCancel.setOnClickListener(this);
        mBtnOK.setOnClickListener(this);

        mTimer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                mTvCountdown.setText( String.format("%02d", minutes) + ":" + String.format("%02d", seconds) );
            }

            public void onFinish() {
                MainActivity mainActivity = LoginInfo.getMainActivity();
                mainActivity.stopStreaming();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", "OK");
                startActivity(intent);
            }

        }.start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_ok) {
            LoginInfo.setAlarm(0, 0, false);
            LoginInfo.savePreferences(AlarmActivity.this);

            mTimer.onFinish();

        } else if (id == R.id.btn_cancel) {
            LoginInfo.setAlarm(0, 0, false);
            LoginInfo.savePreferences(AlarmActivity.this);

            mTimer.cancel();
            this.finish();
        }
    }
}
