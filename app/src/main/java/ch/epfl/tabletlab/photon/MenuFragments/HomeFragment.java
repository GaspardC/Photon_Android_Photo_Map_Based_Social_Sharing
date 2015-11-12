package ch.epfl.tabletlab.photon.MenuFragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import ch.epfl.tabletlab.photon.MenuActivity;
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




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.home, container, false);
        setUpViews();
        setUpMap();


        return parentView;
    }

    private void setUpViews() {
        MenuActivity parentActivity = (MenuActivity) getActivity();
        resideMenu = parentActivity.getResideMenu();
    }

    private void setUpMap() {

        fm=getChildFragmentManager();

        myMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.myMap);

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


            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(17).build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            mGoogleMap.clear();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(latitude, longitude));
            markerOptions.title("Here I am");
            markerOptions.snippet("(" + latitude + "," + longitude + ")");
            mGoogleMap.addMarker(markerOptions).showInfoWindow();

            /*USE TO CREATE HIS OWN MARKER*/
           /* String text = "Here I am";
            int textSize = 30;
            Paint paint = new Paint();
            paint.setTextSize(textSize);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.LEFT);
            int width = (int) (paint.measureText(text) + 0.5f); // round
            float baseline = (int) (-paint.ascent() + 0.5f); // ascent() is negative
            int height = (int) (baseline + paint.descent() + 0.5f);
            Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            Paint backPaint = new Paint();
            backPaint.setARGB(250, 0, 0, 255);
            backPaint.setAntiAlias(true);
            RectF backRect = new RectF(0, 0, width, height);
            canvas.drawRoundRect(backRect, 5, 5, backPaint);
            canvas.drawText(text, 0, baseline, paint);
            mGoogleMap.clear();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(latitude, longitude));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(image));
            mGoogleMap.addMarker(markerOptions).showInfoWindow();*/

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

}
