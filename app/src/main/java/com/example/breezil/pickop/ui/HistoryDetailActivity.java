package com.example.breezil.pickop.ui;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.breezil.pickop.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryDetailActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private SupportMapFragment mSupportMapFragment;
    private Toolbar mToolBar;
    String mHistoryId;
    private TextView mLocationText, mDistanceText, mDateText, mNameText, mPhoneNumberText;
    CircleImageView mUserImage;

    String mUid, mDriverId;

    DatabaseReference historyRef;

    private LatLng destinationLatLng, pickOpLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        mHistoryId = getIntent().getExtras().getString("historyId");
        polylines = new ArrayList<>();


        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.historyMap);

        mSupportMapFragment.getMapAsync(this);

        mToolBar = findViewById(R.id.historyDetailBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("History Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLocationText = findViewById(R.id.historyLocation);
        mDistanceText = findViewById(R.id.historyDistance);
        mDateText = findViewById(R.id.historyDate);
        mNameText = findViewById(R.id.historyUserName);
        mPhoneNumberText = findViewById(R.id.historyUserPhone);
        mUserImage = findViewById(R.id.historyUserImage);

        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyRef = FirebaseDatabase.getInstance().getReference().child("History").child(mHistoryId);

        loadHistoryInfo();

    }

    private void loadHistoryInfo() {

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                        if (child.getKey().equals("driver")) {
                            mDriverId = child.getValue().toString();
                            getDriverInfo(mDriverId);
                        }

                        if (child.getKey().equals("time")) {
                            mDateText.setText(getTimeStamp(Long.valueOf(child.getValue().toString())));
                        }
                        if (child.getKey().equals("destination")) {
                            mLocationText.setText("To :" +child.getValue().toString());
                        }

                        if (child.getKey().equals("pickOp_location")) {
                            pickOpLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("long").getValue().toString()));
                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("long").getValue().toString()));
                            if (destinationLatLng != new LatLng(0, 0) || destinationLatLng.equals("default")) {
                                getRouteToMarker();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDriverInfo(String mDriverId) {

        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver")
                .child(mDriverId);

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                    if (driverMap.get("first_name") != null && driverMap.get("last_name") != null) {
                        String firstname = driverMap.get("first_name").toString();
                        String lastname = driverMap.get("last_name").toString();
                        mNameText.setText(firstname +" "+lastname);
                    }

                    if (driverMap.get("phone_number") != null) {
                        String phone_number = driverMap.get("phone_number").toString();
                        mPhoneNumberText.setText(phone_number);
                    }


                    if (driverMap.get("image") != null) {
                        String image = driverMap.get("image").toString();
                        if (!image.equals("default")) {
//
                            Glide.with(getApplication())
                                    .load(image)
                                    .apply(new RequestOptions().override(100, 100).placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar))
                                    .into(mUserImage);
                        } else {
                            mUserImage.setImageResource(R.drawable.default_avatar);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getTimeStamp(Long timeStamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timeStamp * 1000);
        String dateTime = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();

        return dateTime;
    }

    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickOpLatLng, destinationLatLng)
                .build();
        routing.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }



    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimary,R.color.colorPrimaryDark,R.color.dark1,R.color.colorAccent,R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickOpLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(pickOpLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_purple)));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("destination"));

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onRoutingCancelled() {
    }
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
}
