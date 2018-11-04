package com.example.evide.simpleclient;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class Util {

    static public void addTeacherToDB(SQLiteDatabase db, String code, String name) {
        String sql = "insert into teachers (code,name) values(?,?)";
        try {
            db.execSQL(sql, new Object[]{code, name});
        } catch (Exception e) {
            //Don't care this exception...
        }
        return;
    }

    static public ArrayList<String> getTeachers(SQLiteDatabase db) {
        ArrayList<String> arrayList = new ArrayList<>();
        String sql = "select code, name from teachers";
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext())
            arrayList.add(c.getString(c.getColumnIndex("code")) +
                    "\n" + c.getString(c.getColumnIndex("name")));
        return arrayList;
    }

    static public void clearTeachers(SQLiteDatabase db) {
        String sql = "delete * from teachers";
        db.execSQL(sql);
    }

    static public void addClassToDB(SQLiteDatabase db, String term, String code) {
        String sql = "insert into tables(term,code) values(?,?)";
        try {
            db.execSQL(sql, new Object[]{term, code});
        } catch (Exception e) {
            //Don't care this exception...
        }
        return;
    }

    static public void delThisTermClassInDB(SQLiteDatabase db, String code)
    {
        String sql = "delete * from tables where code = " + code;
        try {
            db.execSQL(sql);
        } catch (Exception e) {
            //Don't care this exception...
        }
        return;
    }

    static public ArrayList<String> thisTermClassInDB(SQLiteDatabase db) {
        String sql = "select code from tables";
        ArrayList<String> arrayList = new ArrayList<>();
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext())
            arrayList.add(c.getString(c.getColumnIndex("code")));
        return arrayList;
    }

}
