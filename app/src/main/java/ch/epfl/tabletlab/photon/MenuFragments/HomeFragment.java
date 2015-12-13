package ch.epfl.tabletlab.photon.MenuFragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import ch.epfl.tabletlab.photon.DetailsActivity;
import ch.epfl.tabletlab.photon.PhotonPost;
import ch.epfl.tabletlab.photon.MenuActivity;
import ch.epfl.tabletlab.photon.MyMarker;
import ch.epfl.tabletlab.photon.PhotonApplication;
import ch.epfl.tabletlab.photon.PostServer;
import ch.epfl.tabletlab.photon.R;
import ch.epfl.tabletlab.photon.ResideMenu.ResideMenu;


public class HomeFragment extends Fragment {

    /*
     * Constants for handling location results
     */
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;

    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;
    private static boolean HASHTAG_QUERY = true;


    // Maximum post search radius for map in kilometers
    public static int MAX_POST_SEARCH_DISTANCE = 100;

    private View parentView;
    private ResideMenu resideMenu;
    private GoogleMap mGoogleMap;
    private Button searchButton;
    private Button deleteButton;
    FragmentManager fm;
    SupportMapFragment myMapFragment;

    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private int mostRecentMapUpdate;
    private Location currentLocation;
    private android.location.LocationManager locationManager;
    private Location currentMapLocation;
    private HashMap<String, Object> toKeep = new HashMap<>();
    private CameraPosition cameraPosition;
    private ArrayList<String> hastags;
    private EditText hastagsEditText;
    private String EditTextReformated;
    private boolean mapActive = true;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Bitmap bmOverlay;

/*
    public HomeFragment(MenuActivity menuActivity) {
         parentActivity = menuActivity;
    }
*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.home, container, false);
        // Initialize the HashMap for Markers and MyMarker object
//        mMarkersHashMap = new HashMap<Marker, MyMarker>();

        setUpViews();
        setUpMap();
        setSearchOptions();
        setDeleteButton();
        verifyStoragePermissions(getActivity());
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);




        return parentView;
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

    private void setDeleteButton() {
        deleteButton = (Button) getActivity().findViewById(R.id.delete_right_menu);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hastagsEditText.setText("");
                mapActive = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    deleteButton.setBackground(getActivity().getDrawable(R.drawable.delete));
                }
                hastagsEditText.setTextColor(getResources().getColor(R.color.grayColorTextHint));

            }
        });
    }

    private void setSearchOptions() {


        hastagsEditText = (EditText) getActivity().findViewById(R.id.hashtags_text_view);
        hastagsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                hastagsEditText.setTextColor(getResources().getColor(R.color.grayColorText));
                if (hastagsEditText.length() > 40) {
                    sequence = hastagsEditText.getText().subSequence(0, 40);
                }
                EditTextReformated = String.valueOf(sequence);
                EditTextReformated = EditTextReformated.toLowerCase();
                EditTextReformated = EditTextReformated.replaceAll(" ", " #");
                Log.d("searchtext", EditTextReformated);

            }

            @Override
            public void afterTextChanged(Editable s) {
//                        searchButton.performClick();
            }
        });

        searchButton= (Button) getActivity().findViewById(R.id.search_right_menu);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    searchButton.setBackground(getActivity().getDrawable(R.drawable.yellow_search));
                    deleteButton.setBackground(getActivity().getDrawable(R.drawable.delete_yellow));
                }


                String text = String.valueOf(EditTextReformated);
                if (text.isEmpty() || text.equals("null")) {
                    text = getString(R.string.slogan);
                }
                hastags = new ArrayList<String>();
                text = text.replace(" ", "");
                String[] splitText = text.split("#");
                for (int i = 0; i < splitText.length; i++) {
                    if (!splitText[i].equals("")) {
                        hastags.add(splitText[i]);
                    }
                }
                if (!hastags.isEmpty()) {
                    mapActive = false;
                    HASHTAG_QUERY = true;
                    doMapQuery(HASHTAG_QUERY);
                }

            }
        });
    }



    private void displayImage() {
        //TODO utiliser la reele position de la personne
//        // Initialize the HashMap for Markers and MyMarker object
        mMarkersHashMap = new HashMap<Marker, MyMarker>();
        mGoogleMap.clear();
        Log.d("clear","clear");

        //TODO here get all images in the map and add them
        plotMarkers();

    }
    private void plotMarkers()
    {
        if(toKeep.size() > 0)
        {
            int countMaxMarkerDisplayed = 0;
            for(String currentKey :  toKeep.keySet())
            {
                if(countMaxMarkerDisplayed<PhotonApplication.MAX_POST_SEARCH_RESULTS) {

                    countMaxMarkerDisplayed++;
                    final MyMarker myMarker = (MyMarker) toKeep.get(currentKey);
                    // Create user marker with custom icon and other options
                    MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()));

//                    markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.currentlocation_icon));

                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(myMarker.getImage()));


                    Marker currentMarker = mGoogleMap.addMarker(markerOption);
                    mMarkersHashMap.put(currentMarker, myMarker);

                    mGoogleMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
                    mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            Intent intent = new Intent(getActivity(), DetailsActivity.class);
                            MyMarker myMarker1 = mMarkersHashMap.get(marker);
                            intent.putExtra("markerId",myMarker1.getId());
                            intent.putExtra("text",myMarker1.getText());
                            intent.putExtra("hashtags",myMarker1.getHashtags());

                            startActivity(intent);
                            return true;
                        }
                    });

                    mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Intent intent = new Intent(getActivity(), DetailsActivity.class);
                            MyMarker myMarker1 = mMarkersHashMap.get(marker);
                            intent.putExtra("markerId",myMarker1.getId());
                            intent.putExtra("text",myMarker1.getText());
                            intent.putExtra("hashtags",myMarker1.getHashtags());

                            startActivity(intent);
                        }
                    });
//                    currentMarker.showInfoWindow();
                }
                else{
                    break;
                }
            }
        }
    }

    private void setUpViews() {
        MenuActivity parentActivity = (MenuActivity) getActivity();
        resideMenu = parentActivity.getResideMenu();
    }

    private void setUpMap() {

        fm=getChildFragmentManager();

        myMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.myMap);

        // to add gesture operation's ignored views
        FrameLayout ignored_view = (FrameLayout) parentView.findViewById(R.id.myMap);
        resideMenu.addIgnoredView(ignored_view);

        // Set up a progress dialog
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.progress_map_init));
        dialog.show();


        myMapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googlemap) {
                // TODO Auto-generated method stub
                mGoogleMap = googlemap;
                dialog.dismiss();
                startMap();
                displayImage();
            }
        });
//        mGoogleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(
//                R.id.map)).getMap();
        // Changing map type

        myMapFragment.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                // When the camera changes, update the query
                Location loc = new Location("cam");
                loc.setLatitude(position.target.latitude);
                loc.setLongitude(position.target.longitude);
                currentMapLocation = loc;

                if (!mapActive) return; // if th mode search by keyword is active
                HASHTAG_QUERY = false;
                doMapQuery(HASHTAG_QUERY);
            }
        });

    }

    /*
       * Set up the query to update the map view
       */
    private void doMapQuery(boolean hashtagQuery) {



        final int myUpdateNumber = ++mostRecentMapUpdate;
        Location myLoc = (currentMapLocation == null) ? currentLocation : currentMapLocation;

        // If location info isn't available, clean up any existing markers
        if (myLoc == null) {
//            cleanUpMarkers(new HashSet<String>());
            Toast.makeText(this.getActivity(), "Unknown location", Toast.LENGTH_SHORT).show();
            return;
        }
        /*final ParseGeoPoint myPoint = DataManager.geoPointFromLocation(myLoc);
        // Create the map Parse query*/

        PhotonApplication.MAX_POST_SEARCH_DISTANCE = distanceForMapQuery();
        ParseQuery<PhotonPost> mapQuery = null;
        if(hashtagQuery) {
            mapQuery = PostServer.getQueryHashtagAtALocation(hastags, myLoc);
        }
        else{
            mapQuery = PostServer.setMapQuery(myLoc, "");

        }

        // Kick off the query in the background
        assert mapQuery != null;

        mapQuery.findInBackground(new FindCallback<PhotonPost>() {
            @Override
            public void done(List<PhotonPost> objects, ParseException e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //change color of search item
                    searchButton.setBackground(getActivity().getDrawable(R.drawable.search));

                    // hide keyboard
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }

                if (e != null) {
                    if (PhotonApplication.APPDEBUG) {
                        Log.d(PhotonApplication.APPTAG, "An error occurred while querying for map posts.", e);
                    }
                    return;
                }
        /*
         * Make sure we're processing results from
         * the most recent update, in case there
         * may be more than one in progress.
         */
                if (myUpdateNumber != mostRecentMapUpdate) {
                    return;
                }
                // Posts to show on the map

                // Loop through the results of the search
                for (final PhotonPost post : objects) {
                    // Add this post to the list of map pins to keep

/*
                     Check if you need to download the post
                     If you have already downloaded do not download it again return false
*/
                    boolean needToAddThisPost = doYouNeedToAddThisPost(post);
                    Log.d("add photo", String.valueOf(needToAddThisPost));

                    if (needToAddThisPost == true) {

/*
                    Retrieve the image from the server
*/
                        // Set up the map marker's location
                        ParseFile image = post.getImage();
                        ParseFile thumbnail = image;

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
//                                Looper.prepare();
                                ParseFile image = post.getImage();


                                Bitmap cadrePhoto = BitmapFactory.decodeResource(getActivity().getResources(),
                                        R.drawable.canvas_marker);

                                //these width and height depends on the photo , do not change them (or independently)
                                Bitmap cadre = Bitmap.createScaledBitmap(cadrePhoto, 160, 160, false);


                                int width = cadre.getWidth();
                                int height = cadre.getHeight();

                                int myWidth = 100;
                                int myHeight = 100;

                                try {
                                    Bitmap bitmapPhoto = Glide
                                            .with(getActivity().getApplicationContext()) // could be an issue!
                                            .load(image.getUrl())
                                            .asBitmap()
                                            .into(100, 100)
                                            .get();

                                    Bitmap photo = Bitmap.createScaledBitmap(bitmapPhoto, 145, 114, false);
                                    Bitmap roundPhoto = getRoundedCornerBitmap(photo, 10);

                                    int widthPhoto = photo.getWidth();
                                    int heightPhoto = photo.getHeight();


                                    bmOverlay = Bitmap.createBitmap(cadre.getWidth(), cadre.getHeight(), cadre.getConfig());
                                    Canvas canvas = new Canvas(bmOverlay);
                                    canvas.drawBitmap(cadre, new Matrix(), null);
                                    canvas.drawBitmap(roundPhoto, 8, 8, null);


                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                } catch (ExecutionException e1) {
                                    e1.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void dummy) {
                                if (null != bmOverlay) {

                                    // The full bitmap should be available here
                                    MyMarker newMarker = new MyMarker(post.getText(), "", post.getLocation().getLatitude(),
                                            post.getLocation().getLongitude(), bmOverlay);
                                    newMarker.setId(post.getObjectId());
                                    newMarker.setHashtags(post.getHashtags());
                                    newMarker.setAuthor(post.getAuthor());
                                    toKeep.put(post.getObjectId(), newMarker);
                                    displayImage();


                                }
                                ;
                            }
                        }.execute();



                       /*if (thumbnail != null) {
                            thumbnail.getDataInBackground(new GetDataCallback() {

                                @Override
                                public void done(byte[] data, ParseException e) {

                                    if (e == null) {

                                        final BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inSampleSize = 4;

                                        Bitmap bmp = BitmapFactory
                                                .decodeByteArray(
                                                        data, 0,
                                                        data.length, options);
                                        if (bmp != null) {
                                            Log.e("parse file ok", " null");

                                            MyMarker newMarker = new MyMarker(post.getText(), "", post.getLocation().getLatitude(),
                                                    post.getLocation().getLongitude(), bmp);
                                            newMarker.setId(post.getObjectId());

                                            toKeep.put(post.getObjectId(), newMarker);

                                        }
                                    } else {
                                        Log.e("paser after download", " null");
                                    }
                                }
                            });
                        } else {
                            Log.e("parse file", " null");
                        }*/
                    }
                }
                displayAndCleanImagesIfTheyAreNotInTheServer(objects);
            }
        });
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }



    private void displayAndCleanImagesIfTheyAreNotInTheServer(List<PhotonPost> objects) {

        //if some objects are in toKeep but not in objects delete them
        // Loop through the results of the search
        ArrayList<String> keyToRemove = new ArrayList();

        for (String currentKey : toKeep.keySet()) {
            boolean found = false;
            for (final PhotonPost post : objects) {
                if (post.getObjectId().equals(currentKey)) {
                    found = true;
                }
            }
            if (!found) {
                keyToRemove.add(currentKey);
                Marker markerToRemove = getKeyByValue(mMarkersHashMap, (MyMarker) toKeep.get(currentKey));
                if(markerToRemove!= null){
                    markerToRemove.remove();
                }
            }

        }

        for (String key : keyToRemove) {
            toKeep.remove(key);
        }

//        displayImage();
        //not needed anymore
    }
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private boolean doYouNeedToAddThisPost(PhotonPost post) {

        boolean needToAddThisPost = false;

        /* Check if the post exist already in the hashMap */
        if (toKeep.get(post.getObjectId()) == null){
            Log.d("add photo","object " +post.getText() + " was not here");
            needToAddThisPost = true;
        }
        else{
            Log.d("add photo","object " +post.getText() + "already here");
        }


        return needToAddThisPost;
    }

    public int distanceForMapQuery(){


        /* But even if the object was not here maybe you dont want to download it if its not visible on the map*/
        LatLngBounds mLatLngBounds = myMapFragment.getMap().getProjection().getVisibleRegion().latLngBounds;

        LatLng northeast = mLatLngBounds.northeast ;
        LatLng southwest = mLatLngBounds.southwest;
        ParseGeoPoint southwestParseGeoPoint = new ParseGeoPoint(southwest.latitude,southwest.longitude);
        ParseGeoPoint locatonCenterCamera = new ParseGeoPoint(0.5*(northeast.latitude + southwest.latitude),0.5*(northeast.longitude + southwest.longitude));


        double distanceToCorner = locatonCenterCamera.distanceInKilometersTo(southwestParseGeoPoint);
        return (int) distanceToCorner + 10;
        // to round number at the superior int and have a little margin if the user move the map quickly
    }




    private void startMap() {

        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Showing/hiding your current location
        mGoogleMap.setMyLocationEnabled(true);
        // Enable/disable zooming controls
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        // Enable/disable my location button
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        // Enable/disable compass icon
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        // Enable/disable rotate gesture
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);

        // Enable/disable zooming functionality
