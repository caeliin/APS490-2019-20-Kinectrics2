package com.example.capstone_datacollection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.WHITE;

public class getNewDataActivity extends AppCompatActivity {

    // Message types sent from the RPiBluetoothService Handler
    public static final String TAG = "bb: ";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_TOAST = 4;


    private static int Message_Year = 0;
    private static int  Message_Month = 0;
    private static int  Message_Day = 0;

    // Key names received from the RPiBluetoothService Handler
    public static final String TOAST = "toast";

    // Intent request codes
    private final static int REQUEST_ENABLE_BT = 1;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the Bluetooth service
    private RPiBluetoothService mBluetoothService = null;
    private String stationName = null;


    DatabaseHelper myDb;


    //UI elements
    LinearLayout myLayout = null;
    TextView statusDisplay = null;
    Button station1 = null; //button used to initiate the connection
    Button station2 = null;
    Button dumpButton = null;
    Button queryButton = null;
    Button saveButton = null;
    DatePickerDialog datePicker = null; //selecting a date for the query
    Button showGraph = null;
    Button endButton = null;

    Button get10min = null;

    private TextView displayDate;

    LineChart myChart = null;


    TextView requestDisplay = null;
    String currentMessage = null;
    TextView responseDisplay = null;
    TextView yearText = null;
    TextView monthText = null;
    TextView dayText = null;
    TextView timeText = null;
    TextView voltageText = null;


    ArrayList<dataClass> myData = new ArrayList<dataClass>();

    /**
     * Check if the device supports bluetooth, quit if not
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_new_data);
        myDb = new DatabaseHelper(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        myLayout = findViewById(R.id.my_layout);

        setTitle("Select the Station");

        statusDisplay = new TextView(this);
        statusDisplay.setText("Initializing");
        myLayout.addView(statusDisplay);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }

    /**
     * Check if Bluetooth is enabled, ask the user to turn on if not
     */
    @Override
    public void onStart() {
        super.onStart();

        //ask user to turn Bluetooth on
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (mBluetoothService == null) {
                setup();
            }
        }
    }

    /**
     * Restart Bluetooth service if previously quit
     */
    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mBluetoothService != null) {
            if (mBluetoothService.getState() == mBluetoothService.STATE_NONE) {
                mBluetoothService.start();
            }
        }
    }

    /**
     * Set up the application - initializes the Bluetooth service and draws a decent UI
     */
    private void setup() {
        initializeUI();

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothService = new RPiBluetoothService(this, mHandler);
        updateStatus("Started");
    }

    /**
     * A method for debugging purposes - prints a message to the App UI
     * @param content The message that will be printed to the screen
     */
    private void addToUI(String content) {
        TextView display = new TextView(this);
        display.setText(content);
        myLayout.addView(display);
    }

    /**
     * Update the status message TextView at the top of the IU
     */
    private void updateStatus(String message) {
        statusDisplay.setText(message);
    }

    /**
     * Display the request sent to the server
     */
    private void displayRequest(String MESSAGE_WRITE) {
        if (requestDisplay != null) {
            myLayout.removeView(requestDisplay);
            requestDisplay = null;
        }
        if(requestDisplay == null){
            requestDisplay = new TextView(this);

            if(Message_Year == 0 &&Message_Month==0&&Message_Day==0){
                requestDisplay.setText("Display all data"+"\n"+"\n");
            }else{
                requestDisplay.setText("Data starts from: "+Message_Year+"-"+Message_Month+"-"+Message_Day+"\n"+"\n");
            }
        }

        requestDisplay.setTextSize(TypedValue.COMPLEX_UNIT_SP,20f);

        myLayout.addView(requestDisplay);
    }

    /**
     * Display the response from the server
     */
    private void displayResponse(String response) {
        if (responseDisplay != null) {
            myLayout.removeView(responseDisplay);
            responseDisplay = null;
        }




        myData = split( response);
        if(myData.size()>0){
            responseDisplay = new TextView(this);
            String datashow = "";
            for(int i=0;i<myData.size();i++){

                datashow =datashow +"Year:"+myData.get(i).getYear()+" "+"Month:"+myData.get(i).getMonth()+" "+"Day:"+myData.get(i).getDay()+" "+"Time:"+myData.get(i).getTime()+" "+"Voltage:"+myData.get(i).getVoltage()+"v"+"\n"+"\n";


            }
            responseDisplay.setText(datashow);
            myLayout.addView(responseDisplay);

        }else{
            responseDisplay = new TextView(this);
            responseDisplay.setText("fail to display data.");
            myLayout.addView(responseDisplay);        }

    }

    /**
     * Draws the initial user interface
     */
    private void initializeUI() {
        //remove buttons from previous connection
        if (dumpButton != null) {
            myLayout.removeView(dumpButton);
            dumpButton = null;
        }
        if (queryButton != null) {
            myLayout.removeView(queryButton);
            queryButton = null;
        }

        if(showGraph !=null){
            myLayout.removeView(showGraph);
            showGraph = null;
        }
        if(myChart !=null){
            myLayout.removeView(myChart);
            myChart = null;
        }
        if (endButton != null) {
            myLayout.removeView(endButton);
            endButton = null;
        }
        if (saveButton != null) {
            myLayout.removeView(saveButton);
            saveButton = null;
        }





        if (station1  == null) {
            station1  = new Button(this);
            station1 .setText("Station 1");
            station1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    stationName = "Station1";
                    connect(v);
                }
            });
            station1.setBackgroundResource(R.drawable.bottom_border);

            myLayout.addView(station1);
        }
        if (station2  == null) {
            station2  = new Button(this);
            station2 .setText("Station 2");
            station2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    stationName = "Station2";
                    connect(v);
                }
            });
            station2.setBackgroundResource(R.drawable.bottom_border);
            myLayout.addView(station2);
        }
    }

    /**
     * Draw UI elements once the app has connected to the server
     *
     * Remove initialized UI elements
     * Add elements to send commands to the server
     */
    private void connectedUI() {
        if (station1 != null) {
            myLayout.removeView(station1);
            station1 = null;
        }
        if (station2 != null) {
            myLayout.removeView(station2);
            station2 = null;
        }

        //Dump button
        if (dumpButton == null) {
            dumpButton = new Button(this);
            dumpButton.setText("Display all data");
            dumpButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Message_Year = 0;
                    Message_Month = 0;
                    Message_Day = 0;
                    dumpDatabase();

                }
            });

            dumpButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bottom_border, 0, 0, 0);
            dumpButton.setBackgroundResource(R.drawable.bottom_border);


            myLayout.addView(dumpButton);

        }

        //Query button
        if (queryButton == null) {
            queryButton = new Button(this);
            queryButton.setText("Get data start from X date");
            final Context thisContext = this;
            queryButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void onClick(View v) {
                    datePicker = new DatePickerDialog(thisContext);
                    datePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker view, int year, int month, int day) {

                            Message_Year = year;
                            Message_Month = month+1;
                            Message_Day = day;

                            query(year, month + 1 /* DatePicker month is 0 indexed */, day);
                        }
                    });
                    datePicker.show();
                }
            });

            queryButton.setBackgroundResource(R.drawable.bottom_border);
            myLayout.addView(queryButton);
        }



        //Disconnect button
        if (endButton == null) {
            endButton = new Button(this);
            endButton.setText("Close connection");
            endButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(requestDisplay!=null){
                        myLayout.removeView(requestDisplay);
                        requestDisplay = null;
                    }

                    end();
                }
            });
            endButton.setBackgroundResource(R.drawable.bottom_border);
            myLayout.addView(endButton);
        }



    }

    /**
     * Stop the Bluetooth service when the app loses focus
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth service
        if (mBluetoothService != null) mBluetoothService.stop();
    }

    /**
     * Send a request to dump all data that the server has
     */
    public void dumpDatabase() {
        mBluetoothService.dump();

        if (showGraph != null) {
            myLayout.removeView(showGraph);
            showGraph = null;

        }

        if(showGraph ==null) {
            showGraph = new Button(this);
            showGraph.setText("Real time graph");
        }
        if(myChart != null){
            myLayout.removeView(myChart);
            myChart = null;
        }


        if (saveButton != null) {
            myLayout.removeView(saveButton);
            saveButton = null;

        }
        if (saveButton == null) {
            saveButton = new Button(this);
            saveButton.setText("save");
            saveButton.setEnabled(true);


        }
        showGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myChart != null){
                    myLayout.removeView(myChart);
                    myChart = null;
                }
                if(responseDisplay != null){
                    myLayout.removeView(responseDisplay);
                    responseDisplay = null;

                }
                if(myChart != null) {
                    myLayout.removeView(myChart);
                    myChart = null;
                }
                myChart = new LineChart(getNewDataActivity.this);
                myChart.setDragEnabled(true);
                myChart.setScaleEnabled(false);

                ArrayList<dataClass> msgs = split(currentMessage);


                final ArrayList<Entry> yValues = new ArrayList<>();

                for(int i = 0; i <msgs.size();i++){
                    yValues.add(new Entry(i,(float)(msgs.get(i).getVoltage())+0f));

                }
                LineDataSet set1 = new LineDataSet(yValues, "Voltage");

                //set1.setFillAlpha(110);
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                dataSets.add(set1);
                set1.setDrawFilled(true);
                Drawable drawable = ContextCompat.getDrawable(getNewDataActivity.this, R.drawable.fade_blue);
                set1.setFillDrawable(drawable);
                LineData data = new LineData(dataSets);

                XAxis xAxis = myChart.getXAxis();
                xAxis.setValueFormatter(new MyXAxisValueFormatter(currentMessage));
                xAxis.setLabelRotationAngle(-90);


                myChart.setData(data);
                data.setValueTextColor(Color.rgb(138, 189, 206));
                data.setValueTextSize(13f);
                set1.setColor(Color.rgb(132, 196, 182));
                myChart.animateX(5000);

                myChart.notifyDataSetChanged();
                myChart.invalidate();
                myChart.setVisibleXRangeMaximum(20);
                myChart.getXAxis().setTextColor(WHITE);
                myChart.getLegend().setTextColor(WHITE);

                myLayout.addView(myChart,700,1000);
            }
        });
        showGraph.setBackgroundResource(R.drawable.bottom_border);
        showGraph.setTextColor(WHITE);


        myLayout.addView(showGraph);


        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean insertedFinish = false;

                ArrayList<dataClass> msgs = split(currentMessage);

                boolean inserted = true;

                    for (int i = 0; i < msgs.size(); ++i) {
                        //Snackbar.make(v, "inserting", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                        inserted = myDb.insertData(stationName, msgs.get(i).getYear(), msgs.get(i).getMonth(),
                                msgs.get(i).getDay(), msgs.get(i).getTime(), msgs.get(i).getVoltage());
                    }

                Snackbar.make(v, inserted ? "Saved" : "Save failed: Data is already existed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                    insertedFinish = true;

                if(insertedFinish == true){
                    saveButton.setEnabled(false);
                }


            }
        });
        saveButton.setBackgroundResource(R.drawable.bottom_border);

        myLayout.addView(saveButton);



    }


    public class MyXAxisValueFormatter extends ValueFormatter{
        private String currentMeassge;

        public MyXAxisValueFormatter(String currentMeassge){
            this.currentMeassge=currentMeassge;
        }

        @Override
        public String getFormattedValue(float value) {
           int valueint = (int)value;
            ArrayList<dataClass> msgs = split(currentMessage);
            return String.valueOf(msgs.get(valueint).getYear())+"-"+msgs.get(valueint).getMonth()+"-"+msgs.get(valueint).getDay()+"-"+msgs.get(valueint).getTime();

        }
    }






    /**
     * Send a query to dump all data that the server has, no older than the specified date
     */
    public void query(int year, final int month, int day) {
        if (saveButton != null) {
            myLayout.removeView(saveButton);
            saveButton = null;

        }
        if (saveButton == null) {
            saveButton = new Button(this);
            saveButton.setText("save");
            saveButton.setEnabled(true);
        }

        mBluetoothService.query(year, month, day);

        if (showGraph != null) {
            myLayout.removeView(showGraph);
            showGraph = null;
        }

        if(showGraph ==null) {
            showGraph = new Button(this);
            showGraph.setText("Real time graph");
        }
        if(myChart != null){
            myLayout.removeView(myChart);
            myChart = null;
        }




        showGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myChart != null){
                    myLayout.removeView(myChart);
                    myChart = null;
                }

                if(responseDisplay != null){
                    myLayout.removeView(responseDisplay);
                    responseDisplay = null;

                }

                myChart = new LineChart(getNewDataActivity.this);
                myChart.setDragEnabled(true);
                myChart.setScaleEnabled(false);

                ArrayList<dataClass> msgs = split(currentMessage);


                final ArrayList<Entry> yValues = new ArrayList<>();

                for(int i = 0; i <msgs.size();i++){
                    yValues.add(new Entry(i,(float)(msgs.get(i).getVoltage())+0f));

                }
                LineDataSet set1 = new LineDataSet(yValues, "Voltage");

                //set1.setFillAlpha(110);
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                dataSets.add(set1);
                set1.setDrawFilled(true);
                Drawable drawable = ContextCompat.getDrawable(getNewDataActivity.this, R.drawable.fade_blue);
                set1.setFillDrawable(drawable);
                LineData data = new LineData(dataSets);

                XAxis xAxis = myChart.getXAxis();
                xAxis.setValueFormatter(new MyXAxisValueFormatter(currentMessage));
                xAxis.setLabelRotationAngle(-90);


                myChart.setData(data);
                data.setValueTextColor(Color.rgb(138, 189, 206));
                data.setValueTextSize(13f);
                set1.setColor(Color.rgb(132, 196, 182));
                myChart.animateX(5000);

                myChart.notifyDataSetChanged();
                myChart.invalidate();
                myChart.setVisibleXRangeMaximum(20);
                myChart.getXAxis().setTextColor(WHITE);
                myChart.getLegend().setTextColor(WHITE);

                myLayout.addView(myChart,700,1000);

            }
        });
        showGraph.setBackgroundResource(R.drawable.bottom_border);


        myLayout.addView(showGraph);



        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean insertedFinish = false;

                ArrayList<dataClass> msgs = split(currentMessage);

                boolean inserted = true;

                for (int i = 0; i < msgs.size(); ++i) {
                    //Snackbar.make(v, "inserting", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                    inserted = myDb.insertData(stationName, msgs.get(i).getYear(), msgs.get(i).getMonth(),
                            msgs.get(i).getDay(), msgs.get(i).getTime(), msgs.get(i).getVoltage());
                }

                Snackbar.make(v, inserted ? "Saved" : "Save failed: Data is already existed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                insertedFinish = true;

                if(insertedFinish == true){
                    saveButton.setEnabled(false);
                }


            }
        });
        saveButton.setBackgroundResource(R.drawable.bottom_border);
        myLayout.addView(saveButton);


    }



    /**
     * Send a request to the server to gracefully close the connection
     */
    public void end() {
        mBluetoothService.end();
    }






    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch(msg.arg1) {
                        case RPiBluetoothService.STATE_NONE:
                            updateStatus("Disconnected");
                            initializeUI();
                            break;
                        case RPiBluetoothService.STATE_CONNECTING:
                            updateStatus("Connecting");
                            break;
                        case RPiBluetoothService.STATE_CONNECTED:
                            updateStatus("Connected: "+stationName);
                            connectedUI();
                            setTitle("Connected to "+stationName);
                            break;
                        default:
                            updateStatus(Integer.toString(msg.arg1));
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    displayRequest(writeMessage);
                    break;
                case MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    // construct a string from the valid bytes in the buffer

                    displayResponse(readMessage);
                    currentMessage = readMessage;
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * Handles when the user responds to the request to enable Bluetooth
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setup();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, "Error: BT not enabled", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    /**
     * Connect to the Raspberry Pi.
     * Hardcoded in.
     * @param v
     */
    public void connect(View v) {

        BluetoothDevice serverDevice = null;

        /* connect to the RPi, which is already paired */
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceMacAddress = device.getAddress();
//                String displayText = "Paired device: " + deviceName + " - " + deviceMacAddress;
                if (deviceName.equals(stationName)) {
                    serverDevice = device;
//                    displayText += " (!)";
                }

//                addToUI(displayText);
            }
        }

        if (serverDevice != null) {
            mBluetoothService.connect(serverDevice);
        }
    }

    public ArrayList<dataClass> split(String readMessage) {



        ArrayList<dataClass> tmp = new ArrayList<dataClass>();
        String dataString;
        readMessage = readMessage.replaceAll("\n","");

        //try {
        //JSONObject req = new JSONObject(readMessage);

        //dataString = req.get("DATA").toString();
        Log.d("Parsing1","ReadMessgae: "+readMessage);
        //Log.d(TAG,"data: "+ dataString);

        dataString = readMessage.replaceAll("\\[","");
        //Log.d(TAG,"dataString1: "+dataString);
        dataString = dataString.replaceAll("\\]","");
        dataString = dataString.replaceAll(" ","");
        dataString = dataString.replaceAll("\\(","");
        dataString = dataString.replaceAll("\\)","");
        String []datalistwithout = dataString.split(":");

        Log.d("Parsing2","datalistwithout[1]: "+datalistwithout[2]);



        String[] dataList = datalistwithout[2].split(",");
        for(int i = 6;i<dataList.length-1;i=i+7){
            int year = Integer.parseInt(dataList[i-6]);
            int month = Integer.parseInt(dataList[i-5]);
            int day= Integer.parseInt(dataList[i-4]);
            String time = dataList[i-3]+":"+dataList[i-2]+":"+dataList[i-1];
            Float voltage = Float.parseFloat(dataList[i-0]);
            dataClass dataclass = new dataClass(year, month,day,time,voltage);
            tmp.add(dataclass);

        }

        return tmp;
    }


}










