package com.example.breezil.pickop.ui;



import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.service.carrier.CarrierIdentifier;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.breezil.pickop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class UserSettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView mUserImage;
    private ImageButton mChooseImage;
    private EditText mFirstName, mLastName, mEmailText, mPhoneNumber;
    private Button mSaveBtn;


    String mUserFirstName;
    String mUserLastName;
    String mUserEmail;
    String mUserPhoneNumber;

    BottomSheetDialog mBottomSheet;

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    Uri imageUri;

    Uri mCameraURI;

    byte[] thumb_byte;
    String mCurrentPhotoPath;
    String mUid;

    Uri mSaveImage;


    private ProgressDialog mProgress;

    private StorageReference mProfImageStorage;
    DatabaseReference mCustomerInfo;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        mToolbar = findViewById(R.id.userSettingsBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if(getIntent() != null){
            mUserFirstName = getIntent().getStringExtra("first_name");
            mUserLastName = getIntent().getStringExtra("last_name");
            mUserEmail = getIntent().getStringExtra("email");
            mUserPhoneNumber = getIntent().getStringExtra("phone_number");
        }

        //firebase storage
        mProfImageStorage = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();

        mUid = mAuth.getCurrentUser().getUid();

        mCustomerInfo = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(mUid);

        mProgress = new ProgressDialog(this);

        mUserImage = findViewById(R.id.userSettingImage);
        mChooseImage = findViewById(R.id.chooseSettingImage);
        mFirstName = findViewById(R.id.customerSettingsFirstName);
        mLastName = findViewById(R.id.customerSettingsLastName);
        mEmailText = findViewById(R.id.customerSettingsEmail);
        mPhoneNumber = findViewById(R.id.customerSettingsNumber);

        mSaveBtn = findViewById(R.id.ssettingsSaveBtn);


        mFirstName.setText(mUserFirstName);
        mLastName.setText(mUserLastName);
        mEmailText.setText(mUserEmail);
        mPhoneNumber.setText(mUserPhoneNumber);

        mProfImageStorage = FirebaseStorage.getInstance().getReference();


        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUpdate();
            }
        });

        mChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttachmentOptions();
            }
        });

        loadUserInfo();



    }

    private void loadUserInfo() {
        mCustomerInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String image = dataSnapshot.child("image").getValue().toString();
                    if(image != null){
                        if(!image.equals("default")){
                            Glide.with(getApplication()).load(image).into(mUserImage);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



    private void showAttachmentOptions() {
        mBottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = this.getLayoutInflater().inflate(R.layout.choose_image_buttom_sheet,null);

        mBottomSheet.setContentView(bottomSheetView);
        mBottomSheet.show();



        LinearLayout selectGallery = bottomSheetView.findViewById(R.id.select_gallery);
        LinearLayout selectCamera =  bottomSheetView.findViewById(R.id.select_camera);

        selectGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                if(galleryIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(Intent.createChooser(galleryIntent,"Choose Image"),GALLERY_REQUEST_CODE);

                }

            }
        });

        selectCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if(cameraIntent.resolveActivity(getPackageManager()) != null){
//                    startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
//                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        ex.printStackTrace();
                        return;
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraURI = FileProvider.getUriForFile(UserSettingsActivity.this,
                                getPackageName() +".provider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraURI);
                        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                    }
                }
            }
        });
    }

    private void saveUpdate() {
        final String firstname = mFirstName.getText().toString();
        final String lastname = mLastName.getText().toString();
        final String email = mEmailText.getText().toString();
        final String phone = mPhoneNumber.getText().toString();

        if(imageUri != null){
            mSaveImage = imageUri;
        }

        mProgress.setTitle("Uploading Image");
        mProgress.setMessage("please wait...");
        mProgress.show();
        mProgress.setCanceledOnTouchOutside(false);


        //path to store in firebase storage
        StorageReference filePath = mProfImageStorage.child("Customer").child("profileImages").child(mUid + ".jpg");
        final StorageReference thumb_path = mProfImageStorage.child("Customer").child("profileImages").child("thumbs").child(mUid + ".jpg");
        final DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(mUid);

        filePath.putFile(mSaveImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    final String image_url = task.getResult().getDownloadUrl().toString();
                    UploadTask uploadTask = thumb_path.putBytes(thumb_byte);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            final String thumb_url = task.getResult().getDownloadUrl().toString();
                            if(task.isSuccessful()) {
                                mProgress.setTitle("Saving Informations ...");
                                mProgress.setMessage("please wait...");
                                mProgress.show();
                                mProgress.setCanceledOnTouchOutside(false);

                                Map<String,Object> customerMap = new HashMap<>();
                                customerMap.put("image",thumb_url);
                                customerMap.put("full_image",image_url);
                                customerMap.put("email",email);
                                customerMap.put("last_name",lastname);
                                customerMap.put("first_name",firstname);
                                customerMap.put("phone_number",phone);

                                mCustomerInfo.updateChildren(customerMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            mProgress.dismiss();
                                            Intent mainIntent = new Intent(UserSettingsActivity.this,CustomerMapsActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }else {
                    Toast.makeText(UserSettingsActivity.this, "Error in Image", Toast.LENGTH_LONG).show();
                    mProgress.dismiss();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {

            try {
                Uri photoUri = data.getData();
                // start cropping activity for pre-acquired image saved on the device
                CropImage.activity(photoUri)
                        .setAspectRatio(1, 1)
                        .start(this);
            }catch (Exception e){
               e.printStackTrace();
            }



        }else if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
//            Bundle extras = data.getExtras();
//            Bitmap cameraBitmap = (Bitmap) extras.get("data");

//                Uri cameraUri = data.getData();

                if(mCameraURI != null){
                    Uri cameraUri = mCameraURI;
                    // start cropping activity for pre-acquired image saved on the device
                    CropImage.activity(cameraUri)
                            .setAspectRatio(1, 1)
                            .start(this);
                }


        }

        //copied from Aurthur Edmondo github for crop action
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();

                mUserImage.setImageURI(imageUri);
                mBottomSheet.dismiss();

                //set a file path
                File thumb_filepath = new File(imageUri.getPath());



                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //from firebase doc upload bitmap method
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                thumb_byte = baos.toByteArray();



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }

        }
    }
}
