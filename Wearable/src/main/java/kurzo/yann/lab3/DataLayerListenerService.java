package kurzo.yann.lab3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import kurzo.yann.lab3.commons.DataLayerCommons;
import kurzo.yann.lab3.commons.Profile;

import static kurzo.yann.lab3.MainActivity.*;

public class DataLayerListenerService extends WearableListenerService {

    // Tag for Logcat
    private static final String TAG = "DataLayerService";

    // Member for the Wear API handle
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // Start the Wear API connection
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v(TAG, "onDataChanged: " + dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.v(TAG, "DataItem Changed: " + event.getDataItem().toString() + "\n"
                        + DataMapItem.fromDataItem(event.getDataItem()).getDataMap());

                String path = event.getDataItem().getUri().getPath();
                switch (path) {
                    case DataLayerCommons.IMAGE_PATH:
                        Log.v(TAG, "Data Changed for IMAGE_PATH: " + event.getDataItem().toString());
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        Asset photoAsset = dataMapItem.getDataMap().getAsset(DataLayerCommons.IMAGE_KEY);
                        Bitmap imageDecoded = bitmapFromAsset(photoAsset);
                        Log.v(TAG, "Broadcasting message to activity that image is ready");
                        Intent intent = new Intent(INTENT_IMAGE_DECODED);
                        intent.putExtra(ID_PICTURE, imageDecoded);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        break;

                    case DataLayerCommons.COUNT_PATH:
                        Log.v(TAG, "Data Changed for COUNT_PATH: " + event.getDataItem() + "\n"
                                + "Count data = " + DataMapItem.fromDataItem(event.getDataItem())
                                .getDataMap().getInt(DataLayerCommons.COUNT_KEY));
                        break;

                    case DataLayerCommons.PROFILE_PATH:
                        Log.v(TAG, "Data Changed for PROFILE_PATH: " + event.getDataItem());

                        // Get profile
                        DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem())
                                .getDataMap().getDataMap(DataLayerCommons.PROFILE_KEY);
                        Profile profile = new Profile(dataMap, getResources(), mGoogleApiClient);

                        // Intent to send profile to display
                        Log.v(TAG, "Broadcasting message to activity that profile is received");
                        intent = new Intent(INTENT_PROFILE_RECEIVED);
                        intent.putExtra(ID_PICTURE, profile.photo.getBitmap());
                        intent.putExtra(ID_NAME, profile.name);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        break;

                    default:
                        Log.v(TAG, "Data Changed for unrecognized path: " + path);
                        break;
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v(TAG, "DataItem Deleted: " + event.getDataItem().toString());
            }

            // For demo, send a message back to the node that created the data item
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            if (path.equals(DataLayerCommons.COUNT_PATH)) {
                String nodeId = uri.getHost();
                byte[] payload = uri.toString().getBytes();
                Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId,
                        DataLayerCommons.DATA_ITEM_RECEIVED_PATH, payload);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // A message has been received from the Wear API
        Log.v(TAG, "onMessageReceived: " + messageEvent);

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(DataLayerCommons.START_ACTIVITY_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }

    public Bitmap bitmapFromAsset(Asset asset) {
        // Reads an asset from the Wear API and parse it as an image
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result = mGoogleApiClient.blockingConnect(10, TimeUnit.SECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // Convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }

        // Decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}