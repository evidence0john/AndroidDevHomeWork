package com.example.evide.siplemusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MyService extends Service {
    public MyService() {
    }
    public final static int LOAD_INIT = 1;
    private ArrayList<HashMap<String, Object>> musicList;
    private MyBinder serviceBinder = new MyBinder();
    private MediaPlayer player = new MediaPlayer();
    private String curPlay = null;
    private int curPlayId = 0;
    private boolean isPlaying = false;
    private boolean isConnected = false;
    private boolean initLoadFlag = false;
    @Override
    public void onCreate() {
        super.onCreate();
        musicList = new ArrayList<HashMap<String, Object>>();
        Thread refreshT = new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(isConnected && isPlaying){//Refresh
                        isPlaying = player.isPlaying();
                    }
                }
            }
        };
        refreshT.start();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i("evi.service", "Recoonected.........");//???
        isConnected = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("evi.service", "bye........");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        isConnected = true;
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isConnected = false;
        return super.onUnbind(intent);
    }

    public class MyBinder extends Binder{
        void startPlay(String path) throws Exception {
            player.reset();
            player.setDataSource(path);
            player.prepare();
            player.start();
        }
        void pauseTrack(){
            player.pause();
        }
        void continueTrack(){
            player.start();
            //player.getCurrentPosition();
        }
        int trackDuration(){
            return player.getDuration();
        }
        void setPlayingFlag(){
            isPlaying = true;
        }
        void clrPlayingFlag(){
            isPlaying = false;
        }
        boolean getPlayingFlag(){
            return isPlaying;
        }
        void setCurPlaying(String s, int i){
            curPlay = s;
            curPlayId = i;
        }
        String getCurPlaying(){
            return curPlay;
        }
        int getCurPlayingId(){
            return curPlayId;
        }
        int getCurPosition(){
            return player.getCurrentPosition();
        }
        void seekTo(int msec){
            player.seekTo(msec);
        }

        boolean loadMusics(String path, int mode){
            if(initLoadFlag && mode == LOAD_INIT)
                return false;
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
        ArrayList<HashMap<String,Object>> getMusicList()
        {
            return musicList;
        }
    }
}