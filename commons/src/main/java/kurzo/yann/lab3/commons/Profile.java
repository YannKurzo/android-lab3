package kurzo.yann.lab3.commons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Profile {
    private static final String TAG = "Profile";

    public static final String ID_NAME = "name";
    public static final String ID_NICKNAME = "nickname";
    public static final String ID_DESCRITPION = "description";
    public static final String ID_BIRTHDAY = "birthday";
    public static final String ID_PHOTO = "photo";

    public String name;
    public String nickname;
    public String description;
    public Calendar birthday;
    public BitmapDrawable photo;

    public Profile() {
        this.birthday = Calendar.getInstance();
    }

    public Profile(String name, String nickname, String description, Calendar birthday, Drawable photo) {
        this.name = name;
        this.nickname = nickname;
        this.description = description;
        this.birthday = birthday;
        this.photo = (BitmapDrawable) photo;
    }

    public Profile(DataMap map, Resources res, GoogleApiClient mGoogleApiClient) {
        // Construct instance from the datamap
        name = map.getString(ID_NAME);
        nickname = map.getString(ID_NICKNAME);
        description = map.getString(ID_DESCRITPION);
        birthday = Calendar.getInstance();
        birthday.setTimeInMillis(map.getLong(ID_BIRTHDAY));
        photo = new BitmapDrawable(res, loadBitmapFromAsset(map.getAsset(ID_PHOTO), mGoogleApiClient));
    }

    public DataMap toDataMap() {
        DataMap map = new DataMap();
        map.putString(ID_NAME, name);
        map.putString(ID_NICKNAME, nickname);
        map.putString(ID_DESCRITPION, description);
        map.putLong(ID_BIRTHDAY, birthday.getTimeInMillis());
        map.putAsset(ID_PHOTO, toAsset(photo.getBitmap()));
        return map;
    }

    private static Asset toAsset(Bitmap bitmap) {
        // Code inspired from https://developer.android.com/training/wearables/data-layer/assets.html
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private Bitmap loadBitmapFromAsset(Asset asset, GoogleApiClient mGoogleApiClient) {
        // Code from https://developer.android.com/training/wearables/data-layer/assets.html
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(1, TimeUnit.SECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }}
