package kurzo.yann.lab3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
    }
}
