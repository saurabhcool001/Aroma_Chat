package com.example.saurabh.aroma;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static com.example.saurabh.aroma.MainActivity.Tag;

public class SettingActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    Context context;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef, mFriendDatabase;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef;
    private String current_userId;

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private TextView mUserTotalFriend;
    private Button mSettingImageBtn;
    private Button mSettingStatusBtn;
    private Uri mImageUri = null;
    private String uid;
    private ProgressDialog mProgress;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        mToolbar = (Toolbar) findViewById(R.id.activity_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("User Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = mCurrentUser.getUid();
        mDatabaseRef = Database.getReference().child("Users").child(uid);
        mStorageRef = storage.getReference().child("Profile_Images");
        mFriendDatabase = Database.getReference().child("Friends").child(uid);
        mDatabaseRef.keepSynced(true);

        mName = (TextView) findViewById(R.id.setting_display_name);
        mDisplayImage = (CircleImageView) findViewById(R.id.setting_image);
        mStatus = (TextView) findViewById(R.id.setting_status);
        mSettingImageBtn = (Button) findViewById(R.id.setting_image_btn);
        mSettingStatusBtn = (Button) findViewById(R.id.setting_status_btn);
        mUserTotalFriend = (TextView) findViewById(R.id.userTotalFriend);

        mProgress = new ProgressDialog(this);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

//                Toast.makeText(SettingActivity.this, "DataSnapShot : " + dataSnapshot.toString(), Toast.LENGTH_SHORT).show();
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals("default")) {

                    //Log.i("image_setting", "onDataChange: " + image);
                    //Picasso.with(SettingActivity.this).load(image).placeholder(R.mipmap.user_image_transparent).into(mDisplayImage);
//                    Picasso.with(SettingActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
//                            .placeholder(R.mipmap.user_image_transparent).into(mDisplayImage, new Callback() {
                    Picasso.with(SettingActivity.this).load(image)
                            .placeholder(R.mipmap.user_image_transparent).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            //Log.i("image_setting", "onDataChange: 0" + image);
                            Picasso.with(SettingActivity.this).load(image).placeholder(R.mipmap.user_image_transparent).into(mDisplayImage);
                        }

                        @Override
                        public void onError() {
                            //Log.i("image_setting", "onDataChange: 1 " + image);
                        }
                    });

                }

                mFriendDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final int totalFriendCount = (int) dataSnapshot.getChildrenCount();
                        if (totalFriendCount == 0) {

                        }else {
                            mUserTotalFriend.setText("Total Friends : " + totalFriendCount);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSettingStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = mStatus.getText().toString();
                String status_display_name = mName.getText().toString();

                Intent statusIntent = new Intent(SettingActivity.this, StatusActivity.class);
                Bundle extras = new Bundle();
                extras.putString("status_value", status_value);
                extras.putString("status_display_name", status_display_name);
                statusIntent.putExtras(extras);
                startActivity(statusIntent);
            }
        });

        mSettingImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT_IMAGE"), GALLERY_REQUEST);
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingActivity.this);
            }
        });

        mDatabaseRef.keepSynced(true);
        mDatabaseRef.child("online").setValue(true);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
//            String imageUri = data.getDataString();
            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setAspectRatio(1, 1)
                    .setFixAspectRatio(true)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgress.setTitle("Uploading Image");
                mProgress.setMessage("Please wait while we upload image");
                mProgress.setCanceledOnTouchOutside(true);
                mProgress.show();

                Uri resultUri = result.getUri();

                //zetbaitsu image Compressor
                final File thumb_filepath = new File(resultUri.getPath());
                byte[] thumb_bite = new byte[0];
                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(80)
                            .compressToBitmap(thumb_filepath);

                    //Convert data to Byte
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_bite = baos.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //StorageReference filePath = mStorageRef.child(mImageUri.getLastPathSegment());
                StorageReference filePath = mStorageRef.child(uid + ".jpg");
                final StorageReference thumb_filePath_ref = mStorageRef.child("thumb").child(uid + ".jpg");

                final byte[] finalThumb_bite = thumb_bite;
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            @SuppressWarnings("VisibleForTests") final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath_ref.putBytes(finalThumb_bite);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    @SuppressWarnings("VisibleForTests") final String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful()) {

                                        Map<String, Object> user_hashMap = new HashMap<String, Object>();
                                        user_hashMap.put("image", downloadUrl);
                                        user_hashMap.put("thumb_image", thumb_downloadUrl);

                                        mDatabaseRef.updateChildren(user_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    mProgress.dismiss();
                                                    Toast.makeText(SettingActivity.this, "Uploading Success", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SettingActivity.this, "Error in uploading thumbnail.", Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(SettingActivity.this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mDatabaseRef.child("online").setValue(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabaseRef.child("online").setValue(ServerValue.TIMESTAMP);
    }
}
