package com.example.park4free;

import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.park4free.databinding.ActivityMapsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    //get last known location of the user

    public void addMarkers(GoogleMap mMap){
        String line;
        StringBuffer response = new StringBuffer();
        HttpURLConnection connection;

        try {
            URL source = new URL("http://data.goteborg.se/ParkingService/v2.1/PublicTimeParkings/c9848a32-3248-48e7-a28e-575e2d6d2889?latitude=57.7&longitude=11.9&radius=500&format=JSON");
            connection = (HttpURLConnection) source.openConnection();
            //Request setup
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            System.out.println(status);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line=bufferedReader.readLine()) != null){
                response.append(line);
            }
            bufferedReader.close();
            System.out.println(response.toString());
            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i=0; i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                double lat = jsonObject.getDouble("Lat");
                double lng = jsonObject.getLong("Long");
                Log.i("Test","Hello 123456");
                Log.i("Latitude",lat+"");
                Log.i("Longitude",lng+"");
                LatLng parkeringGbg = new LatLng(lat, lng);
                MarkerOptions mk = new MarkerOptions().position(parkeringGbg).title("Marker in Sydney");
                mMap.addMarker(mk);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            //connection.disconnect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        // Add a marker in Sydney and move the camera

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        LatLng australia = new LatLng(-30, 145);
        mMap.addMarker(new MarkerOptions().position(australia).title("Marker in sweden"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sweden));

        addMarkers(mMap);





    }
}