package com.example.capstone_datacollection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;

public class Login extends AppCompatActivity {

    private TextView pw;
    private TextView info;
    private Button loginBton;

    int counter = 3;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pw = (TextView) findViewById(R.id.pw);
        loginBton = (Button) findViewById(R.id.loginBton);
        info = (TextView) findViewById(R.id.info);
        loginBton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(pw.getText().toString());
            }
        });
    }


    private void validate(String pw){
        if( pw.equals("1234")){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else{

            Toast.makeText(Login.this, "Access code incorrect, please try again", Toast.LENGTH_SHORT).show();

            counter--;

            info.setText("Number of incorrect attempts: "+ String.valueOf(counter));
            if(counter == 0){
                info.setText("Access is disable, try again in 1 minute!");
                int second = 5;
                Timer t = new Timer();
                loginBton.setEnabled(false);


                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        String tag = "";
                        // This method will be executed once the timer is over
                        loginBton.setEnabled(true);
                        counter = 3;
                        Log.d(tag,"Wrong");
                        info.setText("Number of incorrect attempts: "+ String.valueOf(counter));

                    }
                },60000);
            }

        }
    }









}
