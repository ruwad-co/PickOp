package com.example.breezil.pickop.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.breezil.pickop.Adapter.HistoryRecyclerViewAdapter;
import com.example.breezil.pickop.R;
import com.example.breezil.pickop.model.History;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView mHistoryRecyclerView;
    HistoryRecyclerViewAdapter historyRecyclerViewAdapter;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mHistoryRecyclerView = findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(true);
        mHistoryRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        mHistoryRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerViewAdapter = new HistoryRecyclerViewAdapter(this,getDataSetHistory());
        mHistoryRecyclerView.setAdapter(historyRecyclerViewAdapter);
    }

    private ArrayList resultHistory = new ArrayList<History>();
    private ArrayList<History> getDataSetHistory() {
        return resultHistory;
    }
}
