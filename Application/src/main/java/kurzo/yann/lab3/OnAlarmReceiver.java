package kurzo.yann.lab3;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Arrays;

/**
 * Created by Yann on 31/10/2017.
 */

public class OnAlarmReceiver extends BroadcastReceiver {

    // Tag for Logcat
    private static final String TAG = "OnAlarmReceiver";

    String BDname = "BDname";

    @Override
    public void onReceive(Context context, Intent intent) {
        BDname = intent.getStringExtra("name");

        sendNotification(context, "Birthday Notification",
                "Today is " + BDname + "'s birthday!",
                intent.getIntExtra("keyNotification", 0));
    }

    private void sendNotification(Context context, String title, String text, int id) {

        // Get preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean birthdayNotificationActivated = sharedPref.getBoolean("notifications_birthday", true);
        boolean birthadyToastActivated = sharedPref.getBoolean("toast_birthday", true);

        if(birthdayNotificationActivated) {

            // Get ringtone and vibrate preferences
            String strRingtone = sharedPref.getString("notifications_birthday_ringtone",
                    "content://settings/system/notification_sound");
            boolean vibrateActivated = sharedPref.getBoolean("notifications_birthday_vibrate", true);

            // Build notification
            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder mNotification = new NotificationCompat.Builder(context, "BirthdayNotificationChannel");

            mNotification.setContentTitle(title);
            mNotification.setContentText(text);
            mNotification.setSmallIcon(R.drawable.birthday_cake);

            if(vibrateActivated) {
                String vibrationMode = sharedPref.getString("notification_birthday_vibration_list", "1");
                long values[] = new long[2*Integer.parseInt(vibrationMode)];
                Arrays.fill(values, 1000);
                mNotification.setVibrate(values);
            }
            Uri uriSound = Uri.parse(strRingtone);
            mNotification.setSound(uriSound);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, 0);
            mNotification.setContentIntent(pendingIntent);
            mNotification.setAutoCancel(true);
            mNotificationManager.notify(id, mNotification.build());
        }
        if(birthadyToastActivated) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }
}
