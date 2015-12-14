package ch.epfl.tabletlab.photon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Gasp on 14/12/15.
 */
public class MyMergeMarker extends MyMarker {


    private final HashSet<MyMarker> markers;


    public MyMergeMarker(String label, String icon, Double latitude, Double longitude, Bitmap b, HashSet<MyMarker> markers) {
        super(label, icon, latitude, longitude, b);

        this.markers = markers;


        int size = markers.size();
        double lat = 0.0;
        double lon = 0.0;

        for(MyMarker marker : markers){
            lat += marker.getmLatitude();
            lon += marker.getmLongitude();

        }
        super.setmLatitude(lat / size);
        super.setmLongitude(lon / size);
        Bitmap copyBimap = b.copy(b.getConfig(), true);
        Bitmap bitmampWithNumber = drawNumber(copyBimap,size);
         super.setImage(bitmampWithNumber);

    }

    private Bitmap drawNumber(Bitmap bitmap, int number) {
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //paint.setTextAlign(Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);

        // draw text to the Canvas center
        Rect boundsText = new Rect();
        paint.getTextBounds(String.valueOf(number),
                0, String.valueOf(number).length(),
                boundsText);
        int x = (bitmap.getWidth() - boundsText.width()) / 2;
        int y = (bitmap.getHeight() + boundsText.height()) / 2;

        canvas.drawText(String.valueOf(number), x,
                y, paint);
        return bitmap;
    }

    public HashSet<MyMarker> getMarkers() {
        return markers;
    }
}
