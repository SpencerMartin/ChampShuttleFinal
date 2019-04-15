/* Joseph Morrill, Spencer Martin */
package com.josephmorrill.champshuttle;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class Main extends AppCompatActivity {
    private static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public static ArrayList<Shuttle> shuttles = new ArrayList<Shuttle>();
    public static Context context;
    public static int zoom = 13;
    public static double campus_latitude = 44.473889,
            campus_longitude = -73.204552;
    public static int auto_refresh_seconds = 5;
    private static Handler auto_refresh_handler;
    private Runnable auto_refresh_runnable = new Runnable() {
        @Override
        public void run() {
            refresh();

            auto_refresh_handler.postDelayed(this, auto_refresh_seconds*1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Main.context = getApplicationContext();
        setUpMapIfNeeded();
        try {
            getSupportActionBar().show();
        }catch(NullPointerException e){

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem shareBtn = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareBtn);
        // Attach an intent to this ShareActionProvider.
        if (mShareActionProvider != null)
        {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
        else
        {
            Log.d("MainActivity", "Share Action Provider is null");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_home_refresh) {
            make_toast( "Refreshing", Toast.LENGTH_SHORT );
            refresh();
            return true;
        }else if( id == R.id.menu_home_recenter){
            recenter();
            return true;
        }else if( id == R.id.menu_home_shuttle_list ){
            launch_shuttle_list();
            return true;
        }else if( id == R.id.menu_home_shuttle_schedule ){
            launch_shuttle_schedule();
            return true;
        }else if (id == R.id.menu_home_send)
        {
            createEmailFromScheduleDocs();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    private void setUpMap() {

        // Add marker for campus
        draw_marker( campus_latitude, campus_longitude, "Main campus", R.drawable.stop_champlain_25 );

        // Add marker for Spinner
        draw_marker( 44.490692, -73.184864, "Spinner Place", R.drawable.stop_general_25 );

        // Add marker for Lakeside
        draw_marker( 44.460960, -73.215970, "Lakeside", R.drawable.stop_general_25 );

        // Center map to campus
        recenter();

        // Start polling for buses
        auto_refresh_handler = new Handler();
        auto_refresh_handler.postDelayed(auto_refresh_runnable, auto_refresh_seconds * 1000);
    }

    public static void draw_marker( double latitude, double longitude, String title, int drawable_pointer ){
        Main.mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromResource(drawable_pointer))
        );
    }

    private class shuttle_loader extends AsyncTask<Void, Void, String> {
        public shuttle_loader() {}

        //Must implement this method
        protected String doInBackground(Void... params)
        {
            Log.i("Main", "Loading shuttles...");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String result = null; //Contain the raw data from URL
            try
            {
                //Setup connection to movie database
                URL url = new URL(getString(R.string.data_url));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream
                InputStream inputStream = urlConnection.getInputStream();

                //Place input stream into a buffered reader
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null)
                {
                    buffer.append(line + "\n");
                }

                //Create data from URL
                result = buffer.toString();
            }
            catch (IOException e)
            {
                Log.e("Main", "Error ", e);
            }
            finally
            {
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (final IOException e)
                    {
                        Log.e("Main", "Error closing stream", e);
                    }
                }
                return result;
            }
        }
        @Override
        protected void onPostExecute(String result)
        {
            if( result != null ){
                draw_shuttles(result);
            }else{
                make_toast( "Failed to load shuttle data", Toast.LENGTH_SHORT );
            }
        }
    }

    public void make_toast( String message, int duration ){
        Context context = getApplicationContext();

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    public static void make_toast_static( String message, int duration ){
        Toast toast = Toast.makeText(Main.context, message, duration);
        toast.show();
    }

    public void draw_shuttles(String json){
        /* Sample shuttle object
            {
                "Date_Time": "09\/09\/2015 8:09:56 AM",
                "Date_Time_ISO": "2015-09-09T08:09:56.0000000-04:00",
                "UnitID": "332",
                "Unit_Name": "Lakeside 332",
                "Unit_Operator": "None",
                "Lat": "44.609177",
                "Lon": "-73.1575855",
                "Knots": "0",
                "Direction": "102"
            }
         */
        JSONArray shuttles_json = null;
        try{
            shuttles_json = new JSONArray( json );
        }catch(JSONException e){
            make_toast( "Error parsing shuttle data", Toast.LENGTH_SHORT );
        }

        int shuttle_cache_index = -1;
        if( shuttles_json != null ){
            for( int i = 0; i < shuttles_json.length(); i++ ){
                JSONObject shuttle_json = null;
                try{
                    shuttle_json = shuttles_json.getJSONObject(i);
                }catch(JSONException e){
                    // Do a flip
                }
                // Check if data object already exists for JSON shuttle in Main's shuttle array
                shuttle_cache_index = -1;
                for( int j = 0; j < Main.shuttles.size(); j++ ){
                    try {
                        if (Main.shuttles.get(j).id.equals( shuttle_json.getString("UnitID") )) {
                            shuttle_cache_index = j;
                            break;
                        }
                    }catch(Exception e){
                        make_toast( "Error trying to determine shuttle ID", Toast.LENGTH_SHORT );
                    }
                }

                // If found, update appearance status and location. Otherwise, create.
                Shuttle shuttle;
                if( shuttle_cache_index > -1 ){
                    // Found
                    shuttle = Main.shuttles.get( shuttle_cache_index );
                    try {
                        shuttle.set_last_update(shuttle_json.getString("Date_Time"));
                        shuttle.set_direction(shuttle_json.getInt("Direction"));
                        shuttle.set_latitude(shuttle_json.getDouble("Lat"));
                        shuttle.set_longitude(shuttle_json.getDouble("Lon"));
                        shuttle.set_mph(shuttle_json.getInt("Knots"));
                        shuttle.draw();
                        Main.shuttles.set( shuttle_cache_index, shuttle );
                    }catch(JSONException e){
                        // Do a flip
                        Log.e("Main", "Error ", e);
                    }
                }else{
                    // Not found
                    String id = "", name = "", last_update = "";
                    int direction = 0, knots = 0;
                    double latitude = 0, longitude = 0;
                    try {
                        id = shuttle_json.getString("UnitID");
                        name = shuttle_json.getString("Unit_Name");
                        last_update = shuttle_json.getString("Date_Time");
                        direction = shuttle_json.getInt("Direction");
                        latitude = shuttle_json.getDouble("Lat");
                        longitude = shuttle_json.getDouble("Lon");
                        knots = shuttle_json.getInt("Knots");
                    } catch (JSONException e) {
                        // Do a flip
                        Log.e("Main", "Error ", e);
                    }
                    shuttle = new Shuttle(id, name, mMap, last_update, direction, latitude, longitude, knots);
                    shuttle.draw();
                    Main.shuttles.add(shuttle);
                }
            }
        }
    }

    public void refresh(){
        // Create a Connectivity Manager
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Check Network State
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Is network connectivity available?
        if( networkInfo != null && networkInfo.isConnected() ){
            new shuttle_loader().execute();
        } else {
            make_toast("Network connection is unavailable", Toast.LENGTH_LONG);
        }
    }

    public void recenter(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(campus_latitude, campus_longitude), zoom));
    }

    public void launch_shuttle_list(){
        Context c = getApplicationContext();
        Intent i = new Intent(c, ShuttleList.class);
        startActivity(i);
    }

    public void launch_shuttle_schedule(){
        Context c = getApplicationContext();
        Intent i = new Intent(c, ShuttleSchedule.class);
        startActivity(i);
    }

    public void createEmailFromScheduleDocs(){
        // Uses code from http://stackoverflow.com/questions/12405892/attach-pdf-file-to-email-from-app
        String subject = "Champlain Shuttle Schedules";
        String body = "Attached are the schedules for Champlain College's transit shuttles as of " + getString(R.string.schedule_when);

        File lakesideSchedule = context.getExternalFilesDir(getString(R.string.schedule_lakeside)).getAbsoluteFile();
        File spinnerSchedule = context.getExternalFilesDir(getString(R.string.schedule_spinner)).getAbsoluteFile();

        ArrayList<Uri> attachments = new ArrayList<Uri>();
        attachments.add(Uri.fromFile(lakesideSchedule));
        attachments.add(Uri.fromFile(spinnerSchedule));

        Intent email = new Intent(Intent.ACTION_SEND_MULTIPLE);
        email.setType("text/plain");
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, body);
        email.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);

        startActivity(Intent.createChooser(email, "Choose an email client"));
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,getString(R.string.homepage_url));
        if (shareIntent.resolveActivity(getPackageManager()) == null){
            Context context = getApplicationContext();
            String text = "No app available for sharing";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        return shareIntent;
    }

    public void test(){
        for( int i = 0; i < shuttles.size(); i++ ){
            Log.e( "Main", shuttles.get(i).toString() );
        }
    }
}
