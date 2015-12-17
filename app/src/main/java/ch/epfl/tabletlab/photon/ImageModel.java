package ch.epfl.tabletlab.photon;


import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * Created by Gasp on 15/12/15.
 */
public class ImageModel implements Parcelable,Serializable{ //Serializable Marker without Bitmap

    private String text;
    private String id;
    private String hashtags;
    private String author;
    private String url;

    public ImageModel(MyMarker marker) {
        this.text = marker.getText();
        this.id = marker.getId();
        this.hashtags = marker.getHashtags();
        this.author = marker.getAuthor();
        this.url = marker.getUrl();
    }
    public ImageModel(){};


    protected ImageModel(Parcel in) {
        text = in.readString();
        id = in.readString();
        hashtags = in.readString();
        author = in.readString();
        url = in.readString();
    }

    public static final Creator<ImageModel> CREATOR = new Creator<ImageModel>() {
        @Override
        public ImageModel createFromParcel(Parcel in) {
            return new ImageModel(in);
        }

        @Override
        public ImageModel[] newArray(int size) {
            return new ImageModel[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        dest.writeString(id);
        dest.writeString(hashtags);
        dest.writeString(author);
        dest.writeString(url);
    }
}
