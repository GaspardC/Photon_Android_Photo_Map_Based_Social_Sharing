package ch.epfl.tabletlab.photon;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * Created by Gasp on 14/11/2015.
 */
public class MyMarker implements Parcelable, Serializable

{
    private String text;
    private String mIcon;
    private Double mLatitude;
    private Double mLongitude;
    private Bitmap bitmap;
    private String id;
    private String hashtags;
    private String author;
    private String url;

    public MyMarker(String label, String icon, Double latitude, Double longitude, Bitmap b)
    {
        this.text = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
        this.bitmap = b;
    }


    protected MyMarker(Parcel in) { //Bitmap are not parcelale
        text = in.readString();
        mIcon = in.readString();
        id = in.readString();
        hashtags = in.readString();
        author = in.readString();
        url = in.readString();
    }

    public static final Creator<MyMarker> CREATOR = new Creator<MyMarker>() {
        @Override
        public MyMarker createFromParcel(Parcel in) {
            return new MyMarker(in);
        }

        @Override
        public MyMarker[] newArray(int size) {
            return new MyMarker[size];
        }
    };

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(mIcon);
//        dest.writeParcelable(bitmap, flags);
        dest.writeString(id);
        dest.writeString(hashtags);
        dest.writeString(author);
        dest.writeString(url);
    }
}