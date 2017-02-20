package com.surya.quakealert.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.surya.quakealert.MainActivity;
import com.surya.quakealert.R;
import com.surya.quakealert.Utility;
import com.surya.quakealert.data.QuakeContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by Surya on 17-02-2017.
 */

public class QuakeSyncAdapter extends AbstractThreadedSyncAdapter{
    private static final String TAG = QuakeSyncAdapter.class.getSimpleName();
    private static final int SYNC_INTERVAL = 60 * 120;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final String ACTION_DATA_UPDATED = "UPDATE";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({QUAKE_STATUS_OK, QUAKE_SERVER_DOWN, QUAKE_STATUS_SERVER_INVALID, QUAKE_STATUS_INVALID,QUAKE_STATUS_UNKNOWN})
    public @interface QuakeStatus {}

    public static final int QUAKE_STATUS_OK = 0;
    public static final int QUAKE_SERVER_DOWN = 1;
    public static final int QUAKE_STATUS_SERVER_INVALID = 2;
    public static final int QUAKE_STATUS_INVALID = 3;
    public static final int QUAKE_STATUS_UNKNOWN = 4;

    public QuakeSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    public QuakeSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        Log.e(TAG,"Starting OnPerformSync");

        URL url = null;
        try {
            url = new URL(Utility.USGS_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG,"Error in the USGS URL");
        }


