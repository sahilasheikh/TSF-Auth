package com.begawoinc.tsfauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Registration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        getSupportActionBar().hide();




    }

    public void login(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        this.finish();
    }
}