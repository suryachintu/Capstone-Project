package com.surya.quakealert;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.surya.quakealert.data.QuakeContract;
import com.surya.quakealert.sync.QuakeSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.surya.quakealert.R.id.map;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements
        QuakeAdapter.ListItemClickListener,LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private Unbinder unbinder;
    @BindView(R.id.quake_recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.error_message)
    TextView tv;
    private QuakeAdapter mQuakeAdapter;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({QUAKE_STATUS_OK, QUAKE_SERVER_DOWN, QUAKE_STATUS_SERVER_INVALID, QUAKE_STATUS_INVALID})
    public @interface QuakeStatus {}

    public static final int QUAKE_STATUS_OK = 0;
    public static final int QUAKE_SERVER_DOWN = 1;
    public static final int QUAKE_STATUS_SERVER_INVALID = 2;
    public static final int QUAKE_STATUS_INVALID = 3;


    public interface QuakeClickListener{

        void OnItemClick(int position, QuakeAdapter.QuakeViewHolder holder);

    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(1, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        unbinder = ButterKnife.bind(this,rootView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mQuakeAdapter = new QuakeAdapter(getActivity(),null,this);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(mQuakeAdapter);


        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = QuakeContract.QuakeEntry.COLUMN_QUAKE_MAGNITUDE + " >= ?";
        String sortOrder = QuakeContract.QuakeEntry.COLUMN_QUAKE_MAGNITUDE + " DESC";

        return new CursorLoader(getActivity(),
                                QuakeContract.QuakeEntry.CONTENT_URI,
                                null,
                                selection,
                                new String[]{Utility.getPreference(getActivity(),getString(R.string.quake_order_by_key))},
                                sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateEmptyView();
        if (data.getCount() > 0)
            tv.setVisibility(View.GONE);
        else
            tv.setVisibility(View.VISIBLE);
        mQuakeAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mQuakeAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(int position, QuakeAdapter.QuakeViewHolder holder) {
        Cursor cursor = mQuakeAdapter.getCursor();
        Log.e(TAG,"clicked");
        cursor.moveToPosition(position);
        ((QuakeClickListener)getActivity()).OnItemClick(cursor.getInt(0),holder);
    }

    private void updateEmptyView() {
        if ( mQuakeAdapter.getItemCount() == 0 ) {

            if ( null != tv ) {

                tv.setVisibility(View.VISIBLE);
                int message = R.string.empty_quake_list;
                @QuakeSyncAdapter.QuakeStatus int location = Utility.getLocationStatus(getActivity());
                switch (location) {
                    case QuakeSyncAdapter.QUAKE_SERVER_DOWN:
                        message = R.string.empty_quake_list_server_down;
                        break;
                    case QuakeSyncAdapter.QUAKE_STATUS_SERVER_INVALID:
                        message = R.string.empty_quake_list_server_error;
                        break;
                    case QuakeSyncAdapter.QUAKE_STATUS_INVALID:
                        message = R.string.empty_quake_list;
                        break;
                    default:
                        if (!Utility.isNetworkAvailable(getActivity())) {
                            message = R.string.empty_quake_list_no_network;
                        }
                }
                tv.setText(message);
            }
        }
    }
}
