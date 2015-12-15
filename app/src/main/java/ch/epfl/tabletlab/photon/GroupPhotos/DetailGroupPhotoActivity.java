package ch.epfl.tabletlab.photon.GroupPhotos;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;

import ch.epfl.tabletlab.photon.LightMarker;
import ch.epfl.tabletlab.photon.R;

public class DetailGroupPhotoActivity extends AppCompatActivity {

    Toolbar mToolbar;
    RecyclerView mRecyclerView;


    ArrayList<ImageModel> data = new ArrayList<>();

    public  ArrayList<String> imgs = new ArrayList<>();
    private MasonryAdapter madapter;
    private  ArrayList<String> hashatgs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        HashSet<LightMarker> hashSet = (HashSet<LightMarker>) intent.getExtras().get("markers");
        for(LightMarker marker : hashSet){
            imgs.add(marker.getUrl());
            if((marker.getHashtags()) != null){
                hashatgs.add(marker.getHashtags());
            }
            else{
                hashatgs.add("");
            }
        }
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }


        for (int i = 0; i < imgs.size(); i++) {

            ImageModel imageModel = new ImageModel();
            imageModel.setName(hashatgs.get(i));
            imageModel.setUrl(imgs.get(i));
            data.add(imageModel);
        }



        mRecyclerView = (RecyclerView) findViewById(R.id.masonry_grid);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        madapter = new MasonryAdapter(this,data);
        mRecyclerView.setAdapter(madapter);
        SpacesItemDecoration decoration = new SpacesItemDecoration(16);
        mRecyclerView.addItemDecoration(decoration);


        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {

                        Intent intent = new Intent(DetailGroupPhotoActivity.this, DetailSwipeActivity.class);
                        intent.putParcelableArrayListExtra("data", data);
                        intent.putExtra("pos", position);
                        startActivity(intent);

                    }
                }));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
