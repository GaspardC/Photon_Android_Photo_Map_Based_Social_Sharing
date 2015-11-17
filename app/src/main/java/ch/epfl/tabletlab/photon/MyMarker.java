package ch.epfl.tabletlab.photon;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.widget.ImageView;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Gasp on 14/11/2015.
 */
public class MyMarker
{
    private String mLabel;
    private String mIcon;
    private Double mLatitude;
    private Double mLongitude;
    private Bitmap bitmap;

    public MyMarker(String label, String icon, Double latitude, Double longitude, Bitmap b)
    {
        this.mLabel = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
        this.bitmap = b;
    }

    public String getmLabel()
    {
        return mLabel;
    }

    public void setmLabel(String mLabel)
    {
        this.mLabel = mLabel;
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
}