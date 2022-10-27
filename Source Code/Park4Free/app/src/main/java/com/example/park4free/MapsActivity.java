package com.example.park4free;

import static java.lang.Double.parseDouble;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.SearchView;
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

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


public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, MyRecyclerViewAdapter.ItemClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    //test

    //Search related
    private RecyclerView recyclerView;
    private List<Address> addressList;
    private SearchView searchView;
    private MyRecyclerViewAdapter adapter;

    private ArrayList<String> addresses1;
    private ArrayList<String> addresses2;



    //Used for clustering
    private ClusterManager<MyItem> clusterManager;

    FloatingActionButton button;
    BottomNavigationView menuBottom;
    Switch switchButton;

    //Values For Future Use
    private final int REFRESH_TIME = 5000; // 5 seconds to update
    private final int REFRESH_DISTANCE = 5; // 5 meters to update
    private final float zoomLevel = 16.0f;
    private boolean CLICKABLE = false;


    private JSONArray markerArray;
    private LocationManager locationManager;
    private String latitude, longitude;
    private double cameralat, cameralong;
    private final int radius = 2000;
    private final int radius1 = 500;

    Marker sweden;

    /**
     * This class is used to give the map markers custom parking icons and a custom info box
     */
    private class ParkingRenderer extends DefaultClusterRenderer<MyItem>{

        //private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        //private final ImageView mImageView;

        public ParkingRenderer() {
            super(getApplicationContext(), mMap, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull MyItem parking, MarkerOptions markerOptions) {

            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
            // Draw a single person - show their profile photo and set the info window to show their name
            markerOptions
                    .icon(bitmapDescriptorFromVector(MapsActivity.this, parking.parkingIcon))
                    .title(parking.title).snippet(parking.getSnippet());
        }

    }

    /**
     * Initializes the ClusterManager so that it can be used to display clustered map markers and custom parking icons
     */
    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)

        clusterManager = new ClusterManager<MyItem>(this, mMap);
        //Set the renderer to customize the view
        clusterManager.setRenderer(new ParkingRenderer());


        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!checking4Permissions()) ask4Permissions();
        new AddressHandler(this, this).start();

        menuBottom = findViewById(R.id.bottom_navigation);


        searchView = findViewById(R.id.searchView1);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {return false;}

            @Override
            public boolean onQueryTextChange(String s) {
                filterlist(s);
                return false;
            }
        });

        RecyclerView recyclerView = findViewById(R.id.addressName);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //adapter = new MyRecyclerViewAdapter(this, testA());
        adapter = new MyRecyclerViewAdapter(this, new ArrayList<String>());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        recyclerView.setAlpha(0);
        switchButton = findViewById(R.id.switch1);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);//{recyclerView.setAlpha(1); CLICKABLE = true;}
                else mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);//{recyclerView.setAlpha(0); CLICKABLE = false;}
            }
        });



        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);//{recyclerView.setAlpha(1); CLICKABLE = true;}
                else mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);//{recyclerView.setAlpha(0); CLICKABLE = false;}
            }
        });



        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search view expended
                recyclerView.setAlpha(1); CLICKABLE = true;
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setItemModels(addresses1);
                //addresses2 = addresses1;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //Searchview not expanded
                recyclerView.setAlpha(0); CLICKABLE = false;
                recyclerView.setVisibility(View.INVISIBLE);
                return false;
            }
        });




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

                    String serverURL2 = "http://data.goteborg.se/ParkingService/v2.1/PublicTollParkings/c9848a32-3248-48e7-a28e-575e2d6d2889?latitude=57.7&longitude=11.9&radius=500&format=JSON";
                    new JSONService().execute(serverURL2);

                } else {
                    getLocation();


                    String serverURL = "http://data.goteborg.se/ParkingService/v2.1/PublicTimeParkings/c9848a32-3248-48e7-a28e-575e2d6d2889?latitude=" + latitude + "&longitude=" + longitude + "&radius=" + radius + "&format=JSON";
                    String serverURL2 =  "http://data.goteborg.se/ParkingService/v2.1/PublicTollParkings/c9848a32-3248-48e7-a28e-575e2d6d2889?latitude=" + latitude + "&longitude=" + longitude + "&radius=" + radius + "&format=JSON";

                    cameralat = parseDouble(latitude);
                    cameralong = parseDouble(longitude);

                    new JSONService().execute(serverURL);
                    new JSONService().execute(serverURL2);
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




    public ArrayList<String> testA() {
        // data to populate the RecyclerView with
        ArrayList<String> addresses = new ArrayList<>();
        addresses.add("Tiderakningsgatan 28");
        addresses.add("Lindholmen");
        addresses.add("Avenyn");
        addresses.add("Kortedala");
        addresses.add("Angered");
        addresses.add("Tynnered");

        return addresses;

        //return addresses1;

        // set up the RecyclerView
       /* RecyclerView recyclerView = findViewById(R.id.addressName);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, addresses);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);*/
    }

    /**
     * Gives addresses1 an array, used in the AddressHandler method: readInText
     *  @param array the list of strings that will be added to addresses1
     */
    public void setAddresses(ArrayList<String> array) {addresses1 = array; }

    /**
     * Gives the user a message on the screen, displaying the name of the address and row clicked.
     * Implementation of method in interface ItemClickListener
     *  @param position the string in mData to include in the message
     */
    @Override
    public void onItemClick(View view, int position) {
        if (!CLICKABLE) return;
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        filterlist2(adapter.getItem(position));
    }

    /**
     * Determines what addresses the recyclerview should display given a string
     * @param text the substring addresses that will be displayed should contain
     */
    private void filterlist(String text){
        ArrayList<String> filteredList = new ArrayList<>();

        for (String address : adapter.getmData()){
            if(address.toLowerCase().startsWith(text.toLowerCase())) {
                filteredList.add(address);
            }
        }

        if(filteredList.isEmpty()){
            Toast.makeText(this, "no data", Toast.LENGTH_SHORT).show();
        }
        else{
            adapter.setItemModels(filteredList);
        }
    }

    /**
     * Called onClick from row item; moving the camera to the address coordinates
     * @param s The address to move the camera to
     */
    private void filterlist2(String s) {
        List<Address> filterList = new ArrayList<>();

        // complete filterlist

        if (!TextUtils.isEmpty(s)) {

            Geocoder geocoder = new Geocoder(this);

            try {
                filterList = geocoder.getFromLocationName(s, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Toast.makeText(this, ""+filterList.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
            if (!filterList.isEmpty()) {
                double Lat = filterList.get(0).getLatitude();
                double Longt = filterList.get(0).getLongitude();
                Toast.makeText(this, "" + Lat + ", " + Longt , Toast.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat, Longt), zoomLevel));


                String serverURL = "http://data.goteborg.se/ParkingService/v2.1/PublicTimeParkings/c9848a32-3248-48e7-a28e-575e2d6d2889?latitude=" + Lat + "&longitude=" + Longt + "&radius=" + radius1 + "&format=JSON";
                String serverURL2 =  "http://data.goteborg.se/ParkingService/v2.1/PublicTollParkings/c9848a32-3248-48e7-a28e-575e2d6d2889?latitude=" + Lat + "&longitude=" + Longt + "&radius=" + radius1 + "&format=JSON";

                cameralat = Lat;
                cameralong = Longt;

                new JSONService().execute(serverURL);
                new JSONService().execute(serverURL2);

            }

            else
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
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

        setUpClusterer();

        //Important - required to make info box able to contain rows of text
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

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

        mMap.setPadding(0, 20, 50, 450); //----

        //sweden = dropMarker(new LatLng(-34, 145), "Sweden", true, true);
        //sweden.setTag(0);

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            getLocation();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(parseDouble(latitude), parseDouble(longitude)), zoomLevel));
        } catch (Exception e) {}

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
    private boolean checking4Permissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    private void ask4Permissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                1);

        if (!checking4Permissions()) {
            Toast.makeText(this, "You cannot use the app without granting the necessary permissions!", Toast.LENGTH_SHORT).show();
            openAppSettings();
        }
    }

    public void openAppSettings() {

        Uri packageUri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);

        Intent applicationDetailsSettingsIntent = new Intent();

        applicationDetailsSettingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        applicationDetailsSettingsIntent.setData(packageUri);
        applicationDetailsSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        getApplicationContext().startActivity(applicationDetailsSettingsIntent);
    }

    //Dropping, Removing and Updating Markers
    private Marker dropMarker(LatLng pos, String markerText, Boolean camera, boolean zoom) {
        MarkerOptions marker = new MarkerOptions().position(pos).draggable(true).title(markerText);
        Marker theMarker = mMap.addMarker(marker);

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

    /**
     * Updating the marker on the map
     * @param marker the icon placed on the map
     * @param newPos the position in terms of coordinates
     * @return returns the marker based on the information given
     */
    private Marker updateMarker(Marker marker, LatLng newPos) {
        marker.setPosition(newPos);
        return marker;
    }

    /**
     * Adding markers on the map
     * @param mMap INSERT DESCRIPTION
     */
    public void addMarkers(GoogleMap mMap) {

        if (markerArray != null) {
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

    private void OnGPS() {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    /**
     * INSERT JAVADOC
     */
    //Testing to comment for commitss
    private void getLocation() {
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

    /**
     * INSERT JAVADOC
     */
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

                JSONArray jsonArray = new JSONArray(Content.toString());

                for(int i=0;i<jsonArray.length();i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    boolean isFree = false;
                    String parkingCost = "";

                    try {
                        parkingCost = jsonObject.getString("ParkingCost");
                    } catch (Exception e){
                        isFree = true;
                    }

                    String nameTitle = jsonObject.getString("Name");
                    double lat = jsonObject.getDouble("Lat");
                    double lng = jsonObject.getDouble("Long");
                    LatLng pos = new LatLng(lat, lng);

                    LatLng parkeringGbg = new LatLng(lat, lng);
                    String time = jsonObject.getString("MaxParkingTime");
                    int parkingSpaces;
                    try {
                        parkingSpaces = jsonObject.getInt("ParkingSpaces");
                    } catch (Exception e){
                        parkingSpaces = -1;
                    }

                    MarkerOptions mk = new MarkerOptions();

                    MyItem clusterItem;

                    //custom info box
                    //icon for clusteritem

                    if (isFree) {
                        String text = "";
                        if (parkingSpaces == -1){
                            text = "Max parking time: "+time+"\n";
                        } else {
                            text = "Max parking time: "+time+"\n"+"Number of slots: "+parkingSpaces;
                        }
                        clusterItem = new MyItem(lat, lng, nameTitle, text,R.drawable.parking_icon_green);
                        //mk = new MarkerOptions().position(parkeringGbg).title(nameTitle).icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.parking_icon_green)).snippet(text);
                    } else{
                        String text = "Max parking time: "+time+"\n"+"Parking Cost: "+parkingCost+"\n"+"Number of slots: "+parkingSpaces;
                        clusterItem = new MyItem(lat, lng,  nameTitle, text, R.drawable.parking_icon);
                        //mk = new MarkerOptions().position(parkeringGbg).title(nameTitle).icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.parking_icon)).snippet(text);
                    }

                    //mMap.addMarker(mk);
                    clusterManager.addItem(clusterItem);


                    //TODO
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(cameralat, cameralong), 15.0f));


                }


            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("EXCEPTION   ","");
            }
        }



    }

    /**
     * Turns a png image into a BitMapDescriptor that can be used to display custom parking icons for the map marker
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Used to create a custom info box for the map marker that can contains several lines of text and description
     */
    public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;

        public CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        private void rendowWindowText(Marker marker, View view){

            System.out.println(""+ marker.getId());
            //while (marker.getTitle() == null){}
            String title = marker.getTitle();
            System.out.println(""+title);
            TextView tvTitle = (TextView) view.findViewById(R.id.title);

            try {
                if(!title.equals("")){
                    tvTitle.setText(title);
                }

            }catch (Exception e){}

            String snippet = marker.getSnippet();
            TextView tvSnippet = (TextView) view.findViewById(R.id.snippet);

            if(!snippet.equals("")){
                tvSnippet.setText(snippet);
            }
        }

        /**
         *
         * @param marker the specific icon placed on the window
         * @return INSERT description
         */
        @Override
        public View getInfoWindow(Marker marker) {
            rendowWindowText(marker, mWindow);
            return mWindow;
        }

        /**
         *
         * @param marker the specific icon placed on the window
         * @return INSERT description
         */
        @Override
        public View getInfoContents(Marker marker) {
            rendowWindowText(marker, mWindow);
            return mWindow;
        }
    }


}
