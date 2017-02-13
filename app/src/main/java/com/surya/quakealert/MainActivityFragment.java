package com.surya.quakealert;

import android.os.Binder;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private Unbinder unbinder;
    @BindView(R.id.quake_recyclerView)
    RecyclerView mRecyclerView;
    private QuakeAdapter mQuakeAdapter;
    private OkHttpClient mOkHttpClient;
    private ArrayList<QuakeModel> quakeModels;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        unbinder = ButterKnife.bind(this,rootView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        quakeModels = new ArrayList<>();
        mQuakeAdapter = new QuakeAdapter(getActivity(),quakeModels);

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

        for (int i = 0; i < features.length(); i++) {

            JSONObject quakeObj = features.getJSONObject(i);

            JSONObject properties = quakeObj.getJSONObject("properties");

            Double mag = properties.getDouble(MAGNITUDE);
            String place = properties.getString(PLACE);
            Long time = properties.getLong(TIME);
            String detail = properties.getString(DETAIL);
            String felt = properties.getString(FELT);

            quakeModels.add(new QuakeModel(mag,place,time,detail,felt));
            Log.e(TAG,mag+"\n"+place+"\n"+time+"\n"+detail+"\n"+felt+"\n");
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mQuakeAdapter.notifyDataSetChanged();
            }
        });

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        quakeModels.clear();
    }
}
