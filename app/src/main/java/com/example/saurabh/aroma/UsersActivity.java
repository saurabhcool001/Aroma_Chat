package com.example.saurabh.aroma;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.saurabh.aroma.MainActivity.Tag;

public class UsersActivity extends AppCompatActivity {

    Context context;

    private Toolbar mToolbar;
    private RecyclerView mUserRecyclerList;

    private FirebaseAuth mAuth;
    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef, mUserRef;
    private String current_userId;

    private TextView mUserSingleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        mAuth = FirebaseAuth.getInstance();
        current_userId = mAuth.getCurrentUser().getUid();
        mDatabaseRef = Database.getReference().child("Users");
        mUserRef = Database.getReference().child("Users").child(current_userId);

        mToolbar = (Toolbar) findViewById(R.id.user_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserRecyclerList = (RecyclerView) findViewById(R.id.user_recycler_list);
        mUserRecyclerList.setHasFixedSize(true);
        mUserRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        mDatabaseRef.keepSynced(true);

        mUserSingleName = (TextView) findViewById(R.id.user_single_name);

        mUserRef.child("online").setValue(true);
    }

    private void sendToStart() {
        Intent startIntent = new Intent(UsersActivity.this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                        Users.class,
                        R.layout.user_single_layout,
                        UsersViewHolder.class,
                        mDatabaseRef
                ) {
                    @Override
                    protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                        final String user_id = getRef(position).getKey();

                        if (user_id.equals(current_userId)) {

//                            viewHolder.setName("");
//                            viewHolder.setStatus("");
                        } else {
                            viewHolder.setName(model.getName());
                            viewHolder.setStatus(model.getStatus());
                            //viewHolder.setImage(getApplicationContext(), model.getImage());
                            viewHolder.setImage(getApplicationContext(), model.getThumb_image());

                            //final String user_id = getRef(position).getKey();

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                                    profileIntent.putExtra("user_id", user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        }
                    }
                };

        mUserRecyclerList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatus = (TextView) mView.findViewById(R.id.user_single_status);
            userStatus.setText(status);
        }

//        public void setImage(Context context, String image) {
//            ImageView userImage = (ImageView) mView.findViewById(R.id.user_single_image);
//            Picasso.with(context).load(image).placeholder(R.mipmap.user_image_transparent).into(userImage);
//        }

        public void setImage(Context context, String thumb_image) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(context).load(thumb_image).placeholder(R.mipmap.user_image_transparent).into(userImageView);
        }
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