//        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

        String context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getActivity().getSystemService(context);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        provider = LocationManager.NETWORK_PROVIDER;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
            if (location != null) {
                updateWithNewLocation(location);
            }
            else{
                Location locationTemp = new Location("temp");
                locationTemp.setLatitude(DataManager.getUserLocation().getLatitude());
                locationTemp.setLongitude(DataManager.getUserLocation().getLongitude());
                updateWithNewLocation(locationTemp);
            }
        }
    }


    private void updateWithNewLocation(Location location) {
        currentLocation = location;
        String latLongString;
/*        TextView myLocationText;
        myLocationText = (TextView) parentView.findViewById(R.id.myLocationText);
        String addressString = "No address found";*/
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            latLongString = "Lat: " + latitude + "\nLong: " + longitude;
/*
            Geocoder gc = new Geocoder(getActivity(), Locale.getDefault());
*/

            // TO DO just do it at the beginning or on press compass
            new DataManager().setUserLocation(location);
            cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(10).build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if(currentLocation  == null){ //TO DO Updtate the locatoin
                updateWithNewLocation(location);
            }
        }

        public void onProviderDisabled(String provider) {

            updateWithNewLocation(null);
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };

    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {
        public MarkerInfoWindowAdapter()
        {
        }

        @Override
        public View getInfoWindow(Marker marker)
        {
            View v  = getActivity().getLayoutInflater().inflate(R.layout.infowindow_layout, null);

            MyMarker myMarker = mMarkersHashMap.get(marker);

            ImageView markerIcon = (ImageView) v.findViewById(R.id.marker_icon);

            TextView markerAuthor = (TextView)v.findViewById(R.id.author_post);
            TextView markerHashtags = (TextView)v.findViewById(R.id.marker_hashtags);

            String hashtagsTextDisplayed = myMarker.getHashtags();
/*            String hashtagsArr = myMarker.getHashtags();
            if ( hashtagsArr!= null) {
                for (int i = 0; i < hashtagsArr.length(); i++) {
                    try {
                        hashtagsTextDisplayed = hashtagsTextDisplayed + " #" + hashtagsArr.getString(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }*/

            markerHashtags.setText(hashtagsTextDisplayed);

            markerIcon.setImageBitmap(myMarker.getImage());

            markerAuthor.setText("by @" + myMarker.getAuthor());
            return v;
        }

        @Override
        public View getInfoContents(Marker marker)
        {
            return  null;
        }
    }







    }


