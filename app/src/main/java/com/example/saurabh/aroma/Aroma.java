package com.example.saurabh.aroma;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by saurabh on 23-07-2017.
 */

public class Aroma extends Application {

    Context context;

    private FirebaseDatabase Database;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuthUser;
    private String current_UserId;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        Database = FirebaseDatabase.getInstance();
        mAuthUser = FirebaseAuth.getInstance();
        current_UserId = mAuthUser.getCurrentUser().getUid();
        mUserDatabase = Database.getReference().child("Users").child(current_UserId);

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Picasso

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        if (mAuthUser.getCurrentUser() != null) {

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        //mUserDatabase.child("online").onDisconnect().setValue(false);
                        mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        //mUserDatabase.child("lastSeen").setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
