package ch.epfl.tabletlab.photon.MenuFragments;

import android.Manifest;
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ch.epfl.tabletlab.photon.MenuActivity;
import ch.epfl.tabletlab.photon.MyMarker;
import ch.epfl.tabletlab.photon.R;
import ch.epfl.tabletlab.photon.ResideMenu.ResideMenu;

/**
 * User: special
 * Date: 13-12-22
 * Time: 下午1:33
 * Mail: specialcyci@gmail.com
 */
public class HomeFragment extends Fragment {

    private View parentView;
    private ResideMenu resideMenu;
    private GoogleMap mGoogleMap;
    FragmentManager fm;
    SupportMapFragment myMapFragment;

    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.home, container, false);
        setUpViews();
        setUpMap();




        return parentView;
    }

    private void displayImage() {
        //TODO utiliser la reele position de la personne
        // Initialize the HashMap for Markers and MyMarker object
        mMarkersHashMap = new HashMap<Marker, MyMarker>();

        mMyMarkersArray.add(new MyMarker("#labEPFL", "", Double.parseDouble("46.5269830"), Double.parseDouble("6.5674850")));
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

    }

    private void startMap() {

        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
// Showing/hiding your current location mGoogleMap.setMyLocationEnabled(true);
// Enable/disable zooming controls mGoogleMap.getUiSettings().setZoomControlsEnabled(false); // Enable/disable my location button
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
// Enable/disable compass icon
        mGoogleMap.getUiSettings().setCompassEnabled(true);
// Enable/disable rotate gesture mGoogleMap.getUiSettings().setRotateGesturesEnabled(true); // Enable/disable zooming functionality mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        LocationManager locationManager;
        String context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getActivity().getSystemService(context);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                updateWithNewLocation(location);
            }
        }
        locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
    }


    private void updateWithNewLocation(Location location) {
        String latLongString;
        TextView myLocationText;
        myLocationText = (TextView) parentView.findViewById(R.id.myLocationText);
        String addressString = "No address found";
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            latLongString = "Lat: " + latitude + "\nLong: " + longitude;
            Geocoder gc = new Geocoder(getActivity(), Locale.getDefault());

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
            updateWithNewLocation(location);
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
            markerIcon.setImageResource(R.drawable.smallbeautifulimage);


            markerLabel.setText(myMarker.getmLabel());
            markerOtherText.setText(myMarker.getmIcon());


            return v;
        }
    }

    }


