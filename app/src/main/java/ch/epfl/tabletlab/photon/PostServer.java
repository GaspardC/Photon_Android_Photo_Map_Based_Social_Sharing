package ch.epfl.tabletlab.photon;

import android.location.Location;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.tabletlab.photon.MenuFragments.DataManager;

/**
 * Created by Gasp on 12/12/15.
 */
public class PostServer {

    static int MAX_POST_SEARCH_RESULTS = PhotonApplication.MAX_POST_SEARCH_RESULTS;
    static int MAX_POST_SEARCH_DISTANCE = PhotonApplication.MAX_POST_SEARCH_DISTANCE;

    public static ParseQuery<PhotonPost> getQueryHashtagAtALocation(ArrayList<String> hastags, Location position) {
        ParseQuery<PhotonPost> mapQuery = null;

        List<ParseQuery<PhotonPost>> queries = new ArrayList<ParseQuery<PhotonPost>>();

        for (String k : hastags) {
            ParseQuery<PhotonPost> pQuery = setMapQuery(position, "#" + k);
            queries.add(pQuery);
        }
        mapQuery = ParseQuery.or(queries);
        mapQuery.include("user");
        mapQuery.orderByDescending("createdAt");
        mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);

        return mapQuery;
    }

    public static  ParseQuery<PhotonPost> setMapQuery(Location myLocation, String k) {

        ParseGeoPoint myPoint = DataManager.geoPointFromLocation(myLocation);
        // Create the map Parse query
        ParseQuery<PhotonPost> mapQuery = PhotonPost.getQuery();
        // Set up additional query filters

        // Query Expiration
        Date todaysDate = new Date(new Date().getTime());
        mapQuery.whereGreaterThanOrEqualTo("expirationDate", todaysDate);

        mapQuery.whereWithinKilometers("location", myPoint, MAX_POST_SEARCH_DISTANCE);

        if (!k.equals("")) {
            mapQuery.whereContains("text", k);
        }

        return mapQuery;
    }

    public static PhotonPost getPhotonPost(String id) {
        ParseQuery<PhotonPost> query = PhotonPost.getQuery();
        query.whereEqualTo("objectId", id);

        PhotonPost post;
        try {
            List<PhotonPost> posts =  query.find();
            post = posts.get(0);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return post;

    }






}
