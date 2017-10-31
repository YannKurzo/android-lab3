package kurzo.yann.lab3;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import kurzo.yann.lab3.commons.Profile;

/**
 * Created by Yann on 25/10/2017.
 */

public class ProfileFormFragment extends Fragment {

    // Tag for Logcat
    private static final String TAG = "ProfileFormFragment";

    // Image constant
    private static final int PICK_IMAGE = 1;
    private static final int VALIDATE_IMAGE = 2;

    private static final String KEY_IMAGE_URI = "imageUri";

    private View fragmentView;

    private Date userBirthday;
    private ImageView userImage;
    private static Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_form_layout, container, false);

        // Get views
        userImage = fragmentView.findViewById(R.id.userImage);

        // Restore saved instances
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI);

            // Set saved settings
            if (imageUri != null) {
                userImage.setImageURI(imageUri);
            }
            else {
                final Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.monch);
                userImage.setImageBitmap(image);
            }
        }

        // Otherwise load default settings
        else {
            final Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.monch);
            userImage.setImageBitmap(image);
        }

        // Birthday button
        createBirthdayButton();

        return fragmentView;
    }

    private void createBirthdayButton() {
        final Button buttonBirthday = fragmentView.findViewById(R.id.buttonBirthday);

        // Default value is today
        userBirthday = Calendar.getInstance().getTime();
        java.text.DateFormat dateFormat = DateFormat.getDateFormat(getActivity());
        buttonBirthday.setText(dateFormat.format(userBirthday));

        // Update button text
        final DatePickerDialog.OnDateSetListener mDateSetListener = new
                DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        // Format the result as a Date object
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);
                        userBirthday = calendar.getTime();
                        // Format the date as a string according to the user's locale settings
                        java.text.DateFormat dateFormat = DateFormat.getDateFormat(getActivity());
                        // Display the time
                        buttonBirthday.setText(dateFormat.format(userBirthday));
                    }
                };

        // Open date chooser
        buttonBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the java.util.Calendar class and NOT android.icu.util.calendar!!!
                Calendar cal = Calendar.getInstance();
                // The calendar is created with today's date, so we create the date
                // picker with this date, along with a reference to the defined listener
                // and the theme we want for the picker:
                DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                        R.style.Theme_AppCompat_Light_Dialog,
                        mDateSetListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_IMAGE_URI, imageUri);
    }

    // Open a dialog to choose an image
    public void onClickChooseImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent receivedIntent) {
        super.onActivityResult(requestCode, resultCode, receivedIntent);

        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(getActivity(), ValidationActivity.class);
                    intent.putExtra(ValidationActivity.ID_IMAGE_URI, receivedIntent.getData());
                    startActivityForResult(intent, VALIDATE_IMAGE);
                    imageUri = receivedIntent.getData();
                }
                break;
            case VALIDATE_IMAGE:
                if(resultCode == Activity.RESULT_OK) {
                    userImage.setImageURI(imageUri);
                }
                else {
                    imageUri = null;
                }
                break;
        }
    }

    public Profile getProfile() {

        Profile profile = new Profile(
                ((EditText) fragmentView.findViewById(R.id.userName)).getText().toString(),
                ((EditText) fragmentView.findViewById(R.id.userNickname)).getText().toString(),
                ((EditText) fragmentView.findViewById(R.id.userDescription)).getText().toString(),
                userBirthday,
                ((ImageView) fragmentView.findViewById(R.id.userImage)).getDrawable()
        );

        if(profile.name.isEmpty() || profile.nickname.isEmpty()) {
            Toast.makeText(getActivity(), "Complete \"name and \"nickname fields!",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        else {
            return profile;
        }
    }
}
