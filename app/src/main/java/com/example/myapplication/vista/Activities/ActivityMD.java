package com.example.myapplication.vista.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.R;

public class ActivityMD extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_md);
        getSupportActionBar().setTitle("Mensajes Directos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }



}