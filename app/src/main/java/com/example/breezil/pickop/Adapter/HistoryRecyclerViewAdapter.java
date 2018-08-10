package com.example.breezil.pickop.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.breezil.pickop.R;
import com.example.breezil.pickop.model.History;
import com.example.breezil.pickop.ui.HistoryDetailActivity;
import com.example.breezil.pickop.ui.StartActivity;

import java.util.Date;
import java.util.List;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryHolder> {

    Context mContext;
    List<History> mHistoryList;

    public HistoryRecyclerViewAdapter(Context mContext, List<History> mHistoryList) {
        this.mContext = mContext;
        this.mHistoryList = mHistoryList;
    }


    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_history,parent,false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        HistoryHolder historyHolder = new HistoryHolder(view);

        return historyHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
        holder.historyId.setText(mHistoryList.get(position).getHistoryId());
        holder.time.setText(mHistoryList.get(position).getTime());

    }

    @Override
    public int getItemCount() {
        return mHistoryList.size();
    }

    public static class HistoryHolder  extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView historyId;
        TextView time;
        public HistoryHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            historyId =itemView.findViewById(R.id.historyId);
            time =itemView.findViewById(R.id.timeId);


        }

        @Override
        public void onClick(View v) {
            Intent detailHistoryIntent = new Intent(v.getContext(), HistoryDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("historyId", historyId.getText().toString());
            detailHistoryIntent.putExtras(bundle);
            v.getContext().startActivity(detailHistoryIntent);
        }
    }

}
