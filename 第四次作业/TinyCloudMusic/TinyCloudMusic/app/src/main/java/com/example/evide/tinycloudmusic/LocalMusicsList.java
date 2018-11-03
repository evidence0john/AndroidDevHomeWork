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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class LocalMusicsList extends AppCompatActivity {

    private MusicPlayerService.musicPlayerBinder musicPlayerBinder;
    private Context ctx;
    private ListView lv;
    private ArrayList<HashMap<String, Object>> musicList = new ArrayList<HashMap<String, Object>>();
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicPlayerBinder = (MusicPlayerService.musicPlayerBinder)iBinder;
            //musicPlayerBinder.refreshLocalMusicList();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map = new HashMap<String, Object>();
            map.put("name", "@刷新曲库");
            map.put("path", "@本地歌曲过多可能导致卡滞");
            musicList.add(map);
            for (String item : musicPlayerBinder.listAllMusicsInDB()) {
                map = new HashMap<String, Object>();
                map.put("name", item.substring(0, item.indexOf('\n')));
                map.put("path", item.substring(item.indexOf('\n') + 1));
                musicList.add(map);
            }
            lv.setAdapter(new SimpleAdapter(ctx,
                    musicList,
                    R.layout.simple_music_info,
                    new String[]{"name", "path"},
                    new int[]{R.id.simple_inf_name, R.id.simple_inf_path}
            ));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (i == 0) {
                        Toast.makeText(ctx, "刷新本地曲库中，请稍等", Toast.LENGTH_SHORT).show();
                        musicPlayerBinder.refreshLocalMusicList();
                        Toast.makeText(ctx, "已经刷新本地曲库", Toast.LENGTH_SHORT).show();
                    }
                    musicPlayerBinder.playFile(String.valueOf(musicList.get(i).get("path")));
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_musics_list);
        ctx = this;
        lv = findViewById(R.id.lvLocalMusics);
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
