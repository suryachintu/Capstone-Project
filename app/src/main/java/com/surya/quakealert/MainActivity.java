package com.surya.quakealert;

import android.Manifest;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.surya.quakealert.sync.QuakeSyncAdapter;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.QuakeClickListener {

    private static final int REQUEST_CODE = 200;
    private static final String DF_TAG = "DetailFragment";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        QuakeSyncAdapter.initializeSyncAdapter(this);

        //Ask for location permissions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            }
        }

        if (findViewById(R.id.quake_detail_container) != null){

            mTwoPane = true;

            if (savedInstanceState == null){

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.quake_detail_container,new DetailActivityFragment(),DF_TAG)
                        .commit();

            }

        }else {
            mTwoPane = false;
        }


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
            }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)){
                    //Show an explanation to the user *asynchronously*
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
                }else {
                    //Never ask again and handle your app without permission.
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        new Pair<View, String>(vh.mIcon, vh.mIcon.getTransitionName()),new Pair<View, String>(vh.mMagnitude,vh.mMagnitude.getTransitionName()));
                ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
            }
            else
             startActivity(intent);
        }

    }
}
