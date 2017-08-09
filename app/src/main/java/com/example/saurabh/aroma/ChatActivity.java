package com.example.saurabh.aroma;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.google.android.gms.auth.api.Auth;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    Context context;

    private Toolbar mToolbar;
    private ActionBar mActionBar;

    private String user_id;
    private FirebaseAuth mAuth;
    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef, mChat, mRoofRef, mRootRefDb;
    private String current_userId, chat_user_id, userName;

    private TextView mChatDisplayName, mChatLastSeen;
    private CircleImageView mProfileImage;
    private ImageButton mChatAddBtn, mChatSendBtn;
    private EditText mChatMessageInput;
    private ImageView mChatStatus;
    private String thumbImage, login_thumbImage;

    private static final String TAG = "CHAT_ACTIVITY";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 100;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername, login_userName;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    //    private GoogleApiClient mGoogleApiClient;
    private static final String MESSAGE_URL = "http://aroma.firebase.google.com/message/";

    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;

    private final List<FriendlyMessage> messagesList = new ArrayList<>();
    private MessageAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        context = getApplication();
        FirebaseApp.initializeApp(context);

        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, StartActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);

        Bundle extras = getIntent().getExtras();
        chat_user_id = extras.getString("user_id").toString();
        final String userName = extras.getString("userName").toString();

        mAuth = FirebaseAuth.getInstance();
        current_userId = mAuth.getCurrentUser().getUid();
        mDatabaseRef = Database.getReference().child("Users");
        mChat = Database.getReference().child("Chat");
        mRootRefDb = Database.getReference();

        //getSupportActionBar().setTitle(userName);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        mActionBar.setCustomView(action_bar);

        /* ACTIONBAR */
        mChatDisplayName = (TextView) findViewById(R.id.chat_display_name);
        mChatLastSeen = (TextView) findViewById(R.id.chat_user_lastseen);
        mProfileImage = (CircleImageView) findViewById(R.id.chat_user_image);
        mChatStatus = (ImageView) findViewById(R.id.chat_status);

        mChatDisplayName.setText(userName);

        mChatAddBtn = (ImageButton) findViewById(R.id.addMessageImageView);
        mChatMessageInput = (EditText) findViewById(R.id.messageEditText);
        mChatSendBtn = (ImageButton) findViewById(R.id.sendButton);

        /*GOOGLE LOGIN*/
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//               // .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                .addApi(Auth.GOOGLE_SIGN_IN_API)
//                .build();

        mAdapter = new MessageAdapter(getApplicationContext(),messagesList);

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mAdapter);

        loadMessages();


        // New child entries
        mFirebaseDatabaseReference = Database.getReference();
        final DatabaseReference mRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(current_userId).child(chat_user_id);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(SaurabhPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mChatSendBtn.setEnabled(true);
                } else {
                    mChatSendBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        //mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        //mMessageRecyclerView.setAdapter(mAdapter);

        mDatabaseRef.child(current_userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                login_userName = dataSnapshot.child("name").getValue().toString();
                login_thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        /* USER CLICK ON PROFILE ICON SEND TO PROFILE PAGE */
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingIntent = new Intent(ChatActivity.this, ProfileActivity.class);
                settingIntent.putExtra("user_id", chat_user_id);
                startActivity(settingIntent);
            }
        });


        /* OTHER */

        mDatabaseRef.child(chat_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                if (thumbImage != "default") {
                    Picasso.with(ChatActivity.this).load(thumbImage)
                            .placeholder(R.mipmap.user_image_transparent).into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            //Log.i("image_setting", "onDataChange: 0" + image);
                            Picasso.with(ChatActivity.this).load(thumbImage).placeholder(R.mipmap.user_image_white).into(mProfileImage);
                        }

                        @Override
                        public void onError() {
                            //Log.i("image_setting", "onDataChange: 1 " + image);
                        }
                    });
                }

                if (online.equals("true")) {
                    mChatLastSeen.setText("Online");
                    mChatStatus.setVisibility(View.VISIBLE);
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mChatLastSeen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRef.child(current_userId).child("online").setValue(true);

//        mChat.child(current_userId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.hasChild(chat_user_id)) {
//
//                    Map<String, Object> chatAdd = new HashMap<String, Object>();
//                    chatAdd.put("seen", false);
//                    chatAdd.put("timestamp", ServerValue.TIMESTAMP);
//
//                    Map<String, Object> chatUserMap = new HashMap<String, Object>();
//                    chatUserMap.put("Chat/" + current_userId + "/" + chat_user_id, chatAdd);
//                    chatUserMap.put("Chat/" + chat_user_id + "/" + current_userId, chatAdd);
//
//                    mChat.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
//                        @Override
//                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                            if (databaseError != null) {
//                                Log.i("CHAT_LOG", databaseError.getMessage().toString());
//                            }
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        /* CHAT SEND BUTTON */
//        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendMessage();
//            }
//        });
    }

    private void loadMessages() {
        mProgressBar.setVisibility(View.VISIBLE);

        mRootRefDb.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                boolean msgExits = dataSnapshot.child(current_userId).child(chat_user_id).exists();
                if (msgExits) {
                    Log.i(TAG, "Message Exits");
                }else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRefDb.child("messages").child(current_userId).child(chat_user_id).addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final String user_id = dataSnapshot.getRef().getKey();

                        FriendlyMessage message = dataSnapshot.getValue(FriendlyMessage.class);
                        String currentUserID = message.from.toString();

                        long count = dataSnapshot.getChildrenCount();
                        Toast.makeText(ChatActivity.this, "count : " + currentUserID, Toast.LENGTH_SHORT).show();

                        messagesList.add(message);
                        mAdapter.notifyDataSetChanged();
                        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
                        mMessageRecyclerView.setAdapter(mAdapter);
                        mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void sendMessage() {
        final String messsage = mChatMessageInput.getText().toString();

        mRoofRef = Database.getReference();
        if (!TextUtils.isEmpty(messsage)) {
            String current_userRef = "messages/" + current_userId + "/" + chat_user_id;
            String chat_userRef = "messages/" + chat_user_id + "/" + current_userId;

            DatabaseReference push_user_message = mRoofRef.child("messages").child(current_userId)
                    .child(chat_user_id).push();

            String push_id = push_user_message.getKey();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("name", login_userName);
            messageMap.put("photoUrl", login_thumbImage);
            messageMap.put("messages", messsage);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("imageUrl", null);
            messageMap.put("from", current_userId);
            messageMap.put("time", ServerValue.TIMESTAMP);

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(current_userRef + "/" + push_id, messageMap);
            messageUserMap.put(chat_userRef + "/" + push_id, messageMap);

            mRoofRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i("CHAT_LOG", databaseError.getMessage().toString());
                    } else {
                        mChatMessageInput.setText("");
                    }
                }
            });
        }

        //mChatuser is the one who receives the notification
        mRoofRef.child("Users").child(chat_user_id).child("online").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.getValue().toString();

                //Send notification only if the user is not online

                if(!online.equals("true")){


                    //Uid is the id of current logged in user sending the message

                    Map notimap = new HashMap();
                    notimap.put("from",current_userId);
                    notimap.put("type","message");
                    notimap.put("message",messsage);

                    //Message is uploaded to child "MessageNoti" of which we set a onwrite trigger function in JS.

                    mRoofRef.child("Notification").child(chat_user_id).push().setValue(notimap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendToStart() {
        Intent startIntent = new Intent(ChatActivity.this, StartActivity.class);
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
        mDatabaseRef.child(current_userId).child("online").setValue(ServerValue.TIMESTAMP);
    }

    /*CHAT*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());

                    final FriendlyMessage tempMessage = new FriendlyMessage(null, login_userName, login_thumbImage,
                            LOADING_IMAGE_URL, "text", ServerValue.TIMESTAMP, false);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(current_userId).child(chat_user_id).push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {

                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(current_userId)
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());

                                        putImageInStorage(storageReference, uri, key);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.",
                                                databaseError.toException());
                                    }
                                }
                            });
                }

            }
        }


    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(ChatActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            FriendlyMessage friendlyMessage =
                                    new FriendlyMessage(null, login_userName, login_thumbImage,
                                            task.getResult().getMetadata().getDownloadUrl()
                                                    .toString(), "image", ServerValue.TIMESTAMP, false);
                            mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(current_userId).child(chat_user_id).child(key)
                                    .setValue(friendlyMessage);
                            mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(chat_user_id).child(current_userId).child(key)
                                    .setValue(friendlyMessage);
                        } else {
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());
                        }
                    }
                });
    }

}
