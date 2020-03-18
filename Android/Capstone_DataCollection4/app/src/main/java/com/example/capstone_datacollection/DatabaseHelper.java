package com.example.capstone_datacollection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CollectedData3.db";
    public static final String TABLE_NAME = "Data_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "STATION_NO";
    public static final String COL_3 = "YEAR";
    public static final String COL_4 = "MONTH";
    public static final String COL_5 = "DAY";
    public static final String COL_6 = "TIME";
    public static final String COL_7 = "VOLTAGE";
    private static DatabaseHelper instance = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        instance = this;
        Log.d("Mytag", "Creating Helper");
        //onUpgrade(getWritableDatabase(), 1, 1);
    }

    //public static boolean initialized() {
        //return instance != null;
    //}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,STATION_NO TEXT NOT NULL,YEAR INTEGER NOT NULL,MONTH INTEGER NOT NULL,DAY INTEGER,TIME TEXT NOT NULL,VOLTAGE REAL NOT NULL,UNIQUE (STATION_NO, YEAR, MONTH, DAY, TIME, VOLTAGE))");
        Log.d("Mytag", "Creating table");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String STATION_NO, int YEAR, int MONTH, int DAY, String TIME, float VOLTAGE) {
        SQLiteDatabase db = this.getWritableDatabase();

        //SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put(COL_1, ID);
        contentValues.put(COL_2, STATION_NO);
        contentValues.put(COL_3, YEAR);
        contentValues.put(COL_4, MONTH);
        contentValues.put(COL_5, DAY);
        contentValues.put(COL_6, TIME);
        contentValues.put(COL_7, VOLTAGE);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }




    public Cursor getStationData(String station) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME +
                " where " + COL_2 + "=\"" + station + "\" order by YEAR, MONTH", null);
        return res;
    }

    public Cursor getByKeyword (String STATION_NO,int year, int month, int day) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query="SELECT * FROM Data_table WHERE STATION_NO = ? AND YEAR >= ? AND MONTH >= ? AND DAY >= ?";
        String[] selectionArgs = {STATION_NO,Integer.toString(year),Integer.toString(month),Integer.toString(day)};


        Cursor res = db.rawQuery(query,selectionArgs);
        return res;
    }
}