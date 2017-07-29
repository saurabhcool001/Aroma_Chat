package com.example.saurabh.aroma;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import static com.example.saurabh.aroma.MainActivity.Tag;

public class ProfileActivity extends AppCompatActivity {

    Context context;

    private ImageView mProfileImage;
    private TextView mProfileName;
    private TextView mProfileStatus;
    private TextView mProfileFriendsCount;
    private Button mSendFriendRequestBtn;
    private Button mDeclineBtn;
    private ProgressDialog mProgress;
    private String mCurrent_state;

    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef, mFriendRequestDatabase, mFriendDatabse, mNotificationDatabase, mRootRef, mUserRef;
    private FirebaseUser mCurrentUser;
    private String current_userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        final String user_id = getIntent().getStringExtra("user_id").toString();
        Log.i("user_id", "onCreate: " + user_id);

        mDatabaseRef = Database.getReference().child("Users").child(user_id);
        mFriendRequestDatabase = Database.getReference().child("Friend_request");
        mFriendDatabse = Database.getReference().child("Friends");
        mRootRef = Database.getReference();
        mNotificationDatabase = Database.getReference().child("Notification");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_userId = mCurrentUser.getUid();
        mUserRef = Database.getReference().child("Users").child(current_userId);

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_display_name);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mSendFriendRequestBtn = (Button) findViewById(R.id.profile_request_send_btn);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_friend_request_btn);

        mCurrent_state = "not_friend";

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading User Info");
        mProgress.setMessage("Please wait while we load the user info");
        mProgress.setCanceledOnTouchOutside(true);
        mProgress.show();

        mDatabaseRef.keepSynced(true);
        mFriendRequestDatabase.keepSynced(true);
        mFriendDatabse.keepSynced(true);

        if (user_id.equals(current_userId)) {
            mSendFriendRequestBtn.setVisibility(View.INVISIBLE);
        }
        //Toast.makeText(this, "user_id : " + user_id, Toast.LENGTH_SHORT).show();
        mFriendDatabse.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final int totalFriends = (int) dataSnapshot.getChildrenCount();
                //Toast.makeText(ProfileActivity.this, "Total Friends : " + totalFriends, Toast.LENGTH_SHORT).show();
                String newVal = Integer.toString(totalFriends);
                mProfileFriendsCount.setText("Total Friend : " + newVal);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.mipmap.user_image_transparent).into(mProfileImage);

                // FRIEND LIST / REQUEST FEATURE
                mFriendRequestDatabase.child(current_userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (request_type.equals("received")) {
                                mCurrent_state = "req_received";
                                mSendFriendRequestBtn.setText("ACCEPT FRIEND REQUEST");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            } else if (request_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mSendFriendRequestBtn.setText("CANCLE FRIEND REQUEST");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgress.dismiss();
                        } else {
                            mFriendDatabse.child(current_userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrent_state = "friends";
                                        mSendFriendRequestBtn.setText("UNFRIEND THIS PERSON");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                    mProgress.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgress.dismiss();
                                }
                            });
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


        mSendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSendFriendRequestBtn.setEnabled(false);

                // NOT FRIEND STATE
                if (mCurrent_state.equals("not_friend")) {
                    mFriendRequestDatabase.child(current_userId).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                mFriendRequestDatabase.child(user_id).child(current_userId).child("request_type")
                                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        HashMap<String, String> notificationData = new HashMap<String, String>();
                                        notificationData.put("from", current_userId);
                                        notificationData.put("type", "request");
                                        mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnCompleteListener(
                                                new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        mSendFriendRequestBtn.setEnabled(true);
                                                        mCurrent_state = "req_sent";
                                                        mSendFriendRequestBtn.setText("CANCEL FRIEND REQUEST");
                                                        //set button color remaining

                                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                                        mDeclineBtn.setEnabled(false);
                                                    }
                                                }
                                        );

//                                        mSendFriendRequestBtn.setEnabled(true);
//                                        mCurrent_state = "req_sent";
//                                        mSendFriendRequestBtn.setText("CANCEL FRIEND REQUEST");
//                                        //set button color remaining
//
//                                        mDeclineBtn.setVisibility(View.INVISIBLE);
//                                        mDeclineBtn.setEnabled(false);

                                        Toast.makeText(ProfileActivity.this, "Request Sent Successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                /*UNFRIEND*/
                if (mCurrent_state.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + current_userId + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + current_userId, null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mCurrent_state = "not_friend";
                                mSendFriendRequestBtn.setText("Send friend Request");
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            mSendFriendRequestBtn.setEnabled(true);
                        }
                    });
                }

                /*REQUEST RECEIVED STATE*/
                if (mCurrent_state.equals("req_received")) {
                    Log.i("Request_State", "onCreate: 1");
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabse.child(current_userId).child(user_id).child("date").setValue(currentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mFriendDatabse.child(user_id).child(current_userId).child("date").setValue(currentDate)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mFriendRequestDatabase.child(current_userId).child(user_id).child("request_type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            mFriendRequestDatabase.child(user_id).child(current_userId).child("request_type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    mSendFriendRequestBtn.setEnabled(true);
                                                                    mCurrent_state = "friends";
                                                                    mSendFriendRequestBtn.setText("UNFRIEND THIS PERSON");

                                                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                                                    mDeclineBtn.setEnabled(false);
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                }

                // CANCEL REQUEST STATE
                if (mCurrent_state.equals("req_sent")) {
                    mFriendRequestDatabase.child(current_userId).child(user_id).child("request_type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            mFriendRequestDatabase.child(user_id).child(current_userId).child("request_type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mSendFriendRequestBtn.setEnabled(true);
                                    mCurrent_state = "not_friend";
                                    mSendFriendRequestBtn.setText("SEND FRIEND REQUEST");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                    //Toast.makeText(ProfileActivity.this, "Request send", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });
                }

            }
        });

        /*DECLINE FRIEND REQUEST*/
        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map declineFriendMap = new HashMap();
                declineFriendMap.put("Friend_request/" + current_userId + "/" + user_id, null);
                declineFriendMap.put("Friend_request/" + user_id + "/" + current_userId, null);
                mRootRef.updateChildren(declineFriendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError == null) {

                            mCurrent_state = "not_friend";
                            mSendFriendRequestBtn.setText("Send friend Request");
                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);

                        } else {
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                        mSendFriendRequestBtn.setEnabled(true);
                    }
                });
            }
        });

        // CHECK REQUEST SENT OR NOT
        mFriendRequestDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("Request_State", "onCreate: 0" + mCurrent_state);
                mFriendRequestDatabase.child(current_userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            Log.i("Request_State", "onCreate: 0" + mCurrent_state + " " + request_type);
                            if (request_type.equals("received")) {
                                mCurrent_state = "req_received";
                                mSendFriendRequestBtn.setText("ACCEPT FRIEND REQUEST");
                                Log.i("Request_State", "onCreate: 1" + mCurrent_state);

                            } else if (request_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mSendFriendRequestBtn.setText("CANCLE FRIEND REQUEST");
                                Log.i("Request_State", "onCreate: 2" + mCurrent_state);
                            }
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
        mUserRef.child("online").setValue(true);
    }

    private void sendToStart() {
        Intent startIntent = new Intent(ProfileActivity.this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mUserRef.child("online").setValue(false);
    }
}