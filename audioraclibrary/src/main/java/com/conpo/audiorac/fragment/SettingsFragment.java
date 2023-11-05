package com.conpo.audiorac.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatDelegate;

import com.conpo.audiorac.activity.AlarmActivity;
import com.conpo.audiorac.activity.LoginActivity;
import com.conpo.audiorac.adapter.SpinnerAdapter;
import com.conpo.audiorac.application.LoginInfo;
import com.conpo.audiorac.library.R;
import com.conpo.audiorac.model.Record;
import com.conpo.audiorac.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * 앱 설정 액티비티
 */
public class SettingsFragment extends FragmentBase
        implements View.OnClickListener,
                    CompoundButton.OnCheckedChangeListener,
                    TimePicker.OnTimeChangedListener {
    private final String LOG_TAG = "Settings";

    private int mHour;
    private int mMinute;

    private AlarmManager mAlarmManager;
    private CheckBox mChkBedtime;
    private Button mBtnSaveBedtime;
    private TimePicker mTimePicker;
    private boolean mBedtimeChanged = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_settings, container, false);

        if (mContext == null) {
            mContext = getContext();
        }

        initializeThemeMode();
        initializeBedTimeUI();

        TextView tvUserId = mView.findViewById(R.id.tv_userid);
        TextView tvSiteName = mView.findViewById(R.id.tv_site_name);
        TextView tvVersion = mView.findViewById(R.id.tv_version);

        tvUserId.setText(LoginInfo.getUserId());
        tvSiteName.setText(LoginInfo.getSiteName());
        tvVersion.setText( getString(R.string.text_current_version, Utils.getAppVersion(mContext)) );

        ((Button)mView.findViewById(R.id.btn_logout)).setOnClickListener(this);

        return mView;
    }

    /**
     * 테마 모드 선택
     */
    private void initializeThemeMode() {
        RadioButton mRbLightMode = mView.findViewById(R.id.rb_light_mode);
        RadioButton mRbDarkMode = mView.findViewById(R.id.rb_dark_mode);

        mRbLightMode.setOnClickListener(this);
        mRbDarkMode.setOnClickListener(this);

        Log.d(LOG_TAG, "MODE: " + LoginInfo.getUIMode());
        if (LoginInfo.getUIMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            mRbLightMode.setChecked(true);
        } else {
            mRbDarkMode.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_logout) {
            goLogout();

        } else if (id == R.id.rb_light_mode) {
            if (LoginInfo.getUIMode() != AppCompatDelegate.MODE_NIGHT_NO) {    // Light
                LoginInfo.setUIMode(AppCompatDelegate.MODE_NIGHT_NO);
                LoginInfo.savePreferences(mContext);

                mMainActivity.restart();
            }

        } else if (id == R.id.rb_dark_mode) {
            if (LoginInfo.getUIMode() != AppCompatDelegate.MODE_NIGHT_YES) { // Dark
                LoginInfo.setUIMode(AppCompatDelegate.MODE_NIGHT_YES);
                LoginInfo.savePreferences(mContext);

                mMainActivity.restart();
            }

        } else if (id == R.id.btn_save_bedtime) {
            saveBedtime(true);
        }
    }

    private void initializeBedTimeUI() {
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        cancelAlarm();

        mChkBedtime = mView.findViewById(R.id.chk_bedtime);
        mBtnSaveBedtime = mView.findViewById(R.id.btn_save_bedtime);
        mTimePicker = mView.findViewById(R.id.timePicker);

        mChkBedtime.setOnCheckedChangeListener(this);
        mBtnSaveBedtime.setOnClickListener(this);
        mTimePicker.setOnTimeChangedListener(this);

        if (LoginInfo.isUseAlarm()) {
            mHour = LoginInfo.getAlarmHour();
            mMinute = LoginInfo.getAlarmMinute();

            mTimePicker.setHour(mHour);
            mTimePicker.setMinute(mMinute);

            mChkBedtime.setChecked(true);
            mTimePicker.setEnabled(true);

        } else {
            mTimePicker.setHour(mTimePicker.getHour() + 1);

            mChkBedtime.setChecked(false);
            mTimePicker.setEnabled(false);
        }
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        mBedtimeChanged = true;

        mHour = hourOfDay;
        mMinute = minute;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();

        if (id == R.id.chk_bedtime) {
            mBedtimeChanged = true;

            mBtnSaveBedtime.setEnabled(isChecked);
            mTimePicker.setEnabled(isChecked);
        }
    }

    /**
     * 로그아웃
     */
    private void goLogout() {
        this.Confirm(R.string.app_name,
                R.string.msg_settings_logout,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mMainActivity.stopWebAudioPlayer();

                        LoginInfo.logout(mContext);

                        Intent intent = new Intent(mContext, LoginActivity.class);
                        startActivity(intent);

                        mMainActivity.finish();
                    }
                });
    }

    public void setAlarm(Context context) {

        Intent intent = new Intent(context, AlarmActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, mHour);
        calendar.set(Calendar.MINUTE, mMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long now = Calendar.getInstance().getTimeInMillis();	// System.currentTimeMillis();  // current Time
        long alarmTime = calendar.getTimeInMillis();

        Log.d("TIME", "Now:" + now);
        Log.d("TIME", "Alarm:" + alarmTime);

        if (alarmTime < now) {
            // 현재시간 이전 시간은 24시간 뒤로
            calendar.add(Calendar.DATE, 1);  // number of days to add
            alarmTime = calendar.getTimeInMillis();
        }

        mAlarmManager.set(AlarmManager.RTC, alarmTime, pendingIntent);
        mBedtimeChanged = false;
    }

    public void cancelAlarm() {
        if (mContext == null) return;

        Intent intent = new Intent(mContext, AlarmActivity.class);
        //PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        mAlarmManager.cancel(pIntent);
    }

    /**
     * 설정화면에서 다른 화면으로 전환될 때의 이벤트 처리
     */
    public void onBlur() {
        if (mChkBedtime.isChecked() && mBedtimeChanged) {
            this.Confirm(R.string.app_name,
                    R.string.msg_settings_bedtime_save,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            saveBedtime(true);
                            dialog.dismiss();
                        }
                    });

        } else {
            saveBedtime(false);
        }
    }

    private void saveBedtime(boolean save) {
        if (save) {
            setAlarm(mContext);
            LoginInfo.setAlarm(mHour, mMinute);
            showToast(R.string.msg_settings_bedtime_saved);

        } else {
            LoginInfo.setAlarm(0, 0, false);
        }

        LoginInfo.savePreferences(mContext);
    }


}
