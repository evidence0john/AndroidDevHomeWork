/*
* This activity just used for simple testing, and will not be set as main activity actually
* */
package com.example.evide.tinycloudmusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Checking Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new  String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        //Wait for user GRANT...
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
        setContentView(R.layout.activity_main);
        /*
          Start Mongoose service and launching HTTP server
         */
        Intent intentMongoose = new Intent(this, MongooseService.class);
        startService(intentMongoose);

        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText("Service Running...");
        /*DBHelper DBhelper = new DBHelper(this, "tcmData", null, 1 );
        SQLiteDatabase db_r = DBhelper.getReadableDatabase();
        SQLiteDatabase db_w = DBhelper.getWritableDatabase();
        Util.addMusicToDB(db_w, Environment.getExternalStorageDirectory().getAbsolutePath() + "/netease/cloudmusic/Music/");
        Util.debugShowMusicsInDB(db_r);*/
    }
}
