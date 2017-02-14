package com.surya.quakealert;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.surya.quakealert.data.QuakeContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.surya.quakealert.R.id.latlng;
import static com.surya.quakealert.R.id.map;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements
                    OnMapReadyCallback,LoaderManager.LoaderCallbacks<Cursor>{

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
    private LatLng location;
    private GoogleMap map;

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

        unbinder = ButterKnife.bind(this,rootView);

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
        // Updates the location and zoom of the MapView
        map = googleMap;
//        location = new LatLng(0.0,0.0);
//        updateLocation();
    }

    private void updateLocation() {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location,10);
        map.animateCamera(cameraUpdate);
    }


    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        unbinder.unbind();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = QuakeContract.QuakeEntry._ID + " = ?";
        String selectionArgs = String.valueOf(getActivity().getIntent().getIntExtra(MainActivityFragment.DETAILS,0));
        return new CursorLoader(getActivity(),
                                QuakeContract.QuakeEntry.CONTENT_URI,
                                null,
                                selection,
                                new String[]{selectionArgs},
                                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data!=null && data.moveToFirst()){

            mMagnitude.setText(String.valueOf(data.getDouble(1)));
            mTitle.setText(data.getString(2));
            SimpleDateFormat sdf_actual = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            Date current = new Date(System.currentTimeMillis());
            Date quakeDate = new Date(data.getLong(3));
            mTime.setText((current.getTime() - quakeDate.getTime())/(60*60*1000)%24
                    + " hrs ago, "
                    + sdf_actual.format(quakeDate));
            mLatLng.setText(data.getDouble(6) + ","+  data.getDouble(7));
            location = new LatLng(data.getDouble(6) , data.getDouble(7));
            updateLocation();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
