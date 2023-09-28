package com.conpo.audiorac.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.conpo.audiorac.activity.AlarmActivity;

/**
 * Created by webquark on 2017-11-28.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, AlarmActivity.class);
        context.startActivity(intent1);
    }
}
