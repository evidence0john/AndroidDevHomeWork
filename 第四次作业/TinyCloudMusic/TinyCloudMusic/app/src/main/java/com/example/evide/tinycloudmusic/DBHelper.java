package com.example.evide.tinycloudmusic;

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
        String ddl_server_cfg = "create table server_cfg(key varchar(20) primary key,value varchar(128));";
        String ddl_music_list = "create table music_list(name varchar(128) primary key,path varchar(128));";
        sqLiteDatabase.execSQL(ddl_server_cfg);
        sqLiteDatabase.execSQL(ddl_music_list);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
