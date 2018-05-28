package net.w2s.driverapp.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by RWS 6 on 3/23/2017.
 */

public class AlarmMonitorReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent1) {
        if (intent1.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 30);
            long mills = cal.getTimeInMillis();
            AlarmManager am = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
            Intent intent = new Intent(context, LogoutReceiver.class);
            intent.putExtra("data", mills);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, mills, AlarmManager.INTERVAL_DAY, pi);
        }
    }
}
