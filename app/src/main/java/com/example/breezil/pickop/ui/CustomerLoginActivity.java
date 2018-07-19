package com.example.breezil.pickop.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.breezil.pickop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class CustomerLoginActivity extends AppCompatActivity {

    private EditText mEmailText, mPasswordText;
    private TextView mRegister;
    private Button mLoginBtn;

    private DatabaseReference mUsertokenref;


    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseAuth.AuthStateListener mAuthState;

    private static final String TAG = "CustomerLoginActivity";

    private ProgressDialog mLoginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        mEmailText = findViewById(R.id.customerLogEmailText);
        mPasswordText = findViewById(R.id.customerLogPassText);
        mLoginBtn = findViewById(R.id.customerLogBtn);

        mRegister = findViewById(R.id.gotoRegCustomer);
        mLoginProgress = new ProgressDialog(this);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerLoginActivity.this, PhoneNumberActivity.class));
            }
        });

        mUsertokenref = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer");

        mAuth = FirebaseAuth.getInstance();
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = mEmailText.getText().toString().trim();
                String passwordText = mPasswordText.getText().toString().trim();
                login(emailText,passwordText);
            }
        });
    }
    private void login(String emailText, String passwordText) {

        if(!TextUtils.isEmpty(emailText) && !TextUtils.isEmpty(passwordText)){
            mLoginProgress.setMessage("Login in");
            mLoginProgress.show();
            mAuth.signInWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mLoginProgress.dismiss();
                        String current_user = mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        mUsertokenref.child(current_user).child("deviceToken").setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            Intent mainIntent = new Intent(CustomerLoginActivity.this,CustomerMapsActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    }
                                });
                    }else{
                        mLoginProgress.dismiss();
                        Toast.makeText(CustomerLoginActivity.this ,"Login Error Please try again",Toast.LENGTH_LONG).show();
                    }
                }


            });
        }else{
            Toast.makeText(CustomerLoginActivity.this ,"Please type login details",Toast.LENGTH_LONG).show();
        }
    }

    private void checkUserExist() {

        if(mAuth.getCurrentUser() != null){
            String user_id = mAuth.getCurrentUser().getUid();
            DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("Customer").child(user_id);

            mUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("phone_number")){
                        Intent mainIntent = new Intent(CustomerLoginActivity.this,CustomerMapsActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                    }else {
                        Intent phoneIntent = new Intent(CustomerLoginActivity.this,PhoneNumberActivity.class);
                        phoneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(phoneIntent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }
}
