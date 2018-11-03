/*MusicPlayerService
* */
package com.example.evide.tinycloudmusic;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerService extends Service {

    final static int MODE_V_MIN = 0;
    final static int MODE_CYCLE = 0;
    final static int MODE_ONCE  = 1;
    final static int MODE_SHUFF = 2;
    final static int MODE_REP   = 3;
    final static int MODE_TRAVE = 4;
    final static int MODE_V_MAX = 4;

    private Context ctx = this;

    private musicPlayerBinder binder = new musicPlayerBinder();
    private DBHelper DBhelper = new DBHelper(this, "tcmData", null, 1 );
    private ListDBHelper listDBHelper = new ListDBHelper(this, "lists", null, 1);
    private SQLiteDatabase db_r;
    private SQLiteDatabase db_w;
    private SQLiteDatabase lists_r;
    private SQLiteDatabase lists_w;
    private int playMode;
    private int curPlayingId = 0;
    private int minPlayingId = 0;
    private int maxPlayingId = 0;
    private String curPlaying = null;
    private boolean isPlaying = false;
    private MediaPlayer player = new MediaPlayer();
    private ArrayList<String> curPlayingList = new ArrayList<String>();
    private String curPlayingListName = "没有播放列表";

    private String folderName = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/tplayer/";
    private String musicDownloadFolder = folderName + "download/";

    private boolean cloudPlayStatus = false;

    private boolean isMongooseBinded = false;
    private MongooseService.MongooseBinder mongooseBinder;

    final static int CMD_PLAY_ID = 1;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mongooseBinder = (MongooseService.MongooseBinder)iBinder;
            isMongooseBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public MusicPlayerService() {
        playMode = MODE_CYCLE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db_r = DBhelper.getReadableDatabase();
        db_w = DBhelper.getWritableDatabase();
        lists_r = listDBHelper.getReadableDatabase();
        lists_w = listDBHelper.getWritableDatabase();

        //Create folders
        File tDir = new File(musicDownloadFolder);
        tDir.mkdirs();

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (curPlayingList.isEmpty()) //
                    return;
                switch (playMode) {
                    case MODE_CYCLE:
                        curPlayingId++;
                        if (curPlayingId > maxPlayingId)
                            curPlayingId = minPlayingId;
                        servicePlayId(curPlayingId);
                        break;
                    case MODE_ONCE:
                        //Finished, Do nothing
                        break;
                    case MODE_REP:
                        servicePlayId(curPlayingId);
                        break;
                    case MODE_TRAVE:
                        curPlayingId++;
                        if (curPlayingId > maxPlayingId)
                            curPlayingId--;
                        break;
                    case MODE_SHUFF: //Copy from ... Edit this code...
                        curPlayingId = Util.randNum(minPlayingId, maxPlayingId);
                        servicePlayId(curPlayingId);
                        break;
                }
            }
        });

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
                    if (cloudPlayStatus && isMongooseBinded) {
                        if(mongooseBinder.getCmdFlag() != 0) {
                            switch (mongooseBinder.getCmdFlag()) {
                                case CMD_PLAY_ID:
                                    int id = Integer.parseInt(mongooseBinder.getCmdArg());
                                    curPlayingId = id;
                                    Log.e("ARG", String.valueOf(id));
                                    servicePlayId(id);
                                    mongooseBinder.clrCmdFlag();
                                    break;
                                default:
                                    mongooseBinder.clrCmdFlag();
                            }
                        }
                    }
                }
            }
        }.start();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return binder;
    }

    private void servicePlayFile(String path) throws Exception {
        player.reset();
        player.setDataSource(path);
        player.prepare();
        player.start();
        curPlaying = path.substring(path.lastIndexOf('/') + 1);
        isPlaying = true;
    }

    private void _RefreshLocalMusicList() {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        Util.deleteTable(db_w, "music_list");
        Util.createTable(db_w, "music_list");
        while (cursor.moveToNext()) {
            Util.addMusicToTable(db_w, "music_list", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)), cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
        }
    }

    private void servicePlayId(int id) {
        String item = curPlayingList.get(id);
        try {
            servicePlayFile(item.substring(item.indexOf('\n') + 1));
        } catch (Exception e) {
            //Do sth...
            Log.e("Exception", String.valueOf(e));
        }
        curPlayingId = id;
    }

    private void servicePlayNext() {
        if (curPlayingList.isEmpty())
            return;
        if (playMode == MODE_SHUFF) {
            curPlayingId = Util.randNum(minPlayingId, maxPlayingId);
            servicePlayId(curPlayingId);
        } else {
            curPlayingId++;
            if (curPlayingId > maxPlayingId)
                curPlayingId = minPlayingId;
            servicePlayId(curPlayingId);
        }
        return;
    }

    private void servicePlayPrev() {
        if (curPlayingList.isEmpty())
            return;
        if (playMode == MODE_SHUFF) {
            curPlayingId = Util.randNum(minPlayingId, maxPlayingId);
            servicePlayId(curPlayingId);
        } else {
            curPlayingId--;
            if (curPlayingId < minPlayingId)
                curPlayingId = maxPlayingId;
            servicePlayId(curPlayingId);
        }
        return;
    }

    public class musicPlayerBinder extends Binder {

        public ArrayList<String> listAllMusicsInDB() {
            return Util.listAllMusicsInDB(db_r);
        }

        public ArrayList<String> listLocalLists() {
            return Util.listTablesInDB(lists_r);
        }

        public ArrayList<String> listAllMusicsInTable(String tableName) {
            return  Util.listAllMusicsInTable(lists_r, tableName);
        }

        public boolean newLocalList(String name) {
            return Util.createTable(lists_w, name);
        }

        public void delLocalList(String name) {
            Util.deleteTable(lists_w, name);
        }

        public void addMusicToTable(String tableName, String name, String path) {
            Util.addMusicToTable(lists_w, tableName, name, path);
        }

        public void refreshLocalMusicList() {
            _RefreshLocalMusicList();
        }

        public void removeFromTable(String tableName, String name, String path) {
            Util.removeFromTable(lists_w, tableName, name, path);
        }

        public void loadFullLocalList() {
            curPlayingList = Util.listAllMusicsInDB(db_r);
        }

        public int getCurPlayingId() {
            return curPlayingId;
        }

        public String getCurPlaying() {
            return curPlaying;
        }

        public int getDuration() {
            return player.getDuration();
        }

        public int getCurrentPosition() {
            return player.getCurrentPosition();
        }

        public void seekTo(int msec) {
            player.seekTo(msec);
        }

        public boolean getIsPlayingFlag() {
            return isPlaying;
        }

        public void playFile(String path) {
            try {
                servicePlayFile(path);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ErrorPlaying", "[" + path + "]");
            }
            isPlaying = true;
        }

        public void pausePlaying() {
            player.pause();
            isPlaying = false;
        }

        public void startPlaying() {
            player.start();
            isPlaying = true;
        }

        public void playNext() {
            servicePlayNext();
        }

        public void playPrev() {
            servicePlayPrev();
        }

        public void setCurPlayingList(String listName, int postiton) {
            curPlayingId = postiton;
            curPlayingListName = listName;
            curPlayingList = listAllMusicsInTable(listName);
            if (curPlayingList.isEmpty())
                return;
            maxPlayingId = curPlayingList.size() - 1;
            String item = curPlayingList.get(curPlayingId);
            playFile(item.substring(item.indexOf('\n') + 1));
        }

        public void clearCurPlayingList() {
            curPlayingListName = "没有播放列表";
            curPlayingId = 0;
            curPlayingList.clear();
            maxPlayingId = 0;
        }

        public ArrayList<String> getCurPlayingList() {
            return curPlayingList;
        }

        public String getCurPlayingListName() {
            return curPlayingListName;
        }

        public int getCurPlayingListSize() {
            return curPlayingList.size();
        }

        public void setPlayMode(int mode) {
            playMode = mode;
        }

        public int getPlayMode() {
            return playMode;
        }

        public void switchPlayMode() {
            playMode++;
            if (playMode > MODE_V_MAX)
                playMode = MODE_V_MIN;
        }

        public boolean isCloudPlay() {
            return cloudPlayStatus;
        }

        public void cloudPlayServiceSwitch() {
            if (cloudPlayStatus) {
                Intent intent = new Intent(ctx, MongooseService.class);
                stopService(intent);
                cloudPlayStatus = false;
            } else {
                //------ Write Play List -----//
                File f = new File(folderName + "cur_play.list");
                try {
                    f.createNewFile();
                    FileWriter writer = new FileWriter(f);
                    for (String sname : curPlayingList) {
                        writer.write(sname.substring(0, sname.indexOf('\n') + 1));
                    }
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cloudPlayStatus = true;
                Intent intent = new Intent(ctx, MongooseService.class);
                startService(intent);
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);
            }
        }

    }
}
