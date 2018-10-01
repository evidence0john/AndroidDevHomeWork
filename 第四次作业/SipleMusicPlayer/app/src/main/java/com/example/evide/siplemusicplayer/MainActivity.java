package com.example.evide.siplemusicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private ListView lvMusics;
    private TextView tvCurPlay;
    private TextView tvCurTime;
    private TextView tvEndTime;
    private SeekBar sbProgress;
    private ImageButton bPlay;
    private Intent intent;
    private int curPlayingId = 0;
    private MyService.MyBinder serviceBinder;
    private String workDir = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/netease/cloudmusic/Music/"; //Current work directory
    private boolean isUserMovingTrack = false;
    private ArrayList<HashMap<String, Object>> musicList = new ArrayList<HashMap<String, Object>>();
    private myAdapter adapter;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBinder = (MyService.MyBinder) iBinder;
            //serviceBinder.loadMusics(Environment.getExternalStorageDirectory().getAbsolutePath() +
            //       "/netease/cloudmusic/Music/", MyService.LOAD_INIT);
            //musicList = serviceBinder.getMusicList();
            if(serviceBinder.getPlayingFlag()) {
                curPlayingId = serviceBinder.getCurPlayingId();
                tvCurPlay.setText(serviceBinder.getCurPlaying());
                sbProgress.setMax(serviceBinder.trackDuration());
                tvEndTime.setText(secToTime(serviceBinder.trackDuration() / 1000));
                bPlay.setBackgroundResource(R.mipmap.stop);
                /*Will auto refreshed by Handler
                tvCurTime.setText(secToTime(serviceBinder.getCurPosition() / 1000));
                sbProgress.setProgress(serviceBinder.getCurPosition());*/
            } else
                tvCurPlay.setText("未播放任何曲目");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private class myAdapter extends SimpleAdapter {
        public myAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new  String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        //Wait for user GRANT...
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
        intent = new Intent(this, MyService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
        setContentView(R.layout.activity_main);
        /*
        Initialize Layout Obj
        */
        tvCurPlay = findViewById(R.id.tvCurPlay);
        tvCurTime = findViewById(R.id.tvCurTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        sbProgress = findViewById(R.id.sbProgress);
        bPlay = findViewById(R.id.bPlay);
        lvMusics = findViewById(R.id.lvMusics);
        //Start Service
        //When setAdapter workDir will be initialized...
        loadMusics(workDir); //Do this will set musicList
        adapter = new myAdapter(this,
                musicList,
                R.layout.music_listview,
                new String[]{"filename"},
                new int[]{R.id.filename}
        );
        lvMusics.setAdapter(adapter);
        //Set item listener, when user clicked at a music name item of list view, do this...
        lvMusics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //SimpleAdapter adapter = (SimpleAdapter) lvMusics.getAdapter();
                HashMap<String, Object> m = (HashMap<String, Object>) adapter.getItem(i);
                String musicName = (String) m.get("filename");
                try {
                    serviceBinder.startPlay(workDir + musicName);
                } catch (Exception e) {
                    Log.e("Error", "Exception = " + e);
                }
                serviceBinder.setPlayingFlag();
                serviceBinder.setCurPlaying(musicName, i);
                tvCurPlay.setText("正在播放：" + musicName);
                tvEndTime.setText(secToTime(serviceBinder.trackDuration() / 1000));
                sbProgress.setMax(serviceBinder.trackDuration());
                sbProgress.setProgress(0);
                curPlayingId = serviceBinder.getCurPlayingId();
                bPlay.setBackgroundResource(R.mipmap.stop);
                adapter.notifyDataSetChanged();
            }
        });
        sbProgress.setOnSeekBarChangeListener(this);
        //UI update
        final Handler handler = new Handler();
        new Thread(){
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(refreshUI);
                }
            }
        }.start();
    }

    private Runnable refreshUI = new Runnable() {
        @Override
        public void run() {
            if(serviceBinder.getPlayingFlag()) {
                tvCurTime.setText(secToTime(serviceBinder.getCurPosition() / 1000));
                if (!isUserMovingTrack)
                    sbProgress.setProgress(serviceBinder.getCurPosition());
            } else {
                bPlay.setBackgroundResource(R.mipmap.play);
            }
        }
    };
    //This method to traverse a folder
    private List<String> getFiles(String path){
        workDir = Environment.getExternalStorageDirectory().getAbsolutePath() + path;
        List<String> files = new ArrayList<String>();
        File file = new File(workDir);
        File[] fs = file.listFiles();
        for(File f:fs)
            if (f.isFile())
                files.add(String.valueOf(f).substring(workDir.length()));
        return files;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    /*
    Switch music
    */
    public final static int SWITCH_NEXT = 1;
    public final static int SWITCH_PREV = 2;
    public void switchMusic(int dir)
    {
        int i = serviceBinder.getCurPlayingId();
        switch (dir) {
            case SWITCH_NEXT:
                i =  i + 1;
                if(i == adapter.getCount())
                    i = 0;
                break;
            case SWITCH_PREV:
                i = i - 1;
                if (i < 0)
                    i = adapter.getCount() - 1;
                break;
            default:
                return;
        }
        //myAdapter adapter = (myAdapter) lvMusics.getAdapter();
        HashMap<String, Object> m = (HashMap<String, Object>) adapter.getItem(i);
        String musicName = (String) m.get("filename");
        try {
            serviceBinder.startPlay(workDir + musicName);
        } catch (Exception e) {
            Log.e("Error", "Exception = " + e);
        }
        serviceBinder.setPlayingFlag();
        serviceBinder.setCurPlaying(musicName, i);
        tvCurPlay.setText("正在播放：" + musicName);
        tvEndTime.setText(secToTime(serviceBinder.trackDuration() / 1000));
        sbProgress.setMax(serviceBinder.trackDuration());
        sbProgress.setProgress(0);
        curPlayingId = serviceBinder.getCurPlayingId();
        bPlay.setBackgroundResource(R.mipmap.stop);
        adapter.notifyDataSetChanged();
    }
    public void playNext(View view) {
        switchMusic(SWITCH_NEXT);
    }

    public void playPrev(View view) {
        switchMusic(SWITCH_PREV);
    }

    public void playButtonClick(View view) {
        if (serviceBinder.getPlayingFlag()) {
            serviceBinder.pauseTrack();
            bPlay.setBackgroundResource(R.mipmap.play);
            serviceBinder.clrPlayingFlag();

        } else {
            serviceBinder.continueTrack();
            bPlay.setBackgroundResource(R.mipmap.stop);
            serviceBinder.setPlayingFlag();
        }
    }
    private boolean loadMusics(String path){
        File file = new File(path);
        File[] fs = file.listFiles();
        for(File f:fs)
            if (f.isFile()) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("filename", String.valueOf(f).substring(path.length()));
                musicList.add(map);
            }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    public static String secToTime(int time) {
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

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isUserMovingTrack = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(serviceBinder.getPlayingFlag()){
            serviceBinder.seekTo(seekBar.getProgress());
        }
        isUserMovingTrack = false;
    }
}
