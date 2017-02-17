package com.surya.quakealert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.surya.quakealert.sync.QuakeSyncAdapter.ACTION_DATA_UPDATED;

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

}
