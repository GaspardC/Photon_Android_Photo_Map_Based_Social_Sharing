package ch.epfl.tabletlab.photon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import ch.epfl.tabletlab.photon.MenuFragments.DataManager;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ImageView imageViewDetailed = (ImageView) findViewById(R.id.imageViewDetailed);


        Intent intent = getIntent();
        String markerId = intent.getExtras().getString("markerId");

        PhotonPost post = PostServer.getPhotonPost(markerId);
        ParseFile parseFile = post.getImage();
        String url = parseFile.getUrl();

        Glide.with(this).load(url)
                .centerCrop()
                .placeholder(R.drawable.spinner_static)
                .crossFade()
                .into(imageViewDetailed);



    }
}
