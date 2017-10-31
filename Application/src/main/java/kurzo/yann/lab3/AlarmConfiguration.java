package kurzo.yann.lab3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Yann on 31/10/2017.
 */

public class AlarmConfiguration {

    // Tag for Logcat
    private static final String TAG = "AlarmConfiguration";

    private AlarmManager BirthdayAlarm;
    private Context context;

    ArrayList<Integer> keyList;

    public AlarmConfiguration(Context context){
        this.context = context;
        this.keyList = new ArrayList<>();

        BirthdayAlarm = (AlarmManager)context.getSystemService(MainActivity.ALARM_SERVICE);
    }

    public void setAlarm(String BDname, Calendar birthday, int key){
        //Create a new Pending Intent and add it to the Alarm Manager
        Intent BDintent = new Intent(context, OnAlarmReceiver.class);
        BDintent.putExtra("name", BDname);
        BDintent.putExtra("keyNotification", key);
        PendingIntent BDpi = PendingIntent.getBroadcast(context, key, BDintent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        //Set the alarm
        BirthdayAlarm.set(AlarmManager.RTC_WAKEUP, birthday.getTimeInMillis(), BDpi);
        keyList.add(key);

        Log.e(TAG, "Birthday alarm set for " + BDname + " with key " + key + "\n");
    }

    public void cancelAlarms(){
        // Cancel all the alarms
        if (keyList != null) {
            for(int i = 0; i < keyList.size(); i++) {
                Intent BDintent = new Intent(context, OnAlarmReceiver.class);
                PendingIntent BDpi = PendingIntent.getBroadcast(context, keyList.get(i),
                        BDintent, PendingIntent.FLAG_CANCEL_CURRENT);
                BirthdayAlarm.cancel(BDpi);
            }
        }

        // Clear the list
        keyList.clear();

        Log.e(TAG, "All alarms canceled\n");
    }
}
