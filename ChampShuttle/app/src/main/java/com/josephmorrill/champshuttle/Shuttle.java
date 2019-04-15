/* Joseph Morrill, Spencer Martin */
package com.josephmorrill.champshuttle;

import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Joseph on 11/20/2015.
 */
public class Shuttle {
    public String id;
    public String name;
    protected GoogleMap map;
    protected Marker marker = null;
    protected boolean active = false;
    protected Date last_update;
    protected int direction;
    protected double latitude;
    protected double longitude;
    protected double mph;

    public Shuttle(){}

    public Shuttle( String id, String name, GoogleMap map, String last_update, int direction, double latitude, double longitude, int knots ){
        this.id = id;
        this.name = name;
        this.map = map;
        this.direction = direction;
        this.latitude = latitude;
        this.longitude = longitude;
        set_mph( knots );
        set_last_update(last_update);
    }

    @Override
    public String toString(){
        String result = "{ ";
        result += "id : " + this.id + ", ";
        result += "name : '" + this.name + "', ";
        result += "direction : " + this.direction + ", ";
        result += "latitude : " + this.latitude + ", ";
        result += "longitude : " + this.longitude + ", ";
        result += "mph : " + this.mph + ", ";
        result += "updated : " + this.last_update;
        result += " }";
        return result;
    }

    public void update_active(){
        // For now, we'll declare a shuttle active if it's used at all during the day. This will filter out old ones, like the summer shuttle
        // Used code from http://stackoverflow.com/questions/2517709/comparing-two-dates-to-see-if-they-are-in-the-same-day
        Calendar todayCal = Calendar.getInstance();
        Calendar updateCal = Calendar.getInstance();
        todayCal.setTime(new Date());
        updateCal.setTime(this.last_update);
        boolean sameDay = todayCal.get(Calendar.YEAR) == updateCal.get(Calendar.YEAR) &&
                todayCal.get(Calendar.DAY_OF_YEAR) == updateCal.get(Calendar.DAY_OF_YEAR);
        this.active = sameDay;
    }

    public boolean is_active(){
        return this.active;
    }

    public void set_last_update( String last_update ){
        try {
            SimpleDateFormat shuttle_date_format = new SimpleDateFormat( "d/MM/yyyy h:m:s a" );
            this.last_update = shuttle_date_format.parse(last_update);
        }catch(ParseException e){
            // Do a flip
            Log.e("Shuttle", "Error ", e);
        }
    }

    public void set_direction( int direction ){
        this.direction = direction;
    }

    public void set_latitude( double latitude ){
        this.latitude = latitude;
    }

    public void set_longitude( double longitude ){
        this.longitude = longitude;
    }

    public void set_mph( int knots ) {
        this.mph = knots * 1.16;
    }

    public void draw(){
        update_active();

        // Determine if we need to create or move
        if( this.marker != null ){
            // Move
            if( this.active ) {
                this.marker.setRotation(this.direction);
                animate_marker(this.marker, new LatLng(this.latitude, this.longitude), false);
            }else{
                // Remove marker
                this.marker.remove();
                this.marker = null;
            }
        }else{
            // Create
            if( this.active ) {
                try {
                    // Set marker icon
                    int marker_icon = R.drawable.bus_marker_25;
                    if (name.contains("Lakeside")) {
                        marker_icon = R.drawable.bus_lakeside_25;
                    }
                    if (name.contains("Spinner")) {
                        marker_icon = R.drawable.bus_spinner_25;
                    }

                    Marker new_marker = this.map.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude))
                                    .title(id)
                                    .icon(BitmapDescriptorFactory.fromResource(marker_icon))
                    );
                    new_marker.setRotation(this.direction);
                    this.marker = new_marker;
                } catch (Exception e) {
                    Main.make_toast_static("Failed to draw", Toast.LENGTH_SHORT);
                    Log.e("Shuttle", "Error ", e);
                }
            }
        }
    }

    private void animate_marker(final Marker marker, final LatLng toPosition,
                               final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = this.map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
}
