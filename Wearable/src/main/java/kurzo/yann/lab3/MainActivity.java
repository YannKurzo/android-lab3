package kurzo.yann.lab3;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    public static final String INTENT_IMAGE_DECODED = "INTENT_IMAGE_DECODED";
    public static final String INTENT_PROFILE_RECEIVED = "INTENT_PROFILE_RECEIVED";

    public static final String ID_PICTURE = "photo";
    public static final String ID_NAME = "name";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Register to receive messages from the service handling the Wear API connection
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named as INTENT_IMAGE_DECODED and INTENT_PROFILE_RECEIVED
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(INTENT_IMAGE_DECODED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mProfileReceiver,
                new IntentFilter(INTENT_PROFILE_RECEIVED));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is about to be suspended
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    // Our handler for received Intents with an action named "INTENT_IMAGE_DECODED"
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the image, display it and fade out
            Log.d(TAG, "Got message!");
            Bitmap image = intent.getParcelableExtra(ID_PICTURE);
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(image);
            AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setDuration(2000);
            animation.setFillAfter(true);
            imageView.startAnimation(animation);
        }
    };

    // Our handler for received Intents with an action named "INTENT_PROFILE_RECEIVED"
    private BroadcastReceiver mProfileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message!");

            // Get the data
            Bitmap image = intent.getParcelableExtra(ID_PICTURE);
            String name = intent.getStringExtra(ID_NAME);

            // Get the views
            ImageView imageView = findViewById(R.id.imageView);
            TextView textView = findViewById(R.id.textView);

            // Update the views
            imageView.clearAnimation();
            imageView.setImageBitmap(image);
            textView.setText(name);
        }
    };
}