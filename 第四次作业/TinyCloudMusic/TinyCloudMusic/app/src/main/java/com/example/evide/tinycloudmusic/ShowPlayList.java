package com.example.evide.tinycloudmusic;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShowPlayList extends AppCompatActivity {

    private ListView lv;
    private boolean isBinded = false;
    private int curPlayingId = -1;
    private boolean intentFlag = false;
    private String listName;
    private Bundle extras;
    private Context ctx;
    private ArrayList<String> musicList;
    private ArrayList<String> musicNameList = new ArrayList<String>();
    private MusicPlayerService.musicPlayerBinder musicPlayerBinder;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicPlayerBinder = (MusicPlayerService.musicPlayerBinder)iBinder;
            isBinded = true;
            listName = extras.getString("listName");
            musicList = musicPlayerBinder.listAllMusicsInTable(listName);
            for (String item : musicList) {
                musicNameList.add(item.substring(0, item.indexOf('\n')));
            }
            lv.setAdapter(new MyAdapter(ctx,
                    android.R.layout.simple_expandable_list_item_1,
                    musicNameList
            ));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    musicPlayerBinder.setCurPlayingList(listName, i);
                }
            });
            if (musicPlayerBinder.getCurPlayingListName().compareTo(listName) == 0) {
                curPlayingId = musicPlayerBinder.getCurPlayingId();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private class MyAdapter extends ArrayAdapter {
        public MyAdapter(@NonNull Context context, int resource, @NonNull List objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView != null) {
                if (position == curPlayingId)
                    convertView.setBackgroundColor(Color.rgb(253, 129, 171));
                else
                    convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            return super.getView(position, convertView, parent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_play_list);
        extras = getIntent().getExtras();
        ctx = this;
        lv = findViewById(R.id.lvShowList);
        Intent intent = new Intent(this, MusicPlayerService.class);
        startService(intent); //May not necessary
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        final Handler handler = new Handler();
        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(notifyLv);
                }
            }
        }.start();
    }

    private Runnable notifyLv = new Runnable() {
        @Override
        public void run() {
            curPlayingId = musicPlayerBinder.getCurPlayingId();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        if (intentFlag) {
            Intent intent = new Intent(this, LocalListsMgmt.class);
            startActivity(intent); // Back to local play list management activit
        }
    }

    public void delThisList(View view) {
        musicPlayerBinder.delLocalList(listName);
        intentFlag = true;
        this.finish();
    }

    public void playThisList(View view) {
        musicPlayerBinder.setCurPlayingList( listName,0);
        this.finish();
    }
}
