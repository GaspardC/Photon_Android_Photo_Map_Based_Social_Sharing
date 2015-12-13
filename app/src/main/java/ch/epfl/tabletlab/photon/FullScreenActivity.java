package ch.epfl.tabletlab.photon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class FullScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        Intent intent = getIntent();
        String url = intent.getExtras().getString("url");

        ImageView image = (ImageView) findViewById(R.id.fullscreen_image);
        Glide.with(this).load(url)
                .thumbnail(0.1f)
                .into(image);


    }
}
