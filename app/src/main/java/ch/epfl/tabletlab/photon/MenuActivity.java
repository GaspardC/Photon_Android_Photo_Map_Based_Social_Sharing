package ch.epfl.tabletlab.photon;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.parse.ParseUser;

import ch.epfl.tabletlab.photon.MenuFragments.HomeFragment;
import ch.epfl.tabletlab.photon.MenuFragments.SettingsFragment;
import ch.epfl.tabletlab.photon.ResideMenu.ResideMenu;
import ch.epfl.tabletlab.photon.ResideMenu.ResideMenuItem;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;




public class MenuActivity extends FragmentActivity implements View.OnClickListener{

        private ResideMenu resideMenu;
        private MenuActivity mContext;
        private ResideMenuItem itemHome;
        private ResideMenuItem itemProfile;
        private ResideMenuItem itemPost;
        private ResideMenuItem itemSettings;
        private ParseUser currentUser;



    /**
         * Called when the activity is first created.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            mContext = this;
            setUpMenu();
            if( savedInstanceState == null )
                changeFragment(new HomeFragment());
        }






    private void setUpMenu() {

            // attach to current activity;
            resideMenu = new ResideMenu(this);
            resideMenu.setUse3D(true);
            resideMenu.setBackground(R.drawable.small_background);
            resideMenu.attachToActivity(this);
            resideMenu.setMenuListener(menuListener);
            //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip.
            resideMenu.setScaleValue(0.6f);

            // create menu items;
            itemHome     = new ResideMenuItem(this, R.drawable.icon_home,     "Home");
            itemProfile  = new ResideMenuItem(this, R.drawable.icon_profile,  "Profile");
            itemPost = new ResideMenuItem(this, R.drawable.icon_calendar, "Post");
            itemSettings = new ResideMenuItem(this, R.drawable.icon_settings, "Settings");

            itemHome.setOnClickListener(this);
            itemProfile.setOnClickListener(this);
            itemPost.setOnClickListener(this);
            itemSettings.setOnClickListener(this);

            resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
            resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
            resideMenu.addMenuItem(itemPost, ResideMenu.DIRECTION_LEFT);
            resideMenu.addMenuItem(itemSettings, ResideMenu.DIRECTION_LEFT);

            // You can disable a direction by setting ->
/*
            // resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
*/

            findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                }
            });
/*            findViewById(R.id.title_bar_right_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resideMenu.openMenu(ResideMenu.DIRECTION_RIGHT);
                }
            });*/
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            return resideMenu.dispatchTouchEvent(ev);
        }

        @Override
        public void onClick(View view) {

            if (view == itemHome){
                changeFragment(new HomeFragment());
            }else if (view == itemProfile){

                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);

            }else if (view == itemPost){
//                changeFragment(new CalendarFragment());
                Intent intent = new Intent(this, PostActivity.class);
                startActivity(intent);
            }else if (view == itemSettings){
                changeFragment(new SettingsFragment());
            }

            resideMenu.closeMenu();
        }



        private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
            @Override
            public void openMenu() {
                Toast.makeText(mContext, "Menu is opened!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void closeMenu() {
                Toast.makeText(mContext, "Menu is closed!", Toast.LENGTH_SHORT).show();
            }
        };

        public void changeFragment(Fragment targetFragment){
            resideMenu.clearIgnoredViewList();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment, targetFragment, "fragment")
                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }

        // What good method is to access resideMenu？
        public ResideMenu getResideMenu(){
            return resideMenu;
        }
    }
