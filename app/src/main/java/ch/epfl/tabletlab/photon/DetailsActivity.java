package ch.epfl.tabletlab.photon;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DetailsActivity extends AppCompatActivity {

    private TextView likeText;
    private int numberLikes = 0;
    private PhotonPost post;
    boolean isImageFitToScreen = false;
    private ImageView imageViewDetailed;
    private String url;
    private Toolbar toolbar;
    private Button likeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        imageViewDetailed = (ImageView) findViewById(R.id.imageViewDetailed);
        TextView  textViewHashtags = (TextView) findViewById(R.id.textViewHashtagDetailed);
        TextView  textViewText = (TextView) findViewById(R.id.textViewTextDetailed);
        TextView  textViewAuthor = (TextView) findViewById(R.id.textViewAuthorDetailed);
        likeText = (TextView) findViewById(R.id.textViewLikes);
        likeButton = (Button) findViewById(R.id.buttonLike);




        Intent intent = getIntent();
        String markerId = intent.getExtras().getString("markerId");

        post = PostServer.getPhotonPost(markerId);
        ParseFile parseFile = post.getImage();
        url = parseFile.getUrl();

        Glide.with(this).load(url)
                .centerCrop()
                .crossFade()
                .thumbnail(0.1f)
                .error(R.drawable.load)
                .into(imageViewDetailed);

        String hashtags = intent.getExtras().getString("hashtags");
        textViewHashtags.setText(hashtags);
        String text = intent.getExtras().getString("text");
        textViewText.setText(text);


        numberLikes = post.getLikes();
        likeText.setText(numberLikes + " likes");

        textViewAuthor.setText("by @" + post.getAuthor());

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            onBackPressed();
            }
        });

    }

    public void addLike(View view) throws IOException, ClassNotFoundException {


        Map<String, String> hashMap = new HashMap<String, String>();
        File path = this.getFilesDir();
        File idPhotoFile = new File(path,"liked.photos");

        //load all the id this user has liked
        hashMap = loadHashmap(hashMap,idPhotoFile);

        // if the user has not already liked it +1
        if(!hashMap.containsKey(post.getObjectId())) {

            hashMap.put(post.getObjectId(), "1");
            writeHashmap((HashMap<String, String>) hashMap, idPhotoFile);
            numberLikes++;
            likeButton.setText("-1");
            likeText.setText(numberLikes + " likes");
            post.setLikes(numberLikes);
            post.saveInBackground();
    }
        else{
            hashMap.remove(post.getObjectId());
            writeHashmap((HashMap<String, String>) hashMap, idPhotoFile);
            numberLikes--;
            likeButton.setText("+1");
            likeText.setText(numberLikes + " likes");
            post.setLikes(numberLikes);
            post.saveInBackground();
        }


    }

    private Map<String, String> loadHashmap(Map<String, String> hashMap, File yourFile) throws IOException {

        if(!yourFile.exists()) {
            yourFile.createNewFile();
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(yourFile));

        for (String key : properties.stringPropertyNames()) {
            hashMap.put(key, properties.get(key).toString());
        }
        return hashMap;
    }

    private void writeHashmap(HashMap<String, String> hashMap, File yourFile) throws IOException {

        Properties properties = new Properties();

        for (Map.Entry<String,String> entry : hashMap.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        properties.store(new FileOutputStream(yourFile), null);
    }

    public void displayImageFullScreen(View view) {

        Intent intent = new Intent(this,FullScreenActivity.class);
        intent.putExtra("url",url);
        startActivity(intent);
        }


}
