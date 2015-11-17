package ch.epfl.tabletlab.photon.MenuFragments;

import android.Manifest;
import android.app.Application;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mopub.volley.toolbox.ImageLoader;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ch.epfl.tabletlab.photon.AnywallPost;
import ch.epfl.tabletlab.photon.MenuActivity;
import ch.epfl.tabletlab.photon.MyMarker;
import ch.epfl.tabletlab.photon.PhotonApplication;
import ch.epfl.tabletlab.photon.R;
import ch.epfl.tabletlab.photon.ResideMenu.ResideMenu;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * User: special
 * Date: 13-12-22
 * Time: 下午1:33
 * Mail: specialcyci@gmail.com
 */
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

    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;

    // Maximum post search radius for map in kilometers
    private static final int MAX_POST_SEARCH_DISTANCE = 100;

    private View parentView;
    private ResideMenu resideMenu;
    private GoogleMap mGoogleMap;
    FragmentManager fm;
    SupportMapFragment myMapFragment;

    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();
    private int mostRecentMapUpdate;
    private Location currentLocation;
    private android.location.LocationManager locationManager;
    private Location currentMapLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.home, container, false);
        // Initialize the HashMap for Markers and MyMarker object
        mMarkersHashMap = new HashMap<Marker, MyMarker>();
        setUpViews();
        setUpMap();

        //Create image options.
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.button_default)
//                .cacheInMemory(true)
//                .cacheOnDisc(true)
//                .build();
//
//        //Create a config with those options.
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
//                .defaultDisplayImageOptions(options)
//                .build();
//
//        ImageLoader.getInstance().init(config);




        return parentView;
    }

    private void displayImage() {
        //TODO utiliser la reele position de la personne
//        // Initialize the HashMap for Markers and MyMarker object
//        mMarkersHashMap = new HashMap<Marker, MyMarker>();

//        mMyMarkersArray.add(new MyMarker("#labEPFL", "", Double.parseDouble("46.5269830"), Double.parseDouble("6.5674850")));
        //TODO here get all images in the map and add them
        plotMarkers(mMyMarkersArray);

    }
    private void plotMarkers(ArrayList<MyMarker> markers)
    {
        if(markers.size() > 0)
        {
            for (MyMarker myMarker : markers)
            {

                // Create user marker with custom icon and other options
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()));
                markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.currentlocation_icon));

                Marker currentMarker = mGoogleMap.addMarker(markerOption);
                mMarkersHashMap.put(currentMarker, myMarker);

                mGoogleMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
                currentMarker.showInfoWindow();
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
                mGoogleMap=googlemap;
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
                doMapQuery();
            }
        });

    }

    /*
       * Set up the query to update the map view
       */
    private void doMapQuery() {
        final int myUpdateNumber = ++mostRecentMapUpdate;
        Location myLoc = (currentMapLocation == null) ? currentLocation : currentMapLocation;

        // If location info isn't available, clean up any existing markers
        if (myLoc == null) {
//            cleanUpMarkers(new HashSet<String>());
            Toast.makeText(this.getActivity(), "Unknown location", Toast.LENGTH_SHORT).show();
            return;
        }
        final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);
        // Create the map Parse query
        ParseQuery<AnywallPost> mapQuery = AnywallPost.getQuery();
        // Set up additional query filters
        mapQuery.whereWithinKilometers("location", myPoint, MAX_POST_SEARCH_DISTANCE);
        mapQuery.include("user");
        mapQuery.orderByDescending("createdAt");
        mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);
        // Kick off the query in the background
        mapQuery.findInBackground(new FindCallback<AnywallPost>() {
            @Override
            public void done(List<AnywallPost> objects, ParseException e) {
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
                Set<String> toKeep = new HashSet<String>();
                mMyMarkersArray = new ArrayList<MyMarker>();
                // Loop through the results of the search
                for (final AnywallPost post : objects) {
                    // Add this post to the list of map pins to keep
                    toKeep.add(post.getObjectId());
                    // Check for an existing marker for this post
//                    MyMarker oldMarker = mMarkersHashMap.get(post.getObjectId());

                    // Set up the map marker's location
                    ParseFile image = post.getImage();


                    ParseFile thumbnail = image;
                    final ImageView img = new ImageView(getActivity());
                    final Bitmap[] bmp = new Bitmap[1000];

                    if (thumbnail != null) {
                        thumbnail.getDataInBackground(new GetDataCallback() {

                            @Override
                            public void done(byte[] data, ParseException e) {

                                if (e == null) {
                                    bmp[0] = BitmapFactory.decodeByteArray(data, 0,
                                            data.length);

                                    if (bmp[0] != null) {

                                        Log.e("parse file ok", " null");
                                        // img.setImageBitmap(Bitmap.createScaledBitmap(bmp,
                                        // (display.getWidth() / 5),
                                        // (display.getWidth() /50), false));
//                                        img.setImageBitmap(bmp[0]);
                                        // img.setPadding(10, 10, 0, 0);

                                        MyMarker newMarker = new MyMarker(post.getText(), "", post.getLocation().getLatitude(),
                                                post.getLocation().getLongitude(), bmp[0]);
                                        mMyMarkersArray.add(newMarker);



                                    }
                                } else {
                                    Log.e("paser after downloade", " null");
                                }

                            }
                        });
                    } else {

                        Log.e("parse file", " null");

                        // img.setImageResource(R.drawable.ic_launcher);

                        img.setPadding(10, 10, 10, 10);
                    }



//                    MyMarker newMarker = new MyMarker(post.getText(), "", post.getLocation().getLatitude(),
//                            post.getLocation().getLongitude(), bmp[0]);
//                    mMyMarkersArray.add(newMarker);
                    }
                }

        });
        displayImage();

    }

    /*
 * Helper method to get the Parse GEO point representation of a location
 */
    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }
    private void startMap() {

        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
// Showing/hiding your current location mGoogleMap.setMyLocationEnabled(true);
// Enable/disable zooming controls mGoogleMap.getUiSettings().setZoomControlsEnabled(false); // Enable/disable my location button
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
// Enable/disable compass icon
        mGoogleMap.getUiSettings().setCompassEnabled(true);
// Enable/disable rotate gesture mGoogleMap.getUiSettings().setRotateGesturesEnabled(true); // Enable/disable zooming functionality mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

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
        }
    }


    private void updateWithNewLocation(Location location) {
        currentLocation = location;
        String latLongString;
        TextView myLocationText;
        myLocationText = (TextView) parentView.findViewById(R.id.myLocationText);
        String addressString = "No address found";
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            latLongString = "Lat: " + latitude + "\nLong: " + longitude;
            Geocoder gc = new Geocoder(getActivity(), Locale.getDefault());

            // TO DO just do it at the beginning or on press compass
            new DataManager().setUserLocation(location);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(17).build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

//            mGoogleMap.clear();
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(new LatLng(latitude, longitude));
//            markerOptions.title("Here I am");
//            markerOptions.snippet("(" + latitude + "," + longitude + ")");
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.smallbeautifulimage));
//            mGoogleMap.addMarker(markerOptions).showInfoWindow();

