package com.example.breezil.pickop.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.breezil.pickop.R;
import com.example.breezil.pickop.model.History;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "CutomerMapsActivity";
    private DrawerLayout mDrawerLayout;
    private ConstraintLayout mNavHeader;

    private CircleImageView mNavImage;
    private TextView mNavFirstName;

    private GoogleMap mMap;

    private FirebaseUser mCurrentUser;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;

    LocationRequest mLocationRequest;

    BottomSheetDialog pickOpOptionSheet;

    Button mRequestbtn;
    Button mCancelBtn;

    FloatingActionButton mMenuBtn;

    SupportMapFragment mapFragment;

    private RatingBar mRatingBar;


    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng mPickUpLocation;
    private TextView mDistanceText;
    private Boolean requestBool = false;
    Marker mPickOpMarker;

    Marker mDriverMarker;
    GeoQuery geoQuery;

    String mUid;

    String destination;
    LatLng destinationLatLong;

    TextView driversName;
    TextView driversNumber;
    CircleImageView driversImage;
    TextView driversVehicle;
    TextView requestStateTextView;
    Button closeBottomSheet;
    Button showBottomSheet;

    BottomSheetDialog bottomSheetDialog;

    private LinearLayout mPickOpLight;
    private LinearLayout mPickOpMedium;
    private LinearLayout mPickOpHeavy;
    String mRequestType;

    String mUserFirstName;
    String mUserLastName;
    String mUserEmail;
    String mUserPhoneNumber;
    String mUserImage;

    boolean accepted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        mAuth = FirebaseAuth.getInstance();


        mCurrentUser = mAuth.getCurrentUser();

        if (mCurrentUser != null) {
            mUid = mCurrentUser.getUid();
        }

        destinationLatLong = new LatLng(0.0,0.0);


        mRequestbtn = findViewById(R.id.pickOprequestbtn);

        mDistanceText = findViewById(R.id.distanceText);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mMenuBtn = findViewById(R.id.menuFab);
        mCancelBtn = findViewById(R.id.endPickOpBtn);


        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.getHeaderView(0);

        mNavHeader = navHeaderView.findViewById(R.id.userProfileHeaderLayout);

        mNavImage = navHeaderView.findViewById(R.id.customerNavImage);
        mNavFirstName = navHeaderView.findViewById(R.id.userNavName);

        showBottomSheet = findViewById(R.id.show_sheet_btn);




        mMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });


        mRequestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request();
            }
        });



        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRequest();
            }
        });


        autoComplete();

        mNavHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSettings();
            }
        });

    }


    private void autoComplete() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
                destinationLatLong = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.

            }
        });
    }

    private void request() {
//        if (requestBool) {
//            cancelRequest();
//            mRequestbtn.setText("Cancel");
//        } else {
//            showPickOpOptions();
////            sendRequest();
//        }

        showPickOpOptions();
    }



    private void userNavInfo() {
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(mAuth.getCurrentUser().getUid());
        if (mUserRef != null) {
            mUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                        if (map.get("first_name") != null) {
                            mUserFirstName = dataSnapshot.child("first_name").getValue().toString();
                        }


                        if (map.get("last_name") != null) {
                            mUserLastName = dataSnapshot.child("last_name").getValue().toString();
                        }

                        if (map.get("email") != null) {
                            mUserEmail = dataSnapshot.child("email").getValue().toString();
                        }
                        if (map.get("phone_number") != null) {

                            mUserPhoneNumber = dataSnapshot.child("phone_number").getValue().toString();
                        }

                        if (map.get("image") != null) {
                            mUserImage = dataSnapshot.child("image").getValue().toString();
                        }

                        mNavFirstName.setText(mUserFirstName);

                        if (!mUserImage.equals("default")) {

                            Glide.with(getApplication())
                                    .load(mUserImage)
                                    .apply(new RequestOptions().override(100, 100).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar))
                                    .into(mNavImage);
                        }else {
                            mNavImage.setImageResource(R.drawable.default_avatar);

                        }
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    private void gotoSettings() {
        Intent settingIntent = new Intent(CustomerMapsActivity.this, UserSettingsActivity.class);
        settingIntent.putExtra("first_name", mUserFirstName);
        if (mUserLastName != null) settingIntent.putExtra("last_name", mUserLastName);
        if (mUserPhoneNumber != null) settingIntent.putExtra("phone_number", mUserPhoneNumber);
        if (mUserEmail != null) settingIntent.putExtra("email", mUserEmail);
        startActivity(settingIntent);
    }


    private void showPickOpOptions() {
        pickOpOptionSheet = new BottomSheetDialog(this);
        View bottomSheetView = this.getLayoutInflater().inflate(R.layout.choose_pickop_sheet, null);

        pickOpOptionSheet.setContentView(bottomSheetView);
        pickOpOptionSheet.show();

        mPickOpLight = bottomSheetView.findViewById(R.id.pickop_light_sheet);
        mPickOpMedium = bottomSheetView.findViewById(R.id.pickop_medium_sheet);
        mPickOpHeavy = bottomSheetView.findViewById(R.id.pickop_heavy_sheet);

        mPickOpLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String requestType = "PickOpLight";
                sendRequest(requestType);
                pickOpOptionSheet.dismiss();
                mPickOpLight.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        mPickOpMedium.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String requestType = "PickOpMedium";
                sendRequest(requestType);
                pickOpOptionSheet.dismiss();
                mPickOpMedium.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        mPickOpHeavy.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                String requestType = "PickOpHeavy";
                sendRequest(requestType);
                pickOpOptionSheet.dismiss();
                mPickOpHeavy.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        });


    }


    void sendRequest(String requestType) {


        mRequestType = requestType ;


        requestBool = true;

        String Uid = mAuth.getCurrentUser().getUid();

        DatabaseReference pickOpRef = FirebaseDatabase.getInstance().getReference("pickUpRequest");
        GeoFire geoFire = new GeoFire(pickOpRef);
        geoFire.setLocation(Uid, new GeoLocation(mLastLocation.getLatitude(),
                mLastLocation.getLongitude()));

        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("state","requested");
        requestMap.put("time", ServerValue.TIMESTAMP);
        requestMap.put("type",mRequestType);
        pickOpRef.child("extra").child(mUid).updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Toast.makeText(CustomerMapsActivity.this,"Requested",Toast.LENGTH_LONG).show();
            }
        });



        mPickUpLocation = new LatLng(mLastLocation.getLatitude()
                , mLastLocation.getLongitude());

        mPickOpMarker = mMap.addMarker(new MarkerOptions().position(mPickUpLocation).title("PickUp Here")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_purple)));

