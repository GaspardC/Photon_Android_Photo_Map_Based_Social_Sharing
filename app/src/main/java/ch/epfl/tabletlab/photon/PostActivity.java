package ch.epfl.tabletlab.photon;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.epfl.tabletlab.photon.MenuFragments.DataManager;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class PostActivity extends Activity {
    private static final int REQUEST_TAKE_PHOTO = 0;

    //THE SIZE MAX we use for photo (limitation of Out Of bond memory)
    // The new size we want to scale to
    private static final int REQUIRED_SIZE = 200;

    // UI references.
    private EditText postEditText;
    private TextView characterCountTextView;
    private Button postButton;
    private TextView seekBarValueExpiration;
    private SeekBar seekBarNumberExperiation;
    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";


    Bitmap bmp;
    Intent i;
    Uri BmpFileName = null;

    private int maxCharacterCount = PhotonApplication.getConfigHelper().getPostMaxCharacterCount();
    private ParseGeoPoint geoPoint;
    private ParseUser currentUser;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String fileName;
    private PhotonPost post;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post);
        verifyStoragePermissions(this);
        try {
            launchCamera();
        } catch (IOException e) {
            e.printStackTrace();
        }

        geoPoint = DataManager.getUserLocation();
        setTextEdit();


        characterCountTextView = (TextView) findViewById(R.id.character_count_textview);

        postButton = (Button) findViewById(R.id.post_button);
        postButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                post();
            }
        });

        updatePostButtonState();
        updateCharacterCountTextViewText();
        setSeekBar();
    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void setTextEdit() {

        postEditText = (EditText) findViewById(R.id.post_edittext);
        postEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //TODO change the color if hashtag detected
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePostButtonState();
                updateCharacterCountTextViewText();
            }
        });
    }

    private void post() {
        String text = postEditText.getText().toString().trim();

        // Set up a progress dialog
        final ProgressDialog dialog = new ProgressDialog(PostActivity.this);
        dialog.setMessage(getString(R.string.progress_post));
        dialog.show();

        // Create a post.
        post = new PhotonPost();
        String hashtags = findHashtags(text);
        post.setHashtags(hashtags);

//        String textPost = "#" + text.replaceAll(" ", " #").toLowerCase();


        // Set the location to the current user's location
        post.setLocation(geoPoint);
        post.setText(text);
        post.setUser(ParseUser.getCurrentUser());
        post.setLikes(0);
        post.setAuthor(DataManager.getUser().getString("name"));


        //
        Date d = new Date();

        int time = (PhotonApplication.HOUR_TO_KEEP_PHOTO_DISPLAYED * 3600 * 1000); // dans 24h  par ex
        Date expirationDate = new Date(d.getTime() + (time));
        post.put("expirationDate", expirationDate);

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
            Log.d("Error", "Problem with image");
            return;
        }



        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        text = text.replaceAll(" ", "_").toLowerCase();
        text = text.replaceAll("#", "_").toLowerCase();
        text = text.replaceAll("!", "");
        text = text.replaceAll("é", "");

        ParseFile pFile = new ParseFile(text + ".jpg", stream.toByteArray());
        post.put("image", pFile);
        dialog.dismiss();


        pFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e != null) {
                    Toast.makeText(PostActivity.this, "there was a problem" + e.toString(), Toast.LENGTH_LONG).show();
                    return;
                }

                ParseACL acl = new ParseACL();
                // Give public read access
                acl.setPublicReadAccess(true);
                acl.setPublicWriteAccess(true);
                post.setACL(acl);

                // Save the post
                post.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(PostActivity.this, "there was a problem" + e.toString(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Photo Uploaded",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

            }
        });


        /*post.put("image", pFile);

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
        });*/
    }

    private String findHashtags(String text) {
        String hashtags = "";
        String[] s = text.split("#");
        int i = 0;
        for (String value : s) {
            i++;
            if(i!=1){
                String[] s2 = value.split(" ");
                if(!s2[0].equals(null)){
                    hashtags = hashtags + " #" +(s2[0]);
                }
            }
        }
        return hashtags;
    }

    private Bitmap rotateAndRedimensionningOfTheImage() {

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, bounds);




        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while(bounds.outWidth / scale / 2 >= REQUIRED_SIZE &&
                bounds.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }



        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        Bitmap bm = BitmapFactory.decodeFile(fileName, opts);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
//        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        return Bitmap.createBitmap(bm, 0, 0, opts.outWidth, opts.outHeight, matrix, true);
    }

    private void launchCamera() throws IOException {

        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {

             photoFile = createImageFile();
//            Log.i("DocumentActivity", path);
            BmpFileName = Uri.fromFile(photoFile);
/*            i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, BmpFileName);
            super.onResume();
            startActivityForResult(i, 0);*/



            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
// Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }

        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
         fileName = image.getAbsolutePath();

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private String getPostEditTextText() {
        return postEditText.getText().toString().trim();
    }

    private void updatePostButtonState() {
        int length = getPostEditTextText().length();
        boolean enabled = length > 0 && length < maxCharacterCount;
        postButton.setEnabled(enabled);
    }

    private void updateCharacterCountTextViewText() {
        String characterCountString = String.format("%d/%d", postEditText.length(), maxCharacterCount);
        characterCountTextView.setText(characterCountString);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
// TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            //rotate image just if needed : some constructors have rotated the image when it is taken by the front cam
            // and dimensions max of 200
            bmp =   rotateAndRedimensionningOfTheImage();
            if (bmp != null) {
            Toast.makeText(getApplicationContext(),"problem with the image",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setSeekBar() {
        seekBarNumberExperiation = (SeekBar) findViewById(R.id.seekBarExpirationDate);
        seekBarValueExpiration = (TextView) findViewById(R.id.seekBarValueExpirationDate);
        seekBarNumberExperiation.getThumb().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);

        currentUser = DataManager.getUser();
        if (currentUser == null) return;
        seekBarNumberExperiation.setProgress(24);
        seekBarValueExpiration.setText("  " + String.valueOf(24) + " h visible to the world");


        final int[] seekvalueExpiration = {0};

        seekBarNumberExperiation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                seekBarValueExpiration.setText(
                        "  " + String.valueOf(progress) + " h visible to the world");
                seekvalueExpiration[0] = progress;
                PhotonApplication.HOUR_TO_KEEP_PHOTO_DISPLAYED = seekvalueExpiration[0];
                if (seekvalueExpiration[0] == 48) {
                    seekBar.getProgressDrawable().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);

                } else {
                    seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorLightBlue), PorterDuff.Mode.SRC_IN);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentUser.put("numberDisplayed", seekvalueExpiration[0]);
                currentUser.saveInBackground();
                PhotonApplication.HOUR_TO_KEEP_PHOTO_DISPLAYED = seekvalueExpiration[0];
            }
        });

    }

    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, bmp);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bmp = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
    }
}
