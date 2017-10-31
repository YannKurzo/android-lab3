package kurzo.yann.lab3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

import kurzo.yann.lab3.commons.Profile;

import static kurzo.yann.lab3.commons.Profile.*;

/**
 * Class to handle a Firebase database. It implements ValueEventListener in order
 * to easily override the onDataChange function.
 * Created by Yann on 30/10/2017.
 */

public class MyFirebase implements ValueEventListener {
    // Tag for Logcat
    private static final String TAG = "MyFirebase";

    // Profile database group
    private static final String DATABASE_PROFILES = "profiles";
    // Image database group
    private static final String DATABASE_IMAGES = "images";

    // Photos storage directory
    private static final String STORAGE_PHOTOS_DIR = "profilePhotos";

    // Photo name
    private static final String PHOTO_PREFIX = "photo";
    private static final String PHOTO_EXTENSION = ".jpg";

    // Reference to the list of profiles view
    private ProfileListFragment mProfileListFragment;

    // Constructor
    public MyFirebase(ProfileListFragment profileListFragment) {
        this.mProfileListFragment = profileListFragment;
    }

    // Load profiles when there is a change on the database
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        // Clear profile list before reloading
        mProfileListFragment.clearProfileList();
        mProfileListFragment.mAlarmConfig.cancelAlarms();
        int key = 123;

        // For each profile
        for (final DataSnapshot profileData : dataSnapshot.getChildren()) {
            final Profile profile = new Profile();
            if (mProfileListFragment.isAdded()) {

                // Get values
                profile.name = profileData.child(ID_NAME).getValue(String.class);
                profile.nickname = profileData.child(ID_NICKNAME).getValue(String.class);
                profile.description = profileData.child(ID_DESCRITPION).getValue(String.class);
                profile.birthday = new Date(profileData.child(ID_BIRTHDAY).getValue(Long.class));

                // Set alarm
                Calendar birthday = Calendar.getInstance();
                birthday.setTime(profile.birthday);
                mProfileListFragment.mAlarmConfig.setAlarm(profile.name, birthday, key);
                key++;

                // Get photo on Firebase storage (photo url stored in online profile)
                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(profileData.child(ID_PHOTO).getValue(String.class));

                // Get the photo byte by byte
                storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        // When the photo is loaded, add it to the profile and update list
                        if (mProfileListFragment.isAdded()) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            profile.photo = new BitmapDrawable(mProfileListFragment.getResources(), bitmap);
                            mProfileListFragment.addProfile(profile);
                        }
                    }
                });
            }
        }

    }

    // Display error when data load is canceled
    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.v(TAG, databaseError.toString());
    }

    // Add listener on profile changes
    public void addValueEventListener() {
        FirebaseDatabase.getInstance().getReference().child(DATABASE_PROFILES).
                addValueEventListener(this);
    }

    // Remove listener on profile changes
    public void removeValueEventListener() {
        FirebaseDatabase.getInstance().getReference().child(DATABASE_PROFILES).
                removeEventListener(this);
    }

    // Add a new profile to the database
    // Takes a function reference which is called when the profile is completely uploaded
    public static void addProfileToFirebase(final Profile profile, final Runnable onComplete) {

        // Create a reference to the location, add a new profile, and get a unique key
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileRef = database.getReference(DATABASE_PROFILES).push();
        final String profileId = profileRef.getKey();

        // Create a storage reference from our app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference photoRef = storageRef.child(STORAGE_PHOTOS_DIR)
                .child(PHOTO_PREFIX + profileId + PHOTO_EXTENSION);

        // Convert the image to bytes
        Bitmap bitmap = profile.photo.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        // Create a task which manages the upload of the picture
        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.v(TAG, "Fail to load the picture");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                // Upload complete profile after the photo is correctly uploaded
                profileRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {

                        // Profile parameters
                        mutableData.child(ID_NAME).setValue(profile.name);
                        mutableData.child(ID_NICKNAME).setValue(profile.nickname);
                        mutableData.child(ID_DESCRITPION).setValue(profile.description);
                        mutableData.child(ID_BIRTHDAY).setValue(profile.birthday.getTime());

                        // Photo URL parameter
                        //noinspection VisibleForTests
                        Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        mutableData.child(ID_PHOTO).setValue(downloadUrl.toString());

                        // Photo key (used for deleting photos)
                        String keyImage = database.getReference(DATABASE_IMAGES + "/").push().getKey();
                        DatabaseReference imageNameRef = database.getReference(DATABASE_IMAGES + "/" + keyImage);
                        imageNameRef.setValue(STORAGE_PHOTOS_DIR + "/" + PHOTO_PREFIX + profileId + PHOTO_EXTENSION);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        // Run function parameter
                        onComplete.run();
                    }
                });
            }
        });
    }

    // Clear all profiles from database
    static public void clearProfilesFromFirebase() {
        // Clear all profiles
        FirebaseDatabase.getInstance().getReference(DATABASE_PROFILES).removeValue();
    }

    // Clear all images from storage
    static public void clearImagesFromStorage() {

        // Create a reference to the database and get a image keys
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference profilesRef = database.getReference().child(DATABASE_IMAGES + "/");

        //
        profilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // For each image
                for(final DataSnapshot imageKeyData : dataSnapshot.getChildren()) {
                    // Get the key
                    String imageName = imageKeyData.getValue(String.class);

                    // Get storage reference and delete corresponding image
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    (storageRef.child(imageName)).delete().addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Remove image key
                                imageKeyData.getRef().removeValue();

                                Log.d(TAG, "Image successfully deleted!");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Failed deleting image");
                            }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, databaseError.toString());
            }
        });
    }
}