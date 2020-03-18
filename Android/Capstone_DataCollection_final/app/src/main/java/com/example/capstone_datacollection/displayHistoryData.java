package com.example.capstone_datacollection;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Logger;

public class displayHistoryData extends AppCompatActivity {

    private String MyStation;


    private static int Message_Year = 0;
    private static int  Message_Month = 0;
    private static int  Message_Day = 0;


    private Button displayButton;
    private TextView responseDisplay;

    private Button export;

    private Button displayByDateButton;
    private DatePickerDialog datePicker = null;
    private TextView displayDate;


    DatabaseHelper myDb;



    // Storage Permissions
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
        setContentView(R.layout.activity_display_history_data);


        //myLayout = findViewById(R.id.my_layout);
        MyStation = getIntent().getStringExtra("STATION_NAME");
        // Capture the layout's TextView and set the string as its text

        myDb = new DatabaseHelper(this);

        setTitle(MyStation.equals("01") ? "Station 1" : "Station 2");
        displayButton = (Button) findViewById(R.id.displayButton);
        responseDisplay = (TextView) findViewById((R.id.responseDisplay));
        responseDisplay.setText("");


        displayDate = (TextView) findViewById(R.id.dateDisplay);
        displayDate.setText("");

        export = (Button) findViewById(R.id.btn_export);






        export.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                verifyStoragePermissions(displayHistoryData.this);

                AlertDialog.Builder altdial = new AlertDialog.Builder(displayHistoryData.this);
                String message;

                if(Message_Year==0&&Message_Month==0&&Message_Day==0){
                    message = "Do you want to export and share all the measured data?";
                }else{
                    message = "Do you want to export and share data starting from "+Message_Year+"-"+Message_Month+"-"+Message_Day+"?";
                }

                altdial.setMessage(message).setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                                    new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                                } else {

                                    new ExportDatabaseCSVTask().execute();
                                }
                                //finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });


                AlertDialog alert = altdial.create();
                alert.setTitle("Data export and share");
                alert.show();


            }
        });


        displayByDateButton = (Button) findViewById(R.id.btn_date);
        final Context thisContext = this;


        displayByDateButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                datePicker = new DatePickerDialog(thisContext);
                datePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Message_Year = year;
                        Message_Month = month+1;
                        Message_Day = day;

                        displayDataByDate(year, month+1,day);

                    }
                });

                datePicker.show();


            }
        });






        displayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                displayData();
                export.setEnabled(true);
            }
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        File dir = new File(Environment.getExternalStorageDirectory()+"/.Data_folder/");
        if (dir.isDirectory()){
            String[] children = dir.list();
            for(int i = 0; i < children.length;i++){
                new File(dir,children[i]).delete();
            }
        }


    }

    /*
    method to display all the data
     */
    public void displayData(){
        Message_Year = 0;
        Message_Month = 0;
        Message_Day = 0;
        String displayText;

        Cursor cursor = myDb.getStationData(MyStation.equals("01") ? "Station1" : "Station2");

        if (cursor.getCount() == 0) {
            displayText = "Nothing";
            responseDisplay.setText(displayText);
        }else{
            displayText = "";
            displayDate.setText("Display all data"+"\n"+"\n");


            while(cursor.moveToNext()) {
                displayText += "Year:" + cursor.getInt(2);
                displayText += " Month:" + cursor.getInt(3);
                displayText += " Day:" + cursor.getInt(4);
                displayText += " Time:" + cursor.getString(5);
                displayText += " Voltage:" + cursor.getFloat(6);

                displayText += "\n"+"\n";
            }

        }

        responseDisplay.setText(displayText);

    }
    /*
    method to display data by selected date
     */

    public void displayDataByDate(int year,int month, int day){
        Cursor cursor = myDb.getByKeyword(MyStation.equals("01") ? "Station1" : "Station2",year,month,day);
        String displayText;

        if (cursor.getCount() == 0) {
            displayDate.setText("Data starts from: "+year+"-"+month+"-"+day+"\n"+"\n");
            displayText = "Nothing";
            responseDisplay.setText(displayText);

        }else{
            displayText = "";
            displayDate.setText("Data starts from: "+year+"-"+month+"-"+day+"\n"+"\n");

            while(cursor.moveToNext()) {
                displayText += "Year:" + cursor.getInt(2);
                displayText += " Month:" + cursor.getInt(3);
                displayText += " Day:" + cursor.getInt(4);
                displayText += " Time:" + cursor.getString(5);
                displayText += " Voltage:" + cursor.getFloat(6);

                displayText += "\n"+"\n";
            }

        }

            responseDisplay.setText(displayText);

    }



    private void ShareFile() {

            File file = new File(Environment.getExternalStorageDirectory()+"/.Data_folder/"+"station" + MyStation + "_data"+Message_Year+Message_Month+Message_Day+".csv");


            Intent fileIntent = new Intent(android.content.Intent.ACTION_SEND);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            fileIntent.setType("text/csv");

            fileIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "station" + MyStation + "_data"+Message_Year+Message_Month+Message_Day+".csv");

            Uri uri = FileProvider.getUriForFile(displayHistoryData.this, BuildConfig.APPLICATION_ID + ".provider", file);

            fileIntent.putExtra(Intent.EXTRA_STREAM,uri);
            Log.d("URI",Uri.fromFile(file).toString());
            startActivityForResult(Intent.createChooser(fileIntent, "send email"),1);
            file.deleteOnExit();

    }
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1){
            File file = new File(Environment.getExternalStorageDirectory()+"/.Data_folder/"+"station" + MyStation + "_data"+Message_Year+Message_Month+Message_Day+".csv");
            file.delete();
        }
    }




    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(displayHistoryData.this);
        //DatabaseHelper myDb;
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
            //myDb = new DatabaseHelper(displayHistoryData.this);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            File dbFile = getDatabasePath("CollectedData3.db");
            File mFolder = new File(Environment.getExternalStorageDirectory(), "/.Data_folder/");
            if (!mFolder.exists()) {
                mFolder.mkdirs();
                mFolder.setExecutable(true);
                mFolder.setReadable(true);
                mFolder.setWritable(true);
            }

            File exportDir = new File(Environment.getExternalStorageDirectory(), "/.Data_folder/");

            if (!exportDir.exists()) { exportDir.mkdirs(); }
            File file = new File(exportDir, "station" + MyStation + "_data"+Message_Year+Message_Month+Message_Day+".csv");
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));


                if(Message_Year == 0 &&Message_Month == 0 && Message_Day==0){

                    Cursor curCSV = myDb.getStationData(MyStation.equals("01") ? "Station1" : "Station2");
                    if(curCSV == null){
                        return false;

                    }
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while(curCSV.moveToNext()) {
                        String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                        for(int i=0;i<curCSV.getColumnNames().length;i++)
                        {
                            mySecondStringArray[i] =curCSV.getString(i);
                        }
                        csvWrite.writeNext(mySecondStringArray);
                    }


                    csvWrite.close();
                    curCSV.close();

                }else{
                    Cursor curCSV = myDb.getByKeyword(MyStation.equals("01") ? "Station1" : "Station2",Message_Year,Message_Month,Message_Day);
                    if(curCSV == null){
                        return false;

                    }
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while(curCSV.moveToNext()) {
                        String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                        for(int i=0;i<curCSV.getColumnNames().length;i++)
                        {
                            mySecondStringArray[i] =curCSV.getString(i);
                        }
                        csvWrite.writeNext(mySecondStringArray);
                    }


                    csvWrite.close();
                    curCSV.close();
                }

                return true;
                } catch (IOException e) {
                Log.d("false: ",e.getMessage());
                return false;
            }


        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) { this.dialog.dismiss(); }
            if (success) {
                Toast.makeText(displayHistoryData.this, "Export successful!", Toast.LENGTH_SHORT).show();

                ShareFile();

            } else {
                Toast.makeText(displayHistoryData.this, "Export failed", Toast.LENGTH_SHORT).show();
            }
        }


    }









    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }






}
