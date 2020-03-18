package com.example.capstone_datacollection;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);



    }

    /** Called when the user taps the Send button */
    public void getHistoryData(View view) {
        Intent intent = new Intent(this, getHistoryDataActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the Send button */
    public void getNewData(View view) {
        Intent intent = new Intent(this, getNewDataActivity.class);
        startActivity(intent);
    }


}