//        mRequestbtn.setText("Getting PickOP...");

        getClosestDriver();
        mRequestbtn.setVisibility(View.INVISIBLE);
        mCancelBtn.setVisibility(View.VISIBLE);

    }

    void cancelRequest() {


        mRequestbtn.setEnabled(true);
//        endPickOp();
        mCancelBtn.setVisibility(View.INVISIBLE);
        mRequestbtn.setVisibility(View.VISIBLE);

        if(!accepted){
            endPickOp();

        }else{
            endPickOp();
            mCancelBtn.setText("End PickOp");
        }
    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundId;


    private void getClosestDriver() {

        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        GeoFire geoFire = new GeoFire(driverLocationRef);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(mPickUpLocation.latitude,
                mPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!driverFound && requestBool) {

                    DatabaseReference mCustomerRequestTypeRef = FirebaseDatabase.getInstance()
                            .getReference().child("Users").child("Driver").child(key);
                    mCustomerRequestTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0 ){
                                Map<String, Object> typeMap = (Map<String, Object>)dataSnapshot.getValue();

                                if(driverFound){
                                    return;
                                }

                                mRequestNode();

                                showBottomSheet.setVisibility(View.VISIBLE);
                                showBottomSheet.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(bottomSheetDialog != null){
                                            bottomSheetDialog.show();
                                        }
                                    }
                                });

                                if(typeMap.get("type").equals(mRequestType)){
                                    driverFound = true;
                                    driverFoundId = dataSnapshot.getKey();



                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                                            .child("Users").child("Driver").child(driverFoundId).child("available");

                                    DatabaseReference pickOpsrefRequested = FirebaseDatabase.getInstance().getReference()
                                            .child("PickOps").child(driverFoundId);



                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                    HashMap map = new HashMap();
                                    map.put("customerId", customerId);
                                    map.put("locationLat", mLastLocation.getLatitude());
                                    map.put("locationLong", mLastLocation.getLongitude());
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLong.latitude);
                                    map.put("destinationLong", destinationLatLong.longitude);
                                    map.put("request_state", "requested");
