package com.example.park4free;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.park4free.databinding.ActivityMapsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.ClientProtocolException;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.ResponseHandler;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.BasicResponseHandler;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.GenericArrayType;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;


    //Search related
    private RecyclerView recyclerView;
    private List<Address> addressList;
    private SearchView searchView;
    private ItemAdapter itemAdapter;



    FloatingActionButton button;
    BottomNavigationView menuBottom;

    //Values For Future Use
    private final int REFRESH_TIME = 5000; // 5 seconds to update
    private final int REFRESH_DISTANCE = 5; // 5 meters to update
    private final float zoomLevel = 16.0f;

    JSONArray markerArray;
    LocationManager locationManager;
    String latitude, longitude;

    Marker sweden;


    // get adresses and coordinates

    private void filterlist(String s){
        List<Address> filterList = new ArrayList<>();
        String address = s;

        // complete filterlist

        if (!TextUtils.isEmpty(address)){

            Geocoder geocoder = new Geocoder(this);

            try {
                filterList = geocoder.getFromLocationName(address, 10);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /*
        for(PlaceHolderItem item : itemList){
            if(item.getName().toLowerCase().contains(s.toLowerCase())){
                filterList.add(item);
            }
        }

         */

        if(filterList.isEmpty()){
            Toast.makeText(this,"no data",Toast.LENGTH_SHORT).show();
        }
        else{
            itemAdapter.setFilteredList(filterList);
        }

    }

    public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<PlaceHolderItem> items;
        public ItemAdapter(List<Address> items){

            // convert address to item
            if(!items.isEmpty())
            for(Address address : items){
                this.items.add(new PlaceHolderItem(address.getAddressLine(1),address.getLongitude(),address.getLatitude()));
            }
            else{
                this.items = new ArrayList<>();
            }

        }

        public void setFilteredList(List<Address> filteredList){

            if(!items.isEmpty())
                for(Address address : filteredList){
                    this.items.add(new PlaceHolderItem(address.getAddressLine(1),address.getLongitude(),address.getLatitude()));
                }
            notifyDataSetChanged();

        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    // change below class depending on the data format
    public class PlaceHolderItem{

        private String name;
        private double x;
        private double y;

        public PlaceHolderItem(String name, double x, double y){

            this.name = name;
            this.x = x;
            this.y = y;

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchView = findViewById(R.id.searchView1);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterlist(s);
                return false;
            }
        });

        recyclerView = findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressList = new ArrayList<>();

        // below leads to recycleView handling placeHolderItems
        itemAdapter = new ItemAdapter(addressList);
        recyclerView.setAdapter(itemAdapter);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!checking4Permissions()) ask4Permissions();

        menuBottom = findViewById(R.id.bottom_navigation);


        button = findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your handler code here
                //sweden = dropMarker(new LatLng(-34, 145), "Sweden", true, true);
                //Location currentLocation = mMap.getMyLocation();
                //String lat = (String) currentLocation.toString().getLatitude();
                //String longiT = "dc";

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    OnGPS();
                    String serverURL = "http://data.goteborg.se/ParkingService/v2.1/PublicTimeParkings/c9848a32-3248-48e7-a28e-575e2d6d2889?latitude=57.7&longitude=11.9&radius=500&format=JSON";
                    new JSONService().execute(serverURL);
                } else {
                    getLocation();
                    String serverURL = "http://data.goteborg.se/ParkingService/v2.1/PublicTimeParkings/c9848a32-3248-48e7-a28e-575e2d6d2889?latitude=" + latitude + "&longitude=" + longitude + "&radius=2000&format=JSON";
                    new JSONService().execute(serverURL);
                }

                //String serverURL = "http://data.goteborg.se/ParkingService/v2.1/PublicTimeParkings/c9848a32-3248-48e7-a28e-575e2d6d2889?latitude=57.7&longitude=11.9&radius=500&format=JSON";
                //new JSONService().execute(serverURL);
            }
        });

        //if (!checking4Permissions()) ask4Permissions();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //new MyJSONTask().execute();




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
        //Map Properties
        mMap = googleMap;


        //if (!checking4Permissions()) ask4Permissions();

        //buildGoogleApiClient();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setPadding(0,20,50, 450); //----

        sweden = dropMarker(new LatLng(-34, 145), "Sweden", true, true);
        sweden.setTag(0);

        //updateMarker(sweden, new LatLng(57.755170, 12.024750));
        mMap.setOnMarkerClickListener(this);



    }


    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
            //removeMarker(sweden);
            updateMarker(sweden, new LatLng(57.755170, 12.024750));
            //sweden.setPosition(new LatLng(57.755170, 12.024750));
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }


    //Checking, Asking and Setting Permissions
    private boolean checking4Permissions () {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }
    private void ask4Permissions () {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                1);

        if (!checking4Permissions()) {
            Toast.makeText(this, "You cannot use the app without granting the necessary permissions!", Toast.LENGTH_SHORT).show();
            openAppSettings();
        }
    }
    public void openAppSettings() {

        Uri packageUri = Uri.fromParts( "package", getApplicationContext().getPackageName(), null );

        Intent applicationDetailsSettingsIntent = new Intent();

        applicationDetailsSettingsIntent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
        applicationDetailsSettingsIntent.setData( packageUri );
        applicationDetailsSettingsIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

        getApplicationContext().startActivity( applicationDetailsSettingsIntent );
    }

    //Dropping, Removing and Updating Markers
    private Marker dropMarker (LatLng pos, String markerText, Boolean camera, boolean zoom) {
        MarkerOptions marker = new MarkerOptions().position(pos).draggable(true).title(markerText);
        Marker theMarker  = mMap.addMarker(marker);

        if (camera) {
            if (zoom)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoomLevel));
            else
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        }
        return theMarker;
    }
    private void removeMarker(Marker marker) {
        marker.remove();
    }
    private Marker updateMarker(Marker marker, LatLng newPos) {
        marker.setPosition(newPos);
        return marker;
    }

    public void addMarkers(GoogleMap mMap) {

        if (markerArray!=null) {
            try {

                for (int i = 0; i < markerArray.length(); i++) {
                    JSONObject jsonObject = markerArray.getJSONObject(i);
                    double lat = jsonObject.getDouble("Lat");
                    double lng = jsonObject.getLong("Long");
                    Log.i("Test", "Hello 123456");
                    Log.i("Latitude", lat + "");
                    Log.i("Longitude", lng + "");
                    LatLng parkeringGbg = new LatLng(lat, lng);
                    MarkerOptions mk = new MarkerOptions().position(parkeringGbg).title("Parking!");
                    mMap.addMarker(mk);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void OnGPS() {startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));}
    //Testing to comment for future commits
    private void getLocation() {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (locationGPS != null) {
            double lat = locationGPS.getLatitude();
            double longi = locationGPS.getLongitude();
            latitude = String.valueOf(lat);
            longitude = String.valueOf(longi);
        } else {
            Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
        }
    }


    private class JSONService extends AsyncTask<String, Void, Void> {

        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        private String Error = null;
        private final String TAG = null;
        String name = null;
        private ProgressDialog Dialog = new ProgressDialog(MapsActivity.this);


        protected void onPreExecute() {
            Dialog.setMessage("Loading Nearby Locations");
            Dialog.show();
        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {
            try {

                HttpGet httpget = new HttpGet(urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Content = Client.execute(httpget, responseHandler);

            } catch (ClientProtocolException e) {
                Error = e.getMessage();
                cancel(true);
            } catch (IOException e) {
                Error = e.getMessage();
                cancel(true);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            // Close progress dialog
            Dialog.dismiss();
            Log.e(TAG, "------------------------------------- Output: " + Content);


            try {
                JSONArray jsonArray =new JSONArray(Content.toString());

                for(int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String nameTitle = jsonObject.getString("Name");
                    double lat = jsonObject.getDouble("Lat");
                    double lng = jsonObject.getDouble("Long");
                    LatLng pos = new LatLng(lat, lng);

                    Log.i("Test", "Hello 123456");
                    Log.i("Latitude", lat + "");
                    Log.i("Longitude", lng + "");

                    LatLng parkeringGbg = new LatLng(lat, lng);
                    MarkerOptions mk = new MarkerOptions().position(parkeringGbg).title(nameTitle);
                    //dropMarker(pos,nameTitle, true,true);
                    mMap.addMarker(mk);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 16.0f));

                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("EXCEPTION   ","");
            }
        }



    }
}
