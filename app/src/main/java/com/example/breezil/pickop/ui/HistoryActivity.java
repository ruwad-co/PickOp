package com.example.breezil.pickop.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;

import com.example.breezil.pickop.Adapter.HistoryRecyclerViewAdapter;
import com.example.breezil.pickop.R;
import com.example.breezil.pickop.model.History;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.Calendar;
import java.util.List;

import java.util.ArrayList;
import java.util.Locale;


public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mHistoryRecyclerView;
    HistoryRecyclerViewAdapter historyRecyclerViewAdapter;
    RecyclerView.LayoutManager layoutManager;
    Toolbar mToolBar;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mToolBar = findViewById(R.id.historybar);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("History");


        mHistoryRecyclerView = findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(true);
        mHistoryRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        mHistoryRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerViewAdapter = new HistoryRecyclerViewAdapter(this,getDataSetHistory());
        mHistoryRecyclerView.setAdapter(historyRecyclerViewAdapter);


        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userHistory();

    }

    private void userHistory() {
        DatabaseReference userHistoryIdRef = FirebaseDatabase.getInstance().getReference()
                .child("HistoryId").child("Customer").child(userId);
        userHistoryIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history: dataSnapshot.getChildren()){
                        getHistoryInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getHistoryInformation(String historyKey) {
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference()
                .child("History").child(historyKey);

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String pickOpId = dataSnapshot.getKey();
                    Long timeStamp = 0L;
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        if(dataSnapshot.getKey().equals("time"));
                        timeStamp = Long.valueOf(dataSnapshot.getValue().toString());
                    }

                    History history = new History(pickOpId,getTimeStamp(timeStamp));
                    resultHistory.add(history);
                    historyRecyclerViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private String getTimeStamp(Long timeStamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timeStamp*1000);
        String dateTime = DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();

        return dateTime;
    }

    private ArrayList resultHistory = new ArrayList<History>();
    private ArrayList<History> getDataSetHistory() {
        return resultHistory;
    }
}