//                                    driverRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener()
                                    pickOpsrefRequested.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {

                                            Toast.makeText(CustomerMapsActivity.this, "waiting for pickop response",Toast.LENGTH_LONG).show();

                                            accepted = true;

                                            getDriverLocation();

                                            getDriversInformation();

                                        }
                                    });


                                    getHasPickOpEnded();

//                                    mRequestbtn.setText("Cancel");
                                    Toast.makeText(CustomerMapsActivity.this, "Looking for drivers location", Toast.LENGTH_LONG).show();


                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });





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
                if (!driverFound) {
                    radius++;
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    void getDriversInformation() {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Driver").child(driverFoundId);
        DatabaseReference stateRef = FirebaseDatabase.getInstance().getReference().child("PickOps")
                .child(driverFoundId);

        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = this.getLayoutInflater().inflate(R.layout.driver_info_sheet, null);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

        driversName = bottomSheetView.findViewById(R.id.sheet_name);
        driversNumber = bottomSheetView.findViewById(R.id.sheet_number);
        driversImage = bottomSheetView.findViewById(R.id.sheet_image);
        driversVehicle = bottomSheetView.findViewById(R.id.sheet_vehicle);
        mRatingBar = bottomSheetView.findViewById(R.id.sheet_rating);
        requestStateTextView = bottomSheetView.findViewById(R.id.request_state);
        closeBottomSheet = bottomSheetView.findViewById(R.id.close_sheet_btn);

        stateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String request_state = dataSnapshot.child("request_state").getValue().toString();
                    requestStateTextView.setText(request_state);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        driverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstname = dataSnapshot.child("first_name").getValue().toString();
                    String lastname = dataSnapshot.child("last_name").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();
                    String number = dataSnapshot.child("phone_number").getValue().toString();
                    String vehicle = dataSnapshot.child("vehicle").getValue().toString();

                    driversName.setText(firstname + " " + lastname);
                    driversNumber.setText(number);
                    driversVehicle.setText(vehicle);

                    if (!image.equals("default")) {
                        Glide.with(getApplication())
                                .load(image)
                                .apply(new RequestOptions().override(100, 100).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar))
                                .into(driversImage);
                    }else {
                        driversImage.setImageResource(R.drawable.default_avatar);

                    }

                    int ratingSum = 0;
                    float ratingTotal = 0;
                    float ratingAverage = 0;
                    for(DataSnapshot child: dataSnapshot.child("rating").getChildren() ){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingTotal++;
                    }
                    if(ratingTotal != 0){
                        ratingAverage = ratingSum/ratingTotal;
                        mRatingBar.setRating(ratingAverage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        closeBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    //private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationListener;

    private void getDriverLocation() {
//        driverLocationRef = FirebaseDatabase.getInstance().getReference()
//                .child("DriversWorking").child(driverFoundId).child("l");
        driverLocationRef = FirebaseDatabase.getInstance().getReference()
                .child("DriversAvailable").child(driverFoundId).child("l");
        driverLocationListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBool) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;

                    Toast.makeText(CustomerMapsActivity.this, "Pick Up Found", Toast.LENGTH_LONG).show();

                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if (map.get(1) != null) {
                        locationLong = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLong = new LatLng(locationLat, locationLong);
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(mPickUpLocation.latitude);
                    loc1.setLongitude(mPickUpLocation.longitude);


                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLong.latitude);
                    loc2.setLongitude(driverLatLong.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if (distance < 100) {
                        mDistanceText.setVisibility(View.VISIBLE);
                        mDistanceText.setText(R.string.driverhere);
                    } else {
                        mDistanceText.setVisibility(View.VISIBLE);
                        mDistanceText.setText(String.valueOf(distance));
                    }


                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLong).title("your pickup")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.pickop_medium)));



                    driverLocationRef.removeEventListener(driverLocationListener);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void mRequestNode(){
        String Uid = mAuth.getCurrentUser().getUid();
        DatabaseReference pickOpRef = FirebaseDatabase.getInstance().getReference("pickOpRequest");
        GeoFire geoFire = new GeoFire(pickOpRef);
        geoFire.setLocation(Uid, new GeoLocation(mLastLocation.getLatitude(),
                mLastLocation.getLongitude()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentUser == null) {
            sendToStart();
        }
        if (mUid != null) {
            userNavInfo();
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        connectCustomer();


    }


    DatabaseReference pickOpEndedRef;
    private ValueEventListener pickOpEndedRefListener;
    private void getHasPickOpEnded() {

        if(mAuth.getCurrentUser() != null){
//             pickOpEndedRef = FirebaseDatabase.getInstance().getReference()
//                    .child("Users").child("Driver").child(driverFoundId).child("available").child("customerId");

            pickOpEndedRef = FirebaseDatabase.getInstance().getReference()
                    .child("PickOps").child(driverFoundId);

            pickOpEndedRefListener = pickOpEndedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){



                    }else {

                        endPickOp();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }




    }

    private void endPickOp() {
        requestBool = false;
        geoQuery.removeAllListeners();

        if (driverLocationRef != null) {

            driverLocationRef.removeEventListener(driverLocationListener);
            pickOpEndedRef.removeEventListener(pickOpEndedRefListener);

            if (driverFoundId != null) {
                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child("Driver").child(driverFoundId).child("available");

                driverRef.setValue(true);
                driverFoundId = null;
            }
            driverFound = false;
            radius = 1;
        }


        String Uid = mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pickUpRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(Uid);
        ref.child("extra").child(mUid).removeValue();


        if (mPickOpMarker != null) {
            mPickOpMarker.remove();
        }
        if (mDriverMarker != null) {
            mDriverMarker.remove();
        }

        mRequestbtn.setText("Request PickOp");
        mRequestbtn.setVisibility(View.VISIBLE);
        mDistanceText.setVisibility(View.INVISIBLE);

        if(bottomSheetDialog != null){
            bottomSheetDialog.dismiss();
        }



        showBottomSheet.setVisibility(View.INVISIBLE);



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
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(mLocationRequest.PRIORITY_HIGH_ACCURACY);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else {
                checkLocationPermission();
            }
        }

        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        if(mMap != null){
            mMap.setMyLocationEnabled(true);
        }

    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    float zoomLevel = 16.0f;
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                }
            }
        }
    };



    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("allow pickop to use location")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                            }
                        })
                        .create()

                        .show();

            }else {
                ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
    }


    private void connectCustomer() {
        checkLocationPermission();
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        if(mMap != null){
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
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

        Intent startIntent = new Intent(CustomerMapsActivity.this,StartActivity.class);

        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.navPayment:
                Toast.makeText(CustomerMapsActivity.this,"Payment",Toast.LENGTH_LONG).show();
                break;

            case R.id.navHistory:
                gotoHistory();
                break;
            case R.id.navSupport:
                break;
            case R.id.navFreePickOp:
                break;
            case R.id.settings:
                break;
            case R.id.LogOut:
                mAuth.signOut();
                sendToStart();
                break;
        }
        return true;
    }

    private void gotoHistory() {
        Intent historyIntent = new Intent(this, HistoryActivity.class);
        startActivity(historyIntent);
    }
}
