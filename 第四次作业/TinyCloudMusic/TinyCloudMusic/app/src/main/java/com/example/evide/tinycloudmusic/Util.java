/**
 * Utilities...
 */

package com.example.evide.tinycloudmusic;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class Util {

    /**
     * This method... emmmm...
    */
    static public void addMusicToDB(SQLiteDatabase db, String path) {
        String sql = "insert into music_list(name,path) values(?,?)";
        File file = new File(path);
        File[] fs = file.listFiles();
        for(File f:fs) {
            if (f.isFile()) {
                try {
                    db.execSQL(sql, new Object[]{f.getAbsolutePath().substring(path.length()), path});
                } catch (Exception e) {
                    //Don't care this exception...
                }
            }
        }
    }

    static public void addMusicToTable(SQLiteDatabase db, String tableName, String name, String path) {
        String sql = "insert into " + tableName + " (name,path) values(?,?)";
        try {
            db.execSQL(sql, new Object[]{name, path});
        } catch (Exception e) {
            //Don't care this exception...
        }
    }

    static public void removeFromTable(SQLiteDatabase db, String tableName, String name, String path) {
        String sql = "delete from " + tableName + " where name = ? and path = ?";
        try {
            db.execSQL(sql, new Object[]{name, path});
        } catch (Exception e) {
            //Don't care this exception...
        }
    }

    static public ArrayList<String> listAllMusicsInTable(SQLiteDatabase db, String table) {
        ArrayList<String> list = new ArrayList<String>();
        String sql="select name, path from " + table;
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext())
            list.add(c.getString(c.getColumnIndex("name")) +
                    "\n" + c.getString(c.getColumnIndex("path")));
        return list;
    }

    static public ArrayList<String> listAllMusicsInDB(SQLiteDatabase db) {
        ArrayList<String> list = new ArrayList<String>();
        String sql="select name, path from music_list";
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext())
            list.add(c.getString(c.getColumnIndex("name")) +
                    "\n" + c.getString(c.getColumnIndex("path")));
        return list;
    }

    static public ArrayList<String> listTablesInDB(SQLiteDatabase db) {
        ArrayList<String> list = new ArrayList<String>();
        String sql="select name from sqlite_master where type='table' order by name;";
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext())
            list.add(c.getString(c.getColumnIndex("name")));
        return list;
    }

    static public boolean createTable(SQLiteDatabase db, String name) {
        ArrayList<String> list = new ArrayList<String>();
        String sql="select name from sqlite_master where type='table' order by name;";
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {
            if(name.compareToIgnoreCase(c.getString(c.getColumnIndex("name"))) == 0)
                return false;
        }
        sql = "create table " + name + "(name varchar(128) primary key,path varchar(128));";
        db.execSQL(sql);
        return true;
    }

    static public void deleteTable(SQLiteDatabase db, String name) {
        String sql = "drop table " + name;
        db.execSQL(sql);
    }

    static public void debugShowMusicsInDB(SQLiteDatabase db) {
        String sql="select * from music_list";
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {
            Log.i("Debug DB Show \"name\":", c.getString(c.getColumnIndex("name")));
            Log.i("Debug DB Show \"path\":", c.getString(c.getColumnIndex("path")));
        }
    }

    public static int randNum(int min, int max) {
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }

    static public String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    static public String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
}
