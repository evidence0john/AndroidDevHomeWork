package com.example.evide.simpleclient;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Just implement a simple database helper
 */

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String ddl_teachers = "create table teachers(code varchar(16) primary key,name varchar(128));";
        String ddl_tables = "create table tables(term varchar(16),code varchar(16));";
        sqLiteDatabase.execSQL(ddl_tables);
        sqLiteDatabase.execSQL(ddl_teachers);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
