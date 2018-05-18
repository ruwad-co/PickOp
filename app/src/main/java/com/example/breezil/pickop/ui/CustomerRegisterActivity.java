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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class CustomerRegisterActivity extends AppCompatActivity {

    private EditText mCustomerUserNameText, mCustomerEmailText, mCustomerPasswordText, mCustomerConfirmPasswordText;

    private Button mCustomerCreateBtn;
    private TextView mSignIn;

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mDataref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        mAuth = FirebaseAuth.getInstance();

        mSignIn = findViewById(R.id.customerGotoSignIn);
        mCustomerUserNameText = findViewById(R.id.customerRegUserNameText);
        mCustomerEmailText = findViewById(R.id.customerRegEmailText);
        mCustomerPasswordText = findViewById(R.id.customerRegPasswordText);
        mCustomerConfirmPasswordText = findViewById(R.id.customerConfirmRegPasswordText);

        progressDialog = new ProgressDialog(this);

        mCustomerCreateBtn = findViewById(R.id.customerCreateAccBtn);

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerRegisterActivity.this, CustomerLoginActivity.class));
            }
        });

        mCustomerCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailtext = mCustomerEmailText.getText().toString().trim();
                String passwdtext = mCustomerPasswordText.getText().toString().trim();
                String confirmpasswdtext = mCustomerConfirmPasswordText.getText().toString().trim();
                String userNametext = mCustomerUserNameText.getText().toString().trim();

                registerUser(emailtext,passwdtext,confirmpasswdtext,userNametext);
            }
        });
    }

    private void registerUser(final String emailtext, final String passwdtext, String confirmpasswdtext, final String userNametext) {
        /*
         * TextUtils checks if require fields are empty or not
         * and then toast error message if empty
         * if its not empty we call the firebase user creation method with email and password
         */
        if (!TextUtils.isEmpty(emailtext) || !TextUtils.isEmpty(passwdtext) || !TextUtils.isEmpty(confirmpasswdtext) || !TextUtils.isEmpty(userNametext)){
            if(passwdtext.equals(confirmpasswdtext)){
                progressDialog.setMessage("Creating Account...");
                progressDialog.setTitle("Please wait");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                /*
                 * here createuser with email and password is called
                 */
                mAuth.createUserWithEmailAndPassword(emailtext,passwdtext).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //if user is successfully created, we create a user database
                        //that stores other informations of the user like username , profile image , status
                        if(task.isSuccessful()){
                            //get current user inorder to use it reference storing the user informations
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String Uid = current_user.getUid();

                            //firebase Token, this an instance of device token of a user so as to
                            //save its last session for firebase, if app is closed
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            //set database reference for the current user using the Uid of the current user
                            mDataref = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(Uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name",userNametext);
                            userMap.put("email",emailtext);
                            userMap.put("password",passwdtext);
                            userMap.put("deviceToken",deviceToken);

                            //here call the set value function to save the data structure in database
                            // attach oncomplete listener if successful call intent to the mainActivity
                            // and clear previous flags so it cant go back.

                            mDataref.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    progressDialog.dismiss();
                                    Intent mainIntent = new Intent(CustomerRegisterActivity.this,CustomerMapsActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });

                        }else {
                            Toast.makeText(CustomerRegisterActivity.this,"Unable to sign up please try again",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                Toast.makeText(CustomerRegisterActivity.this,"Password does not match",Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(CustomerRegisterActivity.this,"Please fill the fields..",Toast.LENGTH_LONG).show();
        }
    }
}
