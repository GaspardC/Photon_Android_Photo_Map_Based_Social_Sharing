package ch.epfl.tabletlab.photon.MenuFragments;

import android.location.Location;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

/**
 * Created by Gasp on 15/11/2015.
 */
 public  class DataManager {

    private static Location userLocation;

    public DataManager() {

//        userOrderInformations = new ParseUserOrderInformations();
//        user = userOrderInformations.getUser();
//        userLocation = user.getParseGeoPoint("Location");
//        maxDeliveryDistance = ((double) (Integer)user.get("maxDeliveryDistanceKm"));

    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put("Location",new ParseGeoPoint(userLocation.getLatitude(),userLocation.getLongitude()));
        currentUser.saveInBackground();

    }

    public static ParseUser getUser(){
        ParseUser currentUser = ParseUser.getCurrentUser();
        return currentUser;
    }


    public static ParseGeoPoint getUserLocation(){
        ParseUser currentUser = ParseUser.getCurrentUser();
        return currentUser.getParseGeoPoint("Location");
    }

    public void pushPhotoToServer( ){
//        userOrderInformations.setOrder(orderElement);
//        userOrderInformations.saveInBackground();
    }
}
