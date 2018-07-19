package com.example.breezil.pickop.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.breezil.pickop.R;

public class StartActivity extends AppCompatActivity {

    Button mRegisterBtn;
    Button mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mRegisterBtn = findViewById(R.id.startRegisterBtn);
        mLoginBtn = findViewById(R.id.startLoginBtn);


        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent registerIntent = new Intent(StartActivity.this, CustomerMapsActivity.class);

                startActivity(new Intent(StartActivity.this, PhoneNumberActivity.class));

            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, CustomerLoginActivity.class));
            }
        });
    }
}
