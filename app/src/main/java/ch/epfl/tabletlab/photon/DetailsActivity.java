package ch.epfl.tabletlab.photon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ImageView imageViewDetailed = (ImageView) findViewById(R.id.imageViewDetailed);
        TextView  textViewHashtags = (TextView) findViewById(R.id.textViewHashtagDetailed);
        TextView  textViewText = (TextView) findViewById(R.id.textViewTextDetailed);
        likeText = (TextView) findViewById(R.id.textViewLikes);




        Intent intent = getIntent();
        String markerId = intent.getExtras().getString("markerId");

        post = PostServer.getPhotonPost(markerId);
        ParseFile parseFile = post.getImage();
        String url = parseFile.getUrl();

        Glide.with(this).load(url)
                .centerCrop()
                .crossFade()
                .into(imageViewDetailed);

        String hashtags = intent.getExtras().getString("hashtags");
        textViewHashtags.setText(hashtags);
        String text = intent.getExtras().getString("text");
        textViewText.setText(text);


        numberLikes = post.getLikes();
        likeText.setText(numberLikes + " likes");



    }

    public void addLike(View view) throws IOException, ClassNotFoundException {


        Map<String, String> hashMap = new HashMap<String, String>();
        File path = this.getFilesDir();

        File yourFile = new File(path,"liked.photos");
        if(!yourFile.exists()) {
            yourFile.createNewFile();
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(yourFile));

        for (String key : properties.stringPropertyNames()) {
            hashMap.put(key, properties.get(key).toString());
        }


        if(!hashMap.containsKey(post.getObjectId())) {

            hashMap.put(post.getObjectId(), "1");
            writeHashmap((HashMap<String, String>) hashMap,yourFile);
            numberLikes++;
            likeText.setText(numberLikes + " likes");
            post.setLikes(numberLikes);
            post.saveInBackground();
    }
        else{
            hashMap.remove(post.getObjectId());
            writeHashmap((HashMap<String, String>) hashMap, yourFile);
            numberLikes--;
            likeText.setText(numberLikes + " likes");
            post.setLikes(numberLikes);
            post.saveInBackground();
        }


    }

    private void writeHashmap(HashMap<String, String> hashMap, File yourFile) throws IOException {

        Properties properties = new Properties();

        for (Map.Entry<String,String> entry : hashMap.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        properties.store(new FileOutputStream(yourFile), null);
    }
}
