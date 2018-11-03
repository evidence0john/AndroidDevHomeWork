package com.example.evide.tinycloudmusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPlayList extends AppCompatActivity {

    private ListView lv;
    private EditText et;
    private Context ctx;
    private myAdapter adapter;
    private MusicPlayerService.musicPlayerBinder musicPlayerBinder;
    private ArrayList<HashMap<String, Object>> musicList = new ArrayList<HashMap<String, Object>>();
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicPlayerBinder = (MusicPlayerService.musicPlayerBinder)iBinder;
            for (String item : musicPlayerBinder.listAllMusicsInDB()) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("name", item.substring(0, item.indexOf('\n')));
                map.put("path", item.substring(item.indexOf('\n') + 1));
                map.put("flag", false);
                musicList.add(map);
            }
            adapter = new myAdapter(ctx,
                    musicList,
                    R.layout.simple_music_info,
                    new String[]{"name", "path"},
                    new int[]{R.id.simple_inf_name, R.id.simple_inf_path}
            );
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    HashMap<String, Object> map = musicList.get(i);
                    if (!(boolean)map.get("flag"))
                        map.put("flag", true);
                    else
                        map.put("flag", false);
                    adapter.notifyDataSetChanged();
                    //Log.e("WH?",String.valueOf(musicList.get(i)));
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
        setContentView(R.layout.activity_new_play_list);
        ctx = this;
        et = findViewById(R.id.etNewPlaylist);
        lv = findViewById(R.id.lvNewPlayList);
        Intent intent = new Intent(this, MusicPlayerService.class);
        startService(intent); //May not necessary
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        Intent intent = new Intent(this, LocalListsMgmt.class);
        startActivity(intent); // Back to local play list management activity
    }

    public void newPlayListSubmit(View view) {
        String newListName = String.valueOf(et.getText());
        ArrayList<HashMap<String, Object>> transList = new ArrayList<HashMap<String, Object>>();
        if (newListName.length() == 0) {
            Toast.makeText(this, "请输入列表名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (musicPlayerBinder.listLocalLists().contains(newListName)) {
            Toast.makeText(this, "列表 \"" + newListName + "\" 已存在，请为列表设置唯一的列表名", Toast.LENGTH_SHORT).show();
        } else {
            musicPlayerBinder.newLocalList(newListName);
            for (HashMap<String, Object> map : musicList) {
                if ((boolean)map.get("flag"))
                    musicPlayerBinder.addMusicToTable(newListName, String.valueOf(map.get("name")), String.valueOf(map.get("path")));
            }
            this.finish(); // Finished this activity if create new play list successful.
        }
    }

    private class myAdapter extends SimpleAdapter {

        public myAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HashMap<String, Object> map = musicList.get(position);
            if (convertView != null) {
                if (!(boolean)map.get("flag"))
                    convertView.setBackgroundColor(Color.TRANSPARENT);
                else
                    convertView.setBackgroundColor(Color.rgb(253, 129, 171));
            }
            return super.getView(position, convertView, parent);
        }
    }
}
