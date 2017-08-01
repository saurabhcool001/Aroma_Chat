package com.example.saurabh.aroma;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    Context context;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static final String Tag = "MAIN_ACTIVITY";
    private String current_userId;
    private FirebaseDatabase Database;
    private DatabaseReference mUserRef;

    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Aroma Chat");

        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mSectionPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//        @Override
//        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//            FirebaseUser user = firebaseAuth.getCurrentUser();
//            if (user != null) {
//
//                // User is signed in
//                Log.d(Tag, "onAuthStateChanged:signed_in:" + user.getUid());
//                current_userId = mAuth.getCurrentUser().getUid();
//                Database = FirebaseDatabase.getInstance();
//                mUserRef = Database.getReference().child("Users").child(current_userId);
//                mUserRef.child("online").setValue(true);
//
//            } else if (user == null) {
//                // User is signed out
//                Log.d(Tag, "onAuthStateChanged:signed_out");
//
//                sendToStart();
//
//            }
//        }
//    };
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startIntent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            // User is signed in
            Log.d(Tag, "onAuthStateChanged:signed_in:" + user.getUid());
            current_userId = mAuth.getCurrentUser().getUid();
            Database = FirebaseDatabase.getInstance();
            mUserRef = Database.getReference().child("Users").child(current_userId);
            mUserRef.child("online").setValue(true);

        } else {
            // User is signed out
            Log.d(Tag, "onAuthStateChanged:signed_out");

            sendToStart();

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        //mUserRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);
        //mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_btn) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if (item.getItemId() == R.id.main_setting_btn) {
            Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(settingIntent);
        }

        if (item.getItemId() == R.id.main_all_btn) {
            Intent userIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(userIntent);
        }

        return true;
    }
}
