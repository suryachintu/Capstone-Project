package com.surya.quakealert;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.os.Bundle;
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

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.surya.quakealert.data.QuakeContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
    public static final String DETAILS = "details";
    private Unbinder unbinder;
    @BindView(R.id.quake_recyclerView)
    RecyclerView mRecyclerView;
    private QuakeAdapter mQuakeAdapter;
    private OkHttpClient mOkHttpClient;

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

        mOkHttpClient = new OkHttpClient();

        try {
            fetchData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootView;
    }

    private void fetchData() throws IOException {

        Request request = new Request.Builder()
                                .url(Utility.USGS_URL)
                                .build();

        Response response = null;

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"On Failure OkHttp " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()){

                    try {
                        parseResponse(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

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

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( cvVector.size() > 0 ) {
                    ContentValues[] cvArray = new ContentValues[cvVector.size()];
                    cvVector.toArray(cvArray);
                    int inserted = getContext().getContentResolver().bulkInsert(QuakeContract.QuakeEntry.CONTENT_URI, cvArray);
                    Log.e(TAG,"inserted using cp" + inserted);
                }
                mQuakeAdapter.notifyDataSetChanged();
            }
        });

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
        Log.e(TAG,"oncreate loader");
        return new CursorLoader(getActivity(),
                                QuakeContract.QuakeEntry.CONTENT_URI,
                                null,
                                selection,
                                new String[]{Utility.getPreference(getActivity(),getString(R.string.quake_order_by_key))},
                                sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mQuakeAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mQuakeAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(int position) {

        Cursor cursor = mQuakeAdapter.getCursor();
        cursor.moveToPosition(position);
        Intent intent = new Intent(getActivity(),DetailActivity.class);
        intent.putExtra(DETAILS,cursor.getInt(0));
        startActivity(intent);

    }
}
