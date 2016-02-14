package com.korovkin.yandexfotki;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.korovkin.yandexfotki.model.Entries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public static final String EXTRA_NAME = "name";
    private OkHttpClient client = new OkHttpClient();
    private ArrayList<Entries> entries;
    private ArrayList<String> tags;

    private Spinner spinner;
    private ArrayAdapter adapter;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        activity = this;
        spinner = (Spinner) findViewById(R.id.spinner_nav);

        entries = new ArrayList<>();
        tags = new ArrayList<>();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadMap(tags.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        run(getIntent().getStringExtra(EXTRA_NAME));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void run(String name) {
        Request request = new Request.Builder()
                .url(Constants.API_URL + name + Constants.PHOTOS)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    final Response finalresponse = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MapsActivity.this, finalresponse.message(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                }

                try {

                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = new JSONArray(jsonObject.getJSONArray("entries").toString());

                    tags.add("Все");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        entries.add(new Gson().fromJson(jsonArray.getJSONObject(i).toString(), Entries.class));
                        if (!entries.get(i).tags.isEmpty()) {
                            for (String tag : entries.get(i).tags.keySet()) {
                                tags.add(tag);
                            }
                        }

                        Set set = new HashSet(tags);
                        tags.clear();
                        tags = new ArrayList<String>(set);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, tags);
                        spinner.setAdapter(adapter);
                        loadMap("Все");
                    }
                });
            }
        });
    }


    private void loadMap(String tags) {
        mMap.clear();
        if (tags.contains("Все")) {
            for (final Entries entrie : entries) {
                if (entrie.geo != null) {
                    final double latitude = Double.parseDouble(entrie.geo.coordinates.split(" ")[0]);
                    final double longitude = Double.parseDouble(entrie.geo.coordinates.split(" ")[1]);
                    new addMarkerToMap(entrie, latitude, longitude).execute(entrie.img.m.href);
                }
            }
        } else {
            for (final Entries entrie : entries) {
                if (entrie.tags.keySet().contains(tags) && entrie.geo != null) {
                    final double latitude = Double.parseDouble(entrie.geo.coordinates.split(" ")[0]);
                    final double longitude = Double.parseDouble(entrie.geo.coordinates.split(" ")[1]);
                    new addMarkerToMap(entrie, latitude, longitude).execute(entrie.img.m.href);
                }
            }
        }

    }

    class addMarkerToMap extends AsyncTask<String, Void, Bitmap> {

        private Entries entries;
        private double latitude;
        private double longitude;

        public addMarkerToMap(Entries entries, double latitude, double longitude){
            this.entries = entries;
            this.latitude = latitude;
            this.longitude = longitude;
        }


        @Override
        protected Bitmap doInBackground(String... src) {
            try {
                URL url = new URL(src[0]);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap icon) {
            super.onPostExecute(icon);
            mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title(entries.title)
                            .icon(BitmapDescriptorFactory.fromBitmap(icon))
            );
        }
    }

}
