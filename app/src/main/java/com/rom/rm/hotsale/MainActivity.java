package com.rom.rm.hotsale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.support.v4.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private GoogleMap mMap;
    private static final String MYTAG = "MYTAG";
    private static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION=100;
    private BottomNavigationView navigation;
    private int PROXIMITY_RADIUS = 1000;
    private static double latitude, longitude;
    private GoogleApiClient googleApiClient;
    private static Location location;
    private LocationRequest locationRequest;
    private Marker marker;
    private int DEFAULT_ZOOM=15;
    private Toolbar toolbar;

    public static double getLatitude() {
        return latitude;
    }

    public static void setLatitude(double latitude) {
        MainActivity.latitude = latitude;
    }

    public static double getLongitude() {
        return longitude;
    }

    public static void setLongitude(double longitude) {
        MainActivity.longitude = longitude;
    }


    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        handleIntent(getIntent());
        if (checkGooglePlayServices()) {
            Log.d(MYTAG,"GooglePlay Service available");
            buildGoogleApiClient();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);
            }
        });
    }

    private void onMyMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                askPermissionsAndShowMyLocation();
            }
        });
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MainActivity.this));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(marker.getSnippet());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (jsonObject != null && jsonObject.has("place_id")) {

                    String url = getDetailsUrl(jsonObject.optString("place_id"));
                    GetDetailsOfPlaces getDetailsOfPlaces = new GetDetailsOfPlaces(MainActivity.this);
                    getDetailsOfPlaces.execute(url);
                }
            }
        });
    }

    /**
     * Phương thức kiểm chứng google play services trên thiết bị
     */
    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(result)) {
                googleApiAvailability.getErrorDialog(this, result, 0).show();
            }
            return false;
        }
        return true;
    }

    private void askPermissionsAndShowMyLocation() {
        // hỏi người dùng cho phép xem vị trí của họ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
                    // Kiểm tra quyền hạn
                   AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Allow permission");
                    builder.setMessage("to access this device's location?");
                    builder.setCancelable(false);
                    builder.setNegativeButton("ALLOW", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    buildGoogleApiClient();
                    mMap.setMyLocationEnabled(true);
                    // Hiển thị vị trí hiện thời trên bản đồ.
                    this.showMyLocation();
                }
            }
        else {

            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mMap.clear();
                    location=mMap.getMyLocation();
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();

                    LatLng latLng = new LatLng(latitude, longitude);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)             // Sets the center of the map to location user
                            .zoom(DEFAULT_ZOOM)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    // Thêm Marker cho Map:
                    MarkerOptions option = new MarkerOptions();
                    option.title("You're here");
                    option.snippet("....");
                    option.position(latLng);
                    Marker currentMarker = mMap.addMarker(option);
                    currentMarker.showInfoWindow();
                    return true;
                }
            });
        }
        // Hiển thị vị trí hiện thời trên bản đồ.
        this.showMyLocation();
    }
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
    // Tìm một nhà cung cấp vị trị hiện thời đang được mở.
    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        // Tiêu chí để tìm một nhà cung cấp vị trí.
        Criteria criteria = new Criteria();

        // Tìm một nhà cung vị trí hiện thời tốt nhất theo tiêu chí trên.
        // ==> "gps", "network",...
        String bestProvider = locationManager.getBestProvider(criteria, true);

        boolean enabled = locationManager.isProviderEnabled(bestProvider);

        if (!enabled) {
            Toast.makeText(this, "No location provider enabled!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }

    // Chỉ gọi phương thức này khi đã có quyền xem vị trí người dùng.
    private void showMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String locationProvider = this.getEnabledLocationProvider();

        if (locationProvider == null) {
            return;
        }

        Location myLocation = null;
        try {
            // Lấy ra vị trí.
            myLocation = locationManager.getLastKnownLocation(locationProvider);
        }
        // Với Android API >= 23 phải catch SecurityException.
        catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(MYTAG, "Show My Location Error:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (myLocation != null) {

            latitude=myLocation.getLatitude();
            longitude=myLocation.getLongitude();

            LatLng latLng = new LatLng(latitude, longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to location user
                    .zoom(DEFAULT_ZOOM)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // Thêm Marker cho Map:
            MarkerOptions option = new MarkerOptions();
            option.title("You're here");
            option.snippet("....");
            option.position(latLng);
            Marker currentMarker = mMap.addMarker(option);
            currentMarker.showInfoWindow();
        } else {
            Toast.makeText(this, "Location not found!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "Location not found");
        }
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            String url;
            Object[] dataTransfer;
            String[] typeList;

            if (item.getItemId() == R.id.navigation_scenic
                    || item.getItemId() == R.id.navigation_eating
                    || item.getItemId() == R.id.navigation_emergency
                    || item.getItemId() == R.id.navigation_rest) {
                mMap.clear();
                if (item.getItemId() == R.id.navigation_scenic) {
                    typeList = new String[]{"amusement_park", "zoo", "campground", "museum"};
                } else if (item.getItemId() == R.id.navigation_eating) {
                    typeList = new String[]{"bakery", "restaurant", "cafe"};
                } else if (item.getItemId() == R.id.navigation_emergency) {
                    typeList = new String[]{"police", "pharmacy", "hospital"};
                } else {
                    typeList = new String[]{"hotel", "lodging"};
                }

                for (int index = 0; index < typeList.length; index++) {
                    url = getUrl(latitude, longitude, typeList[index]);
                    dataTransfer = new Object[2];
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = url;
                    GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                    getNearbyPlacesData.execute(dataTransfer);
                }
                return true;
            }
            return false;
        }
    };

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyBpTLBynSv6JC0kBIRRNOmdsVNdsOsD_Do");
        Log.d("Url",googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }
     private String getDetailsUrl(String placeId) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        googlePlaceUrl.append("placeid=" + placeId);
        googlePlaceUrl.append("&key=" + "AIzaSyBpTLBynSv6JC0kBIRRNOmdsVNdsOsD_Do");
        Log.d("DetailsURL", googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
     }


    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest=new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
           LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }
    @Override
    public void onLocationChanged(Location location) {
        this.location= location;
        if (marker!=null){
            marker.remove();
            longitude=location.getLongitude();
            latitude=location.getLatitude();

            LatLng latLng=new LatLng(latitude,longitude);
            MarkerOptions markerOptions= new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Your position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

            marker= mMap.addMarker(markerOptions);

            //Remove camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

            if (googleApiClient!=null){
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
            }
        }

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
//Search with recommend
    private void handleIntent(Intent intent){
        if (intent.getAction().equals(Intent.ACTION_SEARCH)){
            doSearch(intent.getStringExtra(SearchManager.QUERY));
        }
        else if (intent.getAction().equals(Intent.ACTION_VIEW)){
            getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.action_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_search:
                onSearchRequested();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void doSearch(String query){
        Bundle data = new Bundle();
        data.putString("query", query);
        getSupportLoaderManager().restartLoader(0, data, this);
    }

    private void getPlace(String query){
        Bundle data = new Bundle();
        data.putString("query", query); //gửi query (1 cặp key and value)
        getSupportLoaderManager().restartLoader(1, data, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle query) {
        //Tải dữ liệu k đồng bộ từ 1 content provider (lớp con của Async Task Loader)
        CursorLoader cursorLoader=null;
        if (id==0){
            //URI của nội dung cần truy xuất, SELECT, WHERE,ORDERBY
            cursorLoader=new CursorLoader(getBaseContext(),PlaceProvider.SEARCH_URI,null,
                    null,new String[]{query.getString("query")},null);
        }
        else if (id==1){
            cursorLoader=new CursorLoader(getBaseContext(),PlaceProvider.DETAILS_URI,null,
                    null,new String[]{query.getString("query")},null);
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        showLocation(cursor);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
    private void showLocation(Cursor cursor){
        MarkerOptions markerOptions=new MarkerOptions();
        LatLng latLng=null;
        mMap.clear();
        while (cursor.moveToNext()){
            latitude=Double.parseDouble(cursor.getString(1));
            longitude=Double.parseDouble(cursor.getString(2));
            latLng=new LatLng(latitude,longitude);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("vicinity", "");
                jsonObject.put("place_id", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            markerOptions.snippet(jsonObject.toString());
            markerOptions.position(latLng);
            markerOptions.title(cursor.getString(0));
            mMap.addMarker(markerOptions);
        }
        if(latLng!=null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to location user
                    .zoom(DEFAULT_ZOOM)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
}
