package com.surya.quakealert;

import android.os.Binder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

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

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        unbinder = ButterKnife.bind(this,rootView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mQuakeAdapter = new QuakeAdapter(getActivity());

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(mQuakeAdapter);

        mOkHttpClient = new OkHttpClient();


        try {
            String response = fetchData();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Log.e(TAG,response);

        return rootView;
    }

    private String fetchData() throws IOException {

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

                    Log.e(TAG,response.body().string() + "");
                }
            }
        });

        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
