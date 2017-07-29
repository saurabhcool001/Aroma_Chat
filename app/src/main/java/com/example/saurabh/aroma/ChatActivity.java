package com.example.saurabh.aroma;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
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
    private DatabaseReference mDatabaseRef, mChat, mRoofRef;
    private String current_userId, chat_user_id, userName;

    private TextView mChatDisplayName, mChatLastSeen;
    private CircleImageView mProfileImage;
    private ImageButton mChatAddBtn, mChatSendBtn;
    private EditText mChatMessageInput;
    private ImageView mChatStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = getApplication();
        FirebaseApp.initializeApp(context);

        mToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);

        Bundle extras = getIntent().getExtras();
        chat_user_id = extras.getString("user_id").toString();
        String userName = extras.getString("userName").toString();

        mAuth = FirebaseAuth.getInstance();
        current_userId = mAuth.getCurrentUser().getUid();
        mDatabaseRef = Database.getReference().child("Users");
        mChat = Database.getReference().child("Chat");

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

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_addBtn);
        mChatMessageInput = (EditText) findViewById(R.id.chat_message_input);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_sendBtn);

        mDatabaseRef.child(chat_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                final String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

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

        mChat.child(current_userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(chat_user_id)) {

                    Map<String, Object> chatAdd = new HashMap<String, Object>();
                    chatAdd.put("seen", false);
                    chatAdd.put("timestamp", ServerValue.TIMESTAMP);

                    Map<String, Object> chatUserMap = new HashMap<String, Object>();
                    chatUserMap.put("Chat/" + current_userId + "/" + chat_user_id, chatAdd);
                    chatUserMap.put("Chat/" + chat_user_id + "/" + current_userId, chatAdd);

                    mChat.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.i("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /* CHAT SEND BUTTON */
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String messsage = mChatMessageInput.getText().toString();
        mRoofRef = Database.getReference();
        if (!TextUtils.isEmpty(messsage)) {
            String current_userRef = "messages/" + current_userId + "/" + chat_user_id;
            String chat_userRef = "messages/" + chat_user_id + "/" + current_userId;

            DatabaseReference push_user_message = mRoofRef.child("messages").child(current_userId)
                    .child(chat_user_id).push();

            String push_id = push_user_message.getKey();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("messages", messsage);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(current_userRef + "/" + push_id, messageMap);
            messageUserMap.put(chat_userRef + "/" + push_id, messageMap);

            mRoofRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.i("CHAT_LOG", databaseError.getMessage().toString());
                    }else {
                        mChatMessageInput.setText("");
                    }
                }
            });
        }
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
}
