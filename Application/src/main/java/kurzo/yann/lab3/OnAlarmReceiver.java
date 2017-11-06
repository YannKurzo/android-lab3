package kurzo.yann.lab3;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

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
        Toast.makeText(context, "Today is " + BDname + "'s birthday !",
                Toast.LENGTH_LONG).show();

        sendNotification(context, "Birthday Notification",
                "Today is " + BDname + "'s birthday!",
                intent.getIntExtra("keyNotification", 0));
    }

    private void sendNotification(Context context, String title, String text, int id) {
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mNotification = new NotificationCompat.Builder(context);

        mNotification.setContentTitle(title);
        mNotification.setContentText(text);
        mNotification.setSmallIcon(R.drawable.birthday_cake);
        mNotification.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.birthday_cake));

        mNotification.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        Uri uriSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mNotification.setSound(uriSound);

        Intent intent = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent,0);
        mNotification.setContentIntent(pendingIntent);
        mNotification.setAutoCancel(true);
        mNotificationManager.notify(id, mNotification.build());

        int idNotification = intent.getIntExtra("keyNotification",0);
        sendNotification(context,"Birthday Notification","Today is " + BDname + "'s birthday !", idNotification);
    }
}
