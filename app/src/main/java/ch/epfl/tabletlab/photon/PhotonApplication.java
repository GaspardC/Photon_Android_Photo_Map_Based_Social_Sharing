package ch.epfl.tabletlab.photon;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseTwitterUtils;



public class PhotonApplication extends android.app.Application {
  // Debugging switch
  public static final boolean APPDEBUG = false;
  public static int MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL;

  public static int HOUR_TO_KEEP_PHOTO_DISPLAYED = 5;

  // Debugging tag for the application
  public static final String APPTAG = "Photon";

  // Used to pass location from DetailGroupPhotoActivity to PostActivity
  public static final String INTENT_EXTRA_LOCATION = "location";

  // Maximum results returned from a Parse query
  public static int MAX_POST_SEARCH_RESULTS = 20;

  // Maximum post search radius for map in kilometers
  public static int MAX_POST_SEARCH_DISTANCE = 100;

  // Key for saving the search distance preference
  private static final String KEY_SEARCH_DISTANCE = "searchDistance";

  private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;

  private static SharedPreferences preferences;

  private static ConfigHelper configHelper;

  public PhotonApplication() {
  }

  @Override
  public void onCreate() {
    super.onCreate();
    // Required - Initialize the Parse SDK
    Parse.enableLocalDatastore(this);

    Parse.initialize(this, "8Sl49UiakBdglrkaeJNIG4bF74qdApgMR6fS9VRe", "7ID0uhsKi7Syix6joJkXi8R5gfh42cOUWUDRQnSq");

    Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

    ParseFacebookUtils.initialize(this);

    // Optional - If you don't want to allow Twitter login, you can
    // remove this line (and other related ParseTwitterUtils calls)
    ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key),
            getString(R.string.twitter_consumer_secret));

    ParseObject.registerSubclass(PhotonPost.class);


    preferences = getSharedPreferences("com.parse.anywall", Context.MODE_PRIVATE);

    configHelper = new ConfigHelper();
    configHelper.fetchConfigIfNeeded();


  }

  public static float getSearchDistance() {
    return preferences.getFloat(KEY_SEARCH_DISTANCE, DEFAULT_SEARCH_DISTANCE);
  }

  public static ConfigHelper getConfigHelper() {
    return configHelper;
  }

  public static void setSearchDistance(float value) {
    preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).commit();
  }

}
