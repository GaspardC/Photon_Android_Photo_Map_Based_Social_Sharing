package ch.epfl.tabletlab.photon;

import android.graphics.Bitmap;

import org.json.JSONArray;

/**
 * Created by Gasp on 14/11/2015.
 */
public class MyMarker

{
    private String text;
    private String mIcon;
    private Double mLatitude;
    private Double mLongitude;
    private Bitmap bitmap;
    private String id;
    private String hashtags;
    private String author;

    public MyMarker(String label, String icon, Double latitude, Double longitude, Bitmap b)
    {
        this.text = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
        this.bitmap = b;
    }


    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
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
    public void setImage(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHashtags() {
        return hashtags;
    }

    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}