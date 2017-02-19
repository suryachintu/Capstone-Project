package com.surya.quakealert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.surya.quakealert.sync.QuakeSyncAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.surya.quakealert.sync.QuakeSyncAdapter.ACTION_DATA_UPDATED;
import static java.security.AccessController.getContext;

/**
 * Created by Surya on 13-02-2017.
 */

public class Utility {

    public static final String USGS_URL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";


    public static String getPreference(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (key.equals(context.getString(R.string.quake_order_by_key))){
            return prefs.getString(key,context.getString(R.string.pref_sort_mag_four_value));
        }
        else if (key.equals(context.getString(R.string.quake_min_magnitude_key))){
            return prefs.getString(key,context.getString(R.string.pref_min_mag_six_label));
        }
        else if (key.equals(context.getString(R.string.quake_location_key))){
            return prefs.getString(key,context.getString(R.string.pref_around_world_value));
        }else if (key.equals(context.getString(R.string.quake_distance_key))){
            return prefs.getString(key,context.getString(R.string.pref_distance_km_label));
        }else if (key.equals(context.getString(R.string.current_distance))){
            return prefs.getString(key,context.getString(R.string.current_distance));
        }else if (key.equals(context.getString(R.string.country_name))){
            return prefs.getString(key,context.getString(R.string.country_name));
        }else{
            return prefs.getString(key,context.getString(R.string.pref_map_type_roadmap_label));
        }
    }


    public static void updateWidgets(Context context) {
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    public static String getCurrentDay() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.ENGLISH);

        return sdf.format(System.currentTimeMillis());
    }

    public static int getMagnitudeBg(double aDouble,Context context) {

        int magnitudeColorResourceId;
        int magnitudeFloor = (int) Math.floor(aDouble);
        switch (magnitudeFloor) {
            case 0:
            case 1:
                magnitudeColorResourceId = R.color.magnitude1;
                break;
            case 2:
                magnitudeColorResourceId = R.color.magnitude2;
                break;
            case 3:
                magnitudeColorResourceId = R.color.magnitude3;
                break;
            case 4:
                magnitudeColorResourceId = R.color.magnitude4;
                break;
            case 5:
                magnitudeColorResourceId = R.color.magnitude5;
                break;
            case 6:
                magnitudeColorResourceId = R.color.magnitude6;
                break;
            case 7:
                magnitudeColorResourceId = R.color.magnitude7;
                break;
            case 8:
                magnitudeColorResourceId = R.color.magnitude8;
                break;
            case 9:
                magnitudeColorResourceId = R.color.magnitude9;
                break;
            default:
                magnitudeColorResourceId = R.color.magnitude10plus;
                break;
        }
        return ContextCompat.getColor(context, magnitudeColorResourceId);
    }

    public static String getFormattedTime(Date current, Date quakeDate) {

        SimpleDateFormat sdf_actual = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

        return (current.getTime() - quakeDate.getTime()) / (60 * 60 * 1000) % 24
                + " hrs ago, "
                + sdf_actual.format(quakeDate);
    }

    public static Double getDistance(Context context, double lat, double lng) {


        //set the distance
        Location dest = new Location("A");
        dest.setLatitude(lat);
        dest.setLongitude(lng);
        Location mSource = getLocation(context);
        if (mSource == null)
            return 0.0;
        else
            return (double) (mSource.distanceTo(dest)/1000);

    }

    private static Location getLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Double sLat = (double) prefs.getFloat(context.getString(R.string.location_latitude), 0);
        Double sLng = (double) prefs.getFloat(context.getString(R.string.location_longitude), 0);
        if (sLat == 0.0 && sLng == 0.0 )
            return null;
        Location mSource = new Location("B");
        mSource.setLatitude(sLat);
        mSource.setLongitude(sLng);
        return mSource;
    }


    public static String getFormattedTitle(String title) {

        String[] place = title.split("of");
        if (place.length > 1)
            return place[1];
        else
            return place[0];
    }

    public static int getMapType(Context context) {

        String mapType = getPreference(context,context.getString(R.string.quake_map_type_key));

        if (mapType.equals(context.getString(R.string.pref_map_type_roadmap_label))){
            return GoogleMap.MAP_TYPE_NORMAL;
        }else if(mapType.equals(context.getString(R.string.pref_map_type_satellite_label))){
            return GoogleMap.MAP_TYPE_SATELLITE;
        }else if(mapType.equals(context.getString(R.string.pref_map_type_terrian_label))){
            return GoogleMap.MAP_TYPE_TERRAIN;
        }else{
            return GoogleMap.MAP_TYPE_HYBRID;
        }
    }

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }


    @SuppressWarnings("ResourceType")
    static public @QuakeSyncAdapter.QuakeStatus
    int getLocationStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_quake_status_key), QuakeSyncAdapter.QUAKE_STATUS_UNKNOWN);
    }


}
