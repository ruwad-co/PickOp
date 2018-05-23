package com.example.breezil.pickop.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.breezil.pickop.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    FloatingActionButton mLogout;
    FloatingActionButton mRequestbtn;
    FloatingActionButton mCancelRequestBtn;
    private LatLng mPickUpLocation;
    private TextView mDistanceText;
    private Boolean requestBool = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mLogout = findViewById(R.id.logout);
        mRequestbtn = findViewById(R.id.pickupRequest);
        mCancelRequestBtn = findViewById(R.id.cancelRequest);
        mDistanceText = findViewById(R.id.distanceText);




        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                sendToStart();

            }
        });


        mRequestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBool = true;
                mRequestbtn.setVisibility(View.INVISIBLE);
                mCancelRequestBtn.setVisibility(View.VISIBLE);
                String Uid = mAuth.getCurrentUser().getUid();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pickUpRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(Uid, new GeoLocation(mLastLocation.getLatitude(),
                        mLastLocation.getLongitude()));


                mPickUpLocation = new LatLng(mLastLocation.getLatitude()
                        ,mLastLocation.getLongitude());

                mMap.addMarker(new MarkerOptions().position(mPickUpLocation).title("PickUp Here"));

                mRequestbtn.setBackgroundColor(getResources().getColor(R.color.colorGrey));

                getClosestDriver();
            }
        });

        mCancelRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBool = false;
                geoQuery.removeAllListeners();
                driverLocationRef.removeEventListener(driverLocationListener);
                if(driverFoundId != null){
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Driver").child(driverFoundId);
                    driverRef.setValue(true);
                    driverFoundId = null;
                }
                driverFound = false;
                radius = 1;


                String Uid = mAuth.getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pickUpRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(Uid);
            }
        });
    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundId;

    GeoQuery geoQuery;
    private void getClosestDriver() {

        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        GeoFire geoFire = new GeoFire(driverLocationRef);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(mPickUpLocation.latitude,
                mPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestBool){
                    driverFound = true;
                    driverFoundId = key;

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Driver").child(driverFoundId);

                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    HashMap map = new HashMap();
                    map.put("customerId",customerId);
                    driverRef.updateChildren(map);

                    getDriverLocation();
                    mRequestbtn.setEnabled(false);
                    Toast.makeText(CustomerMapsActivity.this,"Looking for drivers location",Toast.LENGTH_LONG).show();


                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound){
                    radius++;
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationListener;
    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference()
                .child("DriversWorking").child(driverFoundId).child("l");
        driverLocationListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBool){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;

                    Toast.makeText(CustomerMapsActivity.this,"Pick Up Found",Toast.LENGTH_LONG).show();
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if(map.get(1) != null){
                        locationLong = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLong = new LatLng(locationLat,locationLong);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(mPickUpLocation.latitude);
                    loc1.setLongitude(mPickUpLocation.longitude);


                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLong.latitude);
                    loc2.setLongitude(driverLatLong.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if(distance < 100){
                        mDistanceText.setVisibility(View.VISIBLE);
                        mDistanceText.setText(R.string.driverhere);
                    }else{
                        mDistanceText.setVisibility(View.VISIBLE);
                        mDistanceText.setText(String.valueOf(distance));
                    }


                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLong).title("your pickup"));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mCurrentUser == null){
            sendToStart();
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
        mMap = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

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
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(mLocationRequest.PRIORITY_HIGH_ACCURACY);

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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mAuth.getCurrentUser() != null){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference Ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");


            GeoFire geoFire = new GeoFire(Ref);
            geoFire.removeLocation(userId);
        }

    }


    private void sendToStart() {
        Intent startIntent = new Intent(CustomerMapsActivity.this,StartActivity.class);startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }

}
