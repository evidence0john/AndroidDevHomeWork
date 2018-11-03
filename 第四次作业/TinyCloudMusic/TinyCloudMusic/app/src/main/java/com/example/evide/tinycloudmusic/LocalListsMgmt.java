package com.example.evide.tinycloudmusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class LocalListsMgmt extends AppCompatActivity {

    private ArrayList<String> lists = new ArrayList<String>();
    private MusicPlayerService.musicPlayerBinder musicPlayerBinder;
    private ListView lv;

    //private Context ctx;
    private LocalListsMgmt ctx;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicPlayerBinder = (MusicPlayerService.musicPlayerBinder)iBinder;
            lists.add(" + 新建播放列表");
            for (String item : musicPlayerBinder.listLocalLists()) {
                if (item.compareToIgnoreCase("android_metadata") != 0)
                    lists.add(item);
            }
            lv.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_expandable_list_item_1, lists));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_lists_mgmt);
        lv = findViewById(R.id.lvLocalLists);
        ctx = this;
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    //Create New list
                    Intent intent = new Intent(ctx, NewPlayList.class);
                    startActivity(intent);
                    ctx.finish();
                    return;
                }
                String listName = lists.get(i);
                Intent intent = new Intent(ctx, ShowPlayList.class);
                intent.putExtra("listName", listName);
                startActivity(intent);
                ctx.finish();
            }
        });

        Intent intent = new Intent(this, MusicPlayerService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
