package ch.epfl.tabletlab.photon.MenuFragments;

import android.location.Location;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.text.ParseException;

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
        ParseUser currentUser = DataManager.getUser();
        currentUser.put("Location",new ParseGeoPoint(userLocation.getLatitude(),userLocation.getLongitude()));
        currentUser.saveInBackground();

    }

    public static ParseUser getUser()  {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null) try {
            throw new ParseException("Parse user is null",0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return currentUser;
    }


    public static ParseGeoPoint getUserLocation(){
        return DataManager.getUser().getParseGeoPoint("Location");
    }

    public void pushPhotoToServer( ){
//        userOrderInformations.setOrder(orderElement);
//        userOrderInformations.saveInBackground();
    }

    /*
* Helper method to get the Parse GEO point representation of a location
*/
    public static ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }
}