//            mGoogleMap.clear();
//            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
//            Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
//            Canvas canvas1 = new Canvas(bmp);
//
//// paint defines the text color,
//// stroke width, size
//            Paint color = new Paint();
//            color.setTextSize(35);
//            color.setColor(Color.BLACK);
//
////modify canvas
//            canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
//                    R.drawable.smallbeautifulimage), 0,0, color);
//            canvas1.drawText(ParseUser.getCurrentUser().getUsername(), 30, 40, color);
//
////add marker to Map
//            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
//                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
//                            // Specifies the anchor to be at a particular point in the marker image.
//                    .anchor(0.5f, 1));

//            /*USE TO CREATE CUSTOM MARKER*/
//            String text = "Here I am";
//            int textSize = 30;
//            Paint paint = new Paint();
//            paint.setTextSize(textSize);
//            paint.setColor(Color.WHITE);
//            paint.setTextAlign(Paint.Align.LEFT);
//            int width = (int) (paint.measureText(text) + 0.5f); // round
//            float baseline = (int) (-paint.ascent() + 0.5f); // ascent() is negative
//            int height = (int) (baseline + paint.descent() + 0.5f);
//            Bitmap image = Bitmap.createBitmap(width+50, height+50, Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(image);
//            Paint backPaint = new Paint();
//            backPaint.setARGB(250, 0, 0, 255);
//            backPaint.setAntiAlias(true);
//            RectF backRect = new RectF(0, 0, width+50, height+50);
//            canvas.drawRoundRect(backRect, 5, 5, backPaint);
//            canvas.drawText(text, 0, baseline, paint);
//            mGoogleMap.clear();
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(new LatLng(latitude, longitude));
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.smallbeautifulimage));
//            mGoogleMap.addMarker(markerOptions).showInfoWindow();

            try {
                List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
                StringBuilder sb = new StringBuilder();
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        sb.append("\n").append(address.getAddressLine(i));
                    }
                    addressString = sb.toString();
                }
            } catch (IOException e) {
            }
        } else {
            latLongString = "No location found";
        }
        myLocationText.setText("Your Current Position is: \n" + latLongString + "\n" + addressString);
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

        ;

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        ;
    };


    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {
        public MarkerInfoWindowAdapter()
        {
        }

        @Override
        public View getInfoWindow(Marker marker)
        {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker)
        {
            View v  = getActivity().getLayoutInflater().inflate(R.layout.infowindow_layout, null);

            MyMarker myMarker = mMarkersHashMap.get(marker);

            ImageView markerIcon = (ImageView) v.findViewById(R.id.marker_icon);

            TextView markerLabel = (TextView)v.findViewById(R.id.marker_label);
            TextView markerOtherText = (TextView)v.findViewById(R.id.another_label);


//            markerIcon.setImageResource(manageMarkerIcon(myMarker.getmIcon()));
//            markerIcon.setImageResource(R.drawable.smallbeautifulimage);
            markerIcon.setImageBitmap(myMarker.getImage());


            markerLabel.setText(myMarker.getmLabel());
            markerOtherText.setText(myMarker.getmIcon());


            return v;
        }
    }



    }


