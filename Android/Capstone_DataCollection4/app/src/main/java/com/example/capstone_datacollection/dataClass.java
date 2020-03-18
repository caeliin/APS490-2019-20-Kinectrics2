package com.example.capstone_datacollection;

public class dataClass {
    private int year, month, day;
    private String time;
    private float voltage;


    public dataClass(int year, int month, int day, String time, float voltage){
        this.year = year;
        this.month = month;
        this.day = day;
        this.time = time;
        this.voltage = voltage;

    }

    public int getYear(){
        return this.year;
    }

    public int getMonth(){
        return this.month;
    }
    public int getDay(){
        return this.day;
    }
    public float getVoltage(){
        return this.voltage;
    }

    public String getTime(){
        return this.time;
    }




}
