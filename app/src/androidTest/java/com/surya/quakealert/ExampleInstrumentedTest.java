package com.surya.quakealert;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.surya.quakealert.data.QuakeContract;
import com.surya.quakealert.sync.QuakeSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.android.gms.internal.zzs.TAG;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.surya.quakealert", appContext.getPackageName());

    }

    @Test
    public void fetchData(){

        Request request = new Request.Builder()
                .url(Utility.USGS_URL)
                .build();

        Response response = null;

        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                assertTrue("Error IO Exception on making OkHttp call", false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()){

                    try {
                        JSONObject res = new JSONObject(response.body().string());

                        JSONArray features = res.getJSONArray("features");

                        final String MAGNITUDE = "mag";
                        final String PLACE = "place";
                        final String TIME = "time";
                        final String DETAIL = "url";
                        final String FELT = "felt";

                        for (int i = 0; i < features.length(); i++) {

                            JSONObject quakeObj = features.getJSONObject(i);

                            JSONObject properties = quakeObj.getJSONObject("properties");

                            Double mag = 0.0;
                            if (!properties.getString(MAGNITUDE).equals("null"))
                                mag = properties.getDouble(MAGNITUDE);
                            else {
                                assertTrue("Magnitude was null while parsing the response", false);
                            }
                            String place = properties.getString(PLACE);
                            Long time = properties.getLong(TIME);
                            String detail = properties.getString(DETAIL);
                            String felt = properties.getString(FELT);

                            JSONObject geometry = quakeObj.getJSONObject("geometry");

                            if (geometry != null)
                                assertTrue("JSON object geometry doesn't exist ", false);
                            if (felt.equals("null"))
                                assertTrue("Error People count was null file parsing json", false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        assertTrue("Error : JSON Exception on parsing the response", false);
                    }
                }else {
                    assertTrue("Error : Response was not successful by OkHttp call", false);
                }
            }
        });

    }

}
