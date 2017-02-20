package com.surya.quakealert;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.surya.quakealert.sync.QuakeSyncAdapter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        MainActivityFragment.QuakeClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE = 200;
    private static final String DF_TAG = "DetailFragment";
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .build();
        }

        QuakeSyncAdapter.initializeSyncAdapter(this);

        if (findViewById(R.id.quake_detail_container) != null) {

            mTwoPane = true;

            if (savedInstanceState == null) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.quake_detail_container, new DetailActivityFragment(), DF_TAG)
                        .commit();

            }

        } else {
            mTwoPane = false;
        }

        //to launch by clicking notification
        if (getIntent().getExtras() != null) {
            int position = getIntent().getExtras().getInt(getString(R.string.quake_extra), -1);
            if (position != -1) {
                OnItemClick(position, null);
            }
        }


        //Ask for location permissions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            }
        }


    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }else {
            if (mGoogleApiClient == null)
                return;
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                boolean isMyLoc = prefs.getBoolean(getString(R.string.notification_around_my_location_key), false);
                editor.putFloat(getString(R.string.location_latitude), (float) mLastLocation.getLatitude());
                editor.putFloat(getString(R.string.location_longitude), (float) mLastLocation.getLongitude());

                if (isMyLoc) {

                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                        if (addresses.size() > 0) {
                            Address obj = addresses.get(0);
                            editor.putString(getString(R.string.country_name), obj.getCountryName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                editor.apply();

            }
        }
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //permission granted
                SharedPreferences preferences = getSharedPreferences(getString(R.string.PREF_PERMISSION),MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(getString(R.string.PREF_PERMISSION),true);
                editor.apply();
                fetchLocation();
            }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)){
                    //Show an explanation to the user *asynchronously*
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
                }else {
                    //Never ask again and handle your app without permission.
                    Log.e(TAG,"Location permissions disabled");
                }
            }

        }
    }

    @Override
    public void OnItemClick(int position, QuakeAdapter.QuakeViewHolder vh) {

        if (mTwoPane){

            Bundle bundle = new Bundle();

            bundle.putInt(getString(R.string.quake_extra),position);

            DetailActivityFragment fragment = new DetailActivityFragment();

            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.quake_detail_container,fragment).commit();

        }else {

            Intent intent = new Intent(this, DetailActivity.class);

            intent.putExtra(getString(R.string.quake_extra), position);

            ActivityOptionsCompat activityOptions =
                    null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && vh!=null) {
                activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        new Pair<View, String>(vh.mIcon, vh.mIcon.getTransitionName()),new Pair<View, String>(vh.mMagnitude,vh.mMagnitude.getTransitionName()));
                ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
            }
            else
             startActivity(intent);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG,"Api connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG,"Api connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"Api failed");
    }
}
