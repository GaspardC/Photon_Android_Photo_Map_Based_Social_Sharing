package ch.epfl.tabletlab.photon;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ch.epfl.tabletlab.photon.MenuFragments.DataManager;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class PostActivity extends Activity {
  // UI references.
  private EditText postEditText;
  private TextView characterCountTextView;
  private Button postButton;

    Bitmap bmp;
    Intent i;
    Uri BmpFileName = null;

  private int maxCharacterCount = PhotonApplication.getConfigHelper().getPostMaxCharacterCount();
  private ParseGeoPoint geoPoint;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_post);
      launchCamera();

//    Intent intent = getIntent();
//    Location location = intent.getParcelableExtra(PhotonApplication.INTENT_EXTRA_LOCATION);
    geoPoint = DataManager.getUserLocation();
//    geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

    postEditText = (EditText) findViewById(R.id.post_edittext);
    postEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }

      @Override
      public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        updatePostButtonState();
        updateCharacterCountTextViewText();
      }
    });

    characterCountTextView = (TextView) findViewById(R.id.character_count_textview);

    postButton = (Button) findViewById(R.id.post_button);
    postButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        post();
      }
    });

    updatePostButtonState();
    updateCharacterCountTextViewText();
  }

  private void post () {
    String text = postEditText.getText().toString().trim();

    // Set up a progress dialog
    final ProgressDialog dialog = new ProgressDialog(PostActivity.this);
    dialog.setMessage(getString(R.string.progress_post));
    dialog.show();

    // Create a post.
    AnywallPost post = new AnywallPost();

    // Set the location to the current user's location
      post.setLocation(geoPoint);
      post.setText(text);
      post.setUser(ParseUser.getCurrentUser());

      //USE TO GET PHoTO FROM DRAWABLE
/*    Resources res = getResources();
    Drawable drawable = res.getDrawable(R.drawable.smallbeautifulimage);
    Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    byte[] bitmapdata = stream.toByteArray();
      ParseFile pFile = new ParseFile("DocImage.jpg", bitmapdata);*/


      // Ensure bmp has value
      if (bmp == null || BmpFileName == null) {
          Log.d ("Error" , "Problem with image");
          return;
      }

      //rotate image if need : some constructors have rotated the image when it is taken by the front cam
      rotateImage();

      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      bmp.compress(Bitmap.CompressFormat.PNG, 10, stream);
      ParseFile pFile = new ParseFile("Photo.jpg", stream.toByteArray());


    post.put("image",pFile);

    ParseACL acl = new ParseACL();

    // Give public read access
    acl.setPublicReadAccess(true);
    post.setACL(acl);

    // Save the post
    post.saveInBackground(new SaveCallback() {
      @Override
      public void done(ParseException e) {
        dialog.dismiss();
        finish();
      }
    });
  }

    private void rotateImage() {


        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        String fileName = "/storage/emulated/0/dir/photo";
        BitmapFactory.decodeFile(fileName, bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(fileName, opts);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        bmp = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
    }

    private void launchCamera() {

        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {

//            String path = Environment.getExternalStorageDirectory().getName() + File.separatorChar + "Android/data/" + this.getPackageName() + "/files/" + "Doc1" + ".jpg";
//            File photoFile = new File(path);

            File photoFile = new File(Environment.getExternalStorageDirectory(), "dir/photo");
            try {
                if (photoFile.exists() == false) {
                    photoFile.getParentFile().mkdir();
                    photoFile.createNewFile();
                }
            } catch (IOException e) {
                Log.e("DocumentActivity", "Could not create file.", e);
            }
//            Log.i("DocumentActivity", path);
            BmpFileName = Uri.fromFile(photoFile);
            i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, BmpFileName);
            startActivityForResult(i, 0);
        }
    }

    private String getPostEditTextText () {
    return postEditText.getText().toString().trim();
  }

  private void updatePostButtonState () {
    int length = getPostEditTextText().length();
    boolean enabled = length > 0 && length < maxCharacterCount;
    postButton.setEnabled(enabled);
  }

  private void updateCharacterCountTextViewText () {
    String characterCountString = String.format("%d/%d", postEditText.length(), maxCharacterCount);
    characterCountTextView.setText(characterCountString);
  }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
// TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                bmp = MediaStore.Images.Media.getBitmap( this.getContentResolver(), BmpFileName);
            } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {

// TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (bmp != null){

            }
        }
    }
}
