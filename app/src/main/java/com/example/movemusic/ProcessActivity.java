package com.example.movemusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class ProcessActivity extends AppCompatActivity {

    private static final String TAG = "ProcessActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        // --- selected platform ---
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String platform = sharedPref.getString(getString(R.string.saved_platform_key), "");
        Log.d(TAG, "onCreate: " + platform);

        // --- selected picture ---
        Intent processIntent = getIntent();
        String imgPath = processIntent.getStringExtra("imgPath");
        Log.d(TAG, "onCreate: " + imgPath);
    }
}