package kurzo.yann.lab3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ValidationActivity extends AppCompatActivity {

    public static final String ID_IMAGE_URI = "imageUri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation);

        Intent intent = getIntent();

        if(intent != null) {
            final Uri imageUri = intent.getParcelableExtra(ID_IMAGE_URI);
            final InputStream imageStream;

            try {
                imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                imageView.setImageBitmap(selectedImage);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void onClickValidate(View view) {
        setResult(Activity.RESULT_OK, null);
        finish();
    }
    public void onClickDiscard(View view) {
        setResult(Activity.RESULT_CANCELED, null);
        finish();
    }
}
