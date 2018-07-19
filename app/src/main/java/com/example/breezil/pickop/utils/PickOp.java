package com.example.breezil.pickop.utils;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class PickOp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //offline capability
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);



        //-----Picasso---//
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
