package com.example.capstone_datacollection;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import java.io.File;

public class HomePage extends AppCompatActivity {
    private static int WELCOME_TIMEOUT = 1500;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_CHECKIN_PROPERTIES,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearFile();
        setContentView(R.layout.activity_home_page);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(HomePage.this,Login.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                finish();
            }
        },WELCOME_TIMEOUT );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        clearFile();

    }

    private void clearFile(){
        File dir = new File(Environment.getExternalStorageDirectory()+"/Data_folder/");
        if (dir.isDirectory()){
            String[] children = dir.list();
            for(int i = 0; i < children.length;i++){
                new File(dir,children[i]).delete();
            }
        }
    }


}
