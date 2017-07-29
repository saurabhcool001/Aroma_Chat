package com.example.saurabh.aroma;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.FirebaseApp;

public class StartActivity extends AppCompatActivity {

    Context context;

    private Button mStartRegisterBtn;
    private Button mStartLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        context = getApplicationContext();
        FirebaseApp.initializeApp(context);

        mStartRegisterBtn = (Button) findViewById(R.id.start_register_btn);
        mStartLoginBtn = (Button) findViewById(R.id.start_login_btn);

        mStartRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                finish();

            }
        });

        mStartLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();

            }
        });
    }
}
