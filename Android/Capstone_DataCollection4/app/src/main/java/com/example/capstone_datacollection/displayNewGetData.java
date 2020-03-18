package com.example.capstone_datacollection;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;



// display data from temporary space
// save data into DB

public class displayNewGetData extends AppCompatActivity {

    private Button btnDisplayAll;
    private Button btnDisplayByDate;
    private TextView requestDisplay;
    private TextView responseDisplay;
    private Button btnSave;
    private static final String TAG = "display New Data";



    DatabaseHelper myDb;
    String MyStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_new_get_data);


        btnDisplayAll = (Button) findViewById(R.id.btnDisplayAll);
        btnDisplayByDate = (Button) findViewById(R.id.btnDisplayByDate);
        requestDisplay = (TextView) findViewById(R.id.requestDisplay);
        requestDisplay.setText(" ");

        responseDisplay = (TextView) findViewById(R.id.responseDisplay);
        responseDisplay.setText(" ");
        btnSave = (Button) findViewById(R.id. btnSave);




        myDb = new DatabaseHelper(this);
        MyStation = getIntent().getStringExtra("STATION_NAME");
        setTitle(MyStation.equals("01") ? "Station 1" : "Station 2");
        //saveToDB();

        btnDisplayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dumpDatabase();
            }
        });


    }

    /**
     * Stop the Bluetooth service when the app loses focus
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth service
       // if (getNewDataActivity.mBluetoothService != null) getNewDataActivity.mBluetoothService.stop();
    }





    public void saveToDB() {
        /*btnSave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //boolean isInserted = myDb.insertData("01", "2019", S1, S2, S3);
                        if (MyStation.equals("01")) {
                            boolean isInserted = myDb.insertData(MyStation, "2019", S1, S2, S3);
                            if (isInserted == true)
                                Toast.makeText(displayNewGetData.this, "S1 Data Inserted", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(displayNewGetData.this, "S1 Data not Inserted", Toast.LENGTH_LONG).show();
                        } else if (MyStation.equals("02")){

                            boolean isInserted2 = myDb.insertData(MyStation, "2019", S4, S5, S6);
                            if (isInserted2 == true)
                                Toast.makeText(displayNewGetData.this, "S2 Data Inserted", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(displayNewGetData.this, "S2 Data not Inserted", Toast.LENGTH_LONG).show();
                        }
                    }


                }
        );*/


    }


    public void dumpDatabase() {
        //getNewDataActivity.mBluetoothService.dump();
    }


    public void query(int year, int month, int day) {
        //getNewDataActivity.mBluetoothService.query(year, month, day);
    }


    public void end() {
        //getNewDataActivity.mBluetoothService.end();
    }




    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

               case getNewDataActivity.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                   String writeMessage = new String(writeBuf);
                   Log.d(TAG, "WriteMessage: "+writeMessage);

                    displayRequest(writeMessage);

                    break;
                case getNewDataActivity.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, "ReadMessage: "+readMessage);

                    displayResponse(readMessage);

                    break;
                case getNewDataActivity.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(getNewDataActivity.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private void displayRequest(String request) {

        requestDisplay.setText("Request: \n" + request);
    }
    /**
     * Display the response from the server
     */
    private void displayResponse(String response) {

        responseDisplay.setText("Response: \n" + response);

    }





}