        if (url == null)
            return;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        String response = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Read the input stream into string
            inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                //nothing to do
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null){
                //for better readability while debugging
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0){
                //stream was empty
                //show the message to the user
                setLocationStatus(getContext(), QUAKE_SERVER_DOWN);
                return;
            }
            response = buffer.toString();
            parseResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Error while fetching data " + e.getMessage());
            setLocationStatus(getContext(), QUAKE_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(TAG,"Error while fetching data " + e.getMessage());
            setLocationStatus(getContext(), QUAKE_STATUS_SERVER_INVALID);
            e.printStackTrace();
        }finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void parseResponse(String string) throws JSONException {

        JSONObject response = new JSONObject(string);

        JSONArray features = response.getJSONArray("features");

        final String MAGNITUDE = "mag";
        final String PLACE = "place";
        final String TIME = "time";
        final String DETAIL = "url";
        final String FELT = "felt";
        final String current_day = Utility.getCurrentDay();

        final Vector<ContentValues> cvVector = new Vector<>(features.length());
        for (int i = 0; i < features.length(); i++) {

            JSONObject quakeObj = features.getJSONObject(i);

            JSONObject properties = quakeObj.getJSONObject("properties");

            Double mag = 0.0;
            if (!properties.getString(MAGNITUDE).equals("null"))
                mag = properties.getDouble(MAGNITUDE);
            String place = properties.getString(PLACE);
            Long time = properties.getLong(TIME);
            String detail = properties.getString(DETAIL);
            String felt = properties.getString(FELT);

            JSONObject geometry = quakeObj.getJSONObject("geometry");

            JSONArray coordinates = geometry.getJSONArray("coordinates");

            Double lat = coordinates.getDouble(0);
            Double lon = coordinates.getDouble(1);

            ContentValues contentValues = new ContentValues();

            contentValues.put(QuakeContract.QuakeEntry.COLUMN_QUAKE_MAGNITUDE,mag);
            contentValues.put(QuakeContract.QuakeEntry.COLUMN_QUAKE_TITLE,place);
            contentValues.put(QuakeContract.QuakeEntry.COLUMN_QUAKE_TIME,time);
            contentValues.put(QuakeContract.QuakeEntry.COLUMN_QUAKE_DETAIL_URL,detail);
            if (!felt.equals("null"))
                contentValues.put(QuakeContract.QuakeEntry.COLUMN_QUAKE_COUNT,Integer.parseInt(felt));
            else
                contentValues.put(QuakeContract.QuakeEntry.COLUMN_QUAKE_COUNT,0);
            contentValues.put(QuakeContract.QuakeEntry.COLUMN_QUAKE_LAT,lat);
            contentValues.put(QuakeContract.QuakeEntry.COLUMN_QUAKE_LONG,lon);
            contentValues.put(QuakeContract.QuakeEntry.COLUMN_DAY,current_day);
            cvVector.add(contentValues);
        }

        if ( cvVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            int inserted = getContext().getContentResolver().bulkInsert(QuakeContract.QuakeEntry.CONTENT_URI, cvArray);

            //delete the old data
            int del = getContext().getContentResolver().delete(QuakeContract.QuakeEntry.CONTENT_URI,
                                                        QuakeContract.QuakeEntry.COLUMN_DAY + " < ?",
                                                        new String[]{Utility.getCurrentDay()});
            if (del > 0){
                //if old data is cleared clear the notification data also
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = settings.edit();
                editor.putStringSet(getContext().getString(R.string.noti_title_set), new HashSet<String>());
                editor.commit();
            }

            Utility.updateWidgets(getContext());
            sendNotification();
        }
        setLocationStatus(getContext(), QUAKE_STATUS_OK);

    }

    private void sendNotification() {

        Context context = getContext();

        String minMag = Utility.getPreference(context,context.getString(R.string.quake_min_magnitude_key));
        String prefLocation = Utility.getPreference(context,context.getString(R.string.quake_location_key));
        String selection = QuakeContract.QuakeEntry.COLUMN_QUAKE_MAGNITUDE + " >= ? ";
        String sortOrder = QuakeContract.QuakeEntry.COLUMN_QUAKE_MAGNITUDE + " DESC";
        Cursor cursor = context.getContentResolver()
                                    .query(QuakeContract.QuakeEntry.CONTENT_URI,
                                            null,
                                            selection,
                                            new String[]{minMag},
                                            sortOrder);

        int i;
        if (cursor != null){

            if (prefLocation.equals(context.getString(R.string.pref_around_world_value))){
                i = 3;
            }else if(prefLocation.equals(context.getString(R.string.pref_around_my_country_value))){
                i = 2;
            }else{
                i = 1;
            }

            cursor.moveToFirst();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = settings.edit();
            Set<String> storedTitles = settings.getStringSet(context.getString(R.string.noti_title_set), new HashSet<String>());
            if (cursor.getCount() <= 0){
                cursor.close();
                return;
            }
            do {

                Double mag = cursor.getDouble(1);
                String title = Utility.getFormattedTitle(cursor.getString(2));
                String formatted_time = Utility.getFormattedTime(new Date(System.currentTimeMillis()), new Date(cursor.getLong(3)));
                Double distance = Utility.getDistance(context, cursor.getDouble(6), cursor.getDouble(7));
                boolean displayNotification = false;

                if (i == 1) {
                    if (distance < 100.00)
                        displayNotification = true;
                } else if (i == 2) {
                    //send notification if the country matches country in title.
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    String country = prefs.getString(context.getString(R.string.country_name), context.getString(R.string.country_name));
                    if (title.contains(country))
                        displayNotification = true;
                } else {
                    displayNotification = true;
                }


                boolean isDisplayed;
                if (storedTitles.size() == 0 || !storedTitles.contains(title)){
                    isDisplayed = false;
                    storedTitles.add(title);
                    editor.putStringSet(context.getString(R.string.noti_title_set),storedTitles);
                }else {
                    isDisplayed = true;
                }
                editor.commit();
                if (displayNotification && !isDisplayed) {

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setAutoCancel(true)
                                    .setSmallIcon(R.drawable.ic_stat_earthquake_24_512)
                                    .setContentTitle(context.getString(R.string.notification_title,title))
                                    .setContentText("Magnitude : "+ mag + " Time : " + formatted_time);
                    // Creates an explicit intent for an Activity in your app
                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra(context.getString(R.string.quake_extra),cursor.getInt(0));

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    // Adds the back stack for the Intent (but not the Intent itself)
                    stackBuilder.addParentStack(MainActivity.class);
                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    // mId allows you to update the notification later on.
                    mNotificationManager.notify(cursor.getInt(0), mBuilder.build());

                }
            }while (cursor.moveToNext());

            cursor.close();

        }

    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {

        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */

        QuakeSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    static private void setLocationStatus(Context c, @QuakeStatus int locationStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_quake_status_key), locationStatus);
        spe.commit();
    }

}

