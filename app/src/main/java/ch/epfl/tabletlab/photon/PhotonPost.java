package ch.epfl.tabletlab.photon;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data model for a post.
 */
@ParseClassName("Posts")
public class PhotonPost extends ParseObject {



    public String getText() {
        return getString("text");
    }

    public void setText(String value) {
        put("text", value);
    }

    public String getHashtags() {
        return getString("hashtags");
    }

    public void setHashtags(String hash) {
        put("hashtags", hash);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public void setImage(ParseFile image) {
        put("image", image);
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }

    public static ParseQuery<PhotonPost> getQuery() {
        return ParseQuery.getQuery(PhotonPost.class);
    }


    public void setLikes(int like) {
        put("likes", like);
    }

    public int getLikes() {
        return (int) get("likes");
    }
}
