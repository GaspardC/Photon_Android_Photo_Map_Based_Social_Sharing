package ch.epfl.tabletlab.photon;

import android.graphics.Bitmap;

/**
 * Created by Gasp on 14/11/2015.
 */
public class MyMarker

{
    private String mHashtag;
    private String mIcon;
    private Double mLatitude;
    private Double mLongitude;
    private Bitmap bitmap;
    private String id;

    public MyMarker(String label, String icon, Double latitude, Double longitude, Bitmap b)
    {
        this.mHashtag = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
        this.bitmap = b;
    }


    public String getmHashtag()
    {
        return mHashtag;
    }

    public void setmHashtag(String mHashtag)
    {
        this.mHashtag = mHashtag;
    }

    public String getmIcon()
    {
        return mIcon;
    }

    public void setmIcon(String icon)
    {
        this.mIcon = icon;
    }

    public Double getmLatitude()
    {
        return mLatitude;
    }

    public void setmLatitude(Double mLatitude)
    {
        this.mLatitude = mLatitude;
    }

    public Double getmLongitude()
    {
        return mLongitude;
    }

    public void setmLongitude(Double mLongitude)
    {
        this.mLongitude = mLongitude;
    }


    public Bitmap getImage() {
        return this.bitmap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}