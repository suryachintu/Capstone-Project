package com.surya.quakealert;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.surya.quakealert.data.QuakeContract;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements
        OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>{

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
    Button mUrl;
    @BindView(R.id.icon_view)
    ImageView mIcon;
    @BindView(R.id.share)
    ImageView mShare;
    @BindView(R.id.empty_fragment_text)
    TextView empty_text;
    @BindView(R.id.content_detail)
    LinearLayout detailContent;
    private LatLng location;
    private GoogleMap map;
    private int mID;
    private String mLink;

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
        updateLocation();
    }

    private void updateLocation() {
        if (map != null && location != null){
            map.clear();
            CameraPosition cp = CameraPosition.builder()
                    .target(location)
                    .bearing(0)
                    .build();
            Circle circle = map.addCircle(new CircleOptions()
                    .center(location)
                    .radius(10000)
                    .fillColor(getResources().getColor(R.color.colorPrimaryDark))
                    .strokeColor(getResources().getColor(R.color.colorAccent)));
            map.setMapType(Utility.getMapType(getActivity()));
            map.addMarker(new MarkerOptions().position(location).title(Utility.getFormattedTitle(mTitle.getText().toString())));
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        }

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
            detailContent.setVisibility(View.VISIBLE);
            mShare.setVisibility(View.VISIBLE);
            empty_text.setVisibility(View.GONE);
            mMagnitude.setText(String.valueOf(data.getDouble(1)));
            mTitle.setText(data.getString(2));
            GradientDrawable magnitudeCircle = (GradientDrawable) mIcon.getBackground();

            // Get the appropriate background color based on the current earthquake magnitude
            int magnitudeColor = Utility.getMagnitudeBg(data.getDouble(1),getContext());

            magnitudeCircle.setColor(magnitudeColor);

            mLink = data.getString(4);

            Date current = new Date(System.currentTimeMillis());
            Date quakeDate = new Date(data.getLong(3));
            mTime.setText(Utility.getFormattedTime(current,quakeDate));
            Double latitude = Math.round(data.getDouble(6) * 100.0) / 100.0;
            Double longitude = Math.round(data.getDouble(7) * 100.0) / 100.0;
            mLatLng.setText(latitude+", "+longitude);
            mCount.setText(getString(R.string.format_count,String.valueOf(data.getInt(5))));
            location = new LatLng(data.getDouble(6), data.getDouble(7));

            String units = Utility.getPreference(getActivity(),getString(R.string.quake_distance_key));

            //get the distance by sending lat, long
            Double distance = Utility.getDistance(getActivity(),latitude,longitude);
            if (distance == 0.0) {
                mDistance.setText(getString(R.string.distance_error));
            }else {
                if (units.equals(getString(R.string.pref_distance_miles_label))) {
                    distance = distance * 0.621371;
                    mDistance.setText(String.valueOf(Math.round(distance)));
                    mDistance.append(getString(R.string.distance_format_miles));
                } else {
                    mDistance.setText(String.valueOf(Math.round(distance)));
                    mDistance.append(getString(R.string.distance_format_km));
                }
            }

            //content descriptions
            mMagnitude.setContentDescription(getString(R.string.a11y_magnitude, mMagnitude.getText()));
            mIcon.setContentDescription(getString(R.string.a11y_magnitude, mMagnitude.getText()));
            mTitle.setContentDescription(getString(R.string.a11y_title,mTitle.getText()));
            mTime.setContentDescription(getString(R.string.a11y_time,mTime.getText()));
            mLatLng.setContentDescription(getString(R.string.a11y_location,mLatLng.getText()));
            mCount.setContentDescription(mCount.getText());
            mUrl.setContentDescription(getString(R.string.a11y_more_btn));
            updateLocation();
        }else {
            detailContent.setVisibility(View.GONE);
            mShare.setVisibility(View.GONE);
            empty_text.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @OnClick(R.id.detail_url)
    public void openLink(){
        //open details in browser
        if (mLink != null){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mLink));
            startActivity(intent);
        }else{
            Toast.makeText(getActivity(), "Link not available", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.share)
    public void shareQuake(){
        //open details in browser
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,"Earthquake\n" + mTitle.getText() + "\n" + "Magnitude : " + mMagnitude.getText() + "\n" + mTime.getText());
        startActivity(intent);
    }
}
