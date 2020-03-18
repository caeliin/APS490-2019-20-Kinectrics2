package com.example.capstone_datacollection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class getHistoryDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_history_data);
        setTitle("Get History Data");


    }

    public void station1(View view) {
        Intent intent = new Intent(this, displayHistoryData.class);
        intent.putExtra("STATION_NAME", "01");
        startActivity(intent);
    }

    public void station2(View view) {
        Intent intent = new Intent(this, displayHistoryData.class);
        intent.putExtra("STATION_NAME", "02");
        startActivity(intent);
    }

}
