package com.surya.quakealert.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Vector;

/**
 * Created by Surya on 17-02-2017.
 */

public class QuakeSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = QuakeSyncAdapter.class.getSimpleName();
    private static final int SYNC_INTERVAL = 60 * 120;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

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
                return;
            }
            response = buffer.toString();
            parseResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Error while fetching data " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG,"Error while fetching data " + e.getMessage());
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

        Log.e(TAG,features.length() + "");

        final String MAGNITUDE = "mag";
        final String PLACE = "place";
        final String TIME = "time";
        final String DETAIL = "detail";
        final String FELT = "felt";


        final Vector<ContentValues> cvVector = new Vector<>(features.length());
        for (int i = 0; i < features.length(); i++) {

            JSONObject quakeObj = features.getJSONObject(i);

            JSONObject properties = quakeObj.getJSONObject("properties");

            Double mag = properties.getDouble(MAGNITUDE);
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
            contentValues.put(QuakeContract.QuakeEntry.COLUMN_DAY, Calendar.DATE);
            cvVector.add(contentValues);
        }

        if ( cvVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            int inserted = getContext().getContentResolver().bulkInsert(QuakeContract.QuakeEntry.CONTENT_URI, cvArray);
            Log.e(TAG,"inserted using cp" + inserted);
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
}
