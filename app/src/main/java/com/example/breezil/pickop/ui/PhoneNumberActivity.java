package com.example.breezil.pickop.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.breezil.pickop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

public class PhoneNumberActivity extends AppCompatActivity {


    CountryCodePicker ccp;
    EditText editTextCarrierNumber;
    Button mSaveBtn;
//    DatabaseReference mDataRef;
//    FirebaseAuth mAuth;

    ProgressBar mPhoneProgress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        ccp = findViewById(R.id.ccp);
        editTextCarrierNumber = findViewById(R.id.editText_carrierNumber);

        ccp.registerCarrierNumberEditText(editTextCarrierNumber);

        mSaveBtn = findViewById(R.id.saveBtn);

//        mAuth = FirebaseAuth.getInstance();
        mPhoneProgress = findViewById(R.id.phoneProgress);






//        String user_id = mAuth.getCurrentUser().getUid();
//
//        mDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(user_id);




        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pNumber = ccp.getFullNumberWithPlus().trim();
                if(!TextUtils.isEmpty(pNumber)){

                    mPhoneProgress.setVisibility(View.VISIBLE);

                    sendToRegister(pNumber);

                }else {
                    Toast.makeText(PhoneNumberActivity.this,"Type status",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void sendToRegister(String pNumber) {

        mPhoneProgress.setVisibility(View.INVISIBLE);

        Intent regIntent = new Intent(PhoneNumberActivity.this,CustomerRegisterActivity.class);
        regIntent.putExtra("number",pNumber);
        regIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(regIntent);
        finish();

    }




}
