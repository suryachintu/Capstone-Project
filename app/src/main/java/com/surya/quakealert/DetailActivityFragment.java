package com.surya.quakealert;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.surya.quakealert.data.QuakeContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.internal.Util;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements
        OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = DetailActivityFragment.class.getSimpleName();
    @BindView(R.id.map)
    MapView mapView;
    private Unbinder unbinder;
    @BindView(R.id.time)
    TextView mTime;
    @BindView(R.id.magnitude)
    TextView mMagnitude;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.latlng)
    TextView mLatLng;
    @BindView(R.id.distance)
    TextView mDistance;
    @BindView(R.id.people_count)
    TextView mCount;
    @BindView(R.id.detail_url)
    TextView mUrl;
    @BindView(R.id.icon_view)
    ImageView mIcon;
    private LatLng location;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private int mID;

    public DetailActivityFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(2, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle args = getArguments();
        if (args != null){
            mID = args.getInt(getString(R.string.quake_extra),0);
        }

        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .build();
        }

        unbinder = ButterKnife.bind(this, rootView);

        // Gets the MapView from the XML layout and creates it
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    private void updateLocation() {
        map.clear();
        CameraPosition cp = CameraPosition.builder()
                .target(location)
                .bearing(0)
                .build();
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.addMarker(new MarkerOptions().position(location).title("Your Location"));
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));

    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        map.clear();
        mapView.onDestroy();
        unbinder.unbind();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = QuakeContract.QuakeEntry._ID + " = ?";
        String selectionArgs = String.valueOf(mID);
        return new CursorLoader(getActivity(),
                QuakeContract.QuakeEntry.CONTENT_URI,
                null,
                selection,
                new String[]{selectionArgs},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {

            mMagnitude.setText(String.valueOf(data.getDouble(1)));
            mTitle.setText(data.getString(2));
            GradientDrawable magnitudeCircle = (GradientDrawable) mIcon.getBackground();

            // Get the appropriate background color based on the current earthquake magnitude
            int magnitudeColor = Utility.getMagnitudeBg(data.getDouble(1),getContext());

            magnitudeCircle.setColor(magnitudeColor);

            SimpleDateFormat sdf_actual = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            Date current = new Date(System.currentTimeMillis());
            Date quakeDate = new Date(data.getLong(3));
            mTime.setText((current.getTime() - quakeDate.getTime()) / (60 * 60 * 1000) % 24
                    + " hrs ago, "
                    + sdf_actual.format(quakeDate));
            mLatLng.setText(Math.round(data.getDouble(6) * 100.0) / 100.0 + "," + Math.round(data.getDouble(7) * 100.0) / 100.0);
            mCount.setText(String.valueOf(data.getInt(5)));
            location = new LatLng(data.getDouble(6), data.getDouble(7));
            updateLocation();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null && location != null) {
            Location dest = new Location("A");
            dest.setLatitude(location.latitude);
            dest.setLongitude(location.longitude);
            double distance = mLastLocation.distanceTo(dest)/1000;
            String units = Utility.getPreference(getActivity(),getString(R.string.quake_distance_key));
            if (units.equals(getString(R.string.pref_distance_miles_label))){
                distance = distance * 0.621371 ;
                mDistance.setText(String.valueOf(Math.round(distance)));
            }else
                mDistance.setText(String.valueOf(Math.round(distance)));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
