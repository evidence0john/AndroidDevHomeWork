package com.example.evide.tinycloudmusic;

import android.Manifest;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.view.menu.MenuView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.security.Permission;

public class MusicPlayerView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView tvCurPlay;
    private TextView tvCurList;
    private TextView tvCurTime;
    private TextView tvEndTime;
    private ImageButton ibPlay;
    private ImageButton ibPrev;
    private ImageButton ibNext;
    private ImageButton ibMode;
    private SeekBar sbProgress;

    private MenuItem itSetting; // On/Off Cloud Play

    private boolean isUserMoving = false;
    private MusicPlayerService.musicPlayerBinder musicPlayerBinder;
    private boolean isServiceConnected  = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicPlayerBinder = (MusicPlayerService.musicPlayerBinder)iBinder;
            isServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player_view);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new  String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new  String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);

        Intent musicPlayerIntent = new Intent(this, MusicPlayerService.class);
        startService(musicPlayerIntent);
        bindService(musicPlayerIntent, serviceConnection, BIND_AUTO_CREATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvCurPlay = findViewById(R.id.tvCurPlay);
        tvCurList = findViewById(R.id.tvCurPlayList);
        tvCurTime = findViewById(R.id.tvCurTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        ibPlay = findViewById(R.id.bPlay);
        ibPrev = findViewById(R.id.bPrev);
        ibNext = findViewById(R.id.bNext);
        ibMode = findViewById(R.id.bPlayMode);
        sbProgress = findViewById(R.id.sbProgress);

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Don't use this method
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserMoving = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicPlayerBinder.getCurPlaying() != null)
                    musicPlayerBinder.seekTo(sbProgress.getProgress());
                isUserMoving = false;
            }
        });

        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isServiceConnected)
                        handler.post(refreshUI);
                }
            }
        }.start();
    }

    private Runnable refreshUI = new Runnable() {
        @Override
        public void run() {
            if (musicPlayerBinder.getCurPlaying() != null) {
                tvCurPlay.setText(musicPlayerBinder.getCurPlaying());
                tvCurTime.setText(Util.secToTime(musicPlayerBinder.getCurrentPosition() / 1000));
                tvEndTime.setText(Util.secToTime(musicPlayerBinder.getDuration() / 1000));
                sbProgress.setMax(musicPlayerBinder.getDuration());
                if (!isUserMoving) // stop updating seek bar while user touching the seek bar
                    sbProgress.setProgress(musicPlayerBinder.getCurrentPosition());
            } else {
                tvCurPlay.setText("未播放任何曲目");
                sbProgress.setMax(0);
                sbProgress.setProgress(0);
            }
            if (musicPlayerBinder.getIsPlayingFlag()) {
                ibPlay.setBackgroundResource(R.mipmap.stop);
            } else {
                ibPlay.setBackgroundResource(R.mipmap.play);
            }
            tvCurList.setText(musicPlayerBinder.getCurPlayingListName());
            //Play Mode
            switch (musicPlayerBinder.getPlayMode()) {
                case MusicPlayerService.MODE_CYCLE:
                    ibMode.setBackgroundResource(R.mipmap.mode_cycle);
                break;
                case MusicPlayerService.MODE_ONCE:
                    ibMode.setBackgroundResource(R.mipmap.mode_once);
                    break;
                case MusicPlayerService.MODE_REP:
                    ibMode.setBackgroundResource(R.mipmap.mode_rep);
                    break;
                case MusicPlayerService.MODE_TRAVE:
                    ibMode.setBackgroundResource(R.mipmap.mode_trave);
                    break;
                case MusicPlayerService.MODE_SHUFF:
                    ibMode.setBackgroundResource(R.mipmap.mode_shuffle);
                    break;
            }
            //Cloud Play
            if (musicPlayerBinder.isCloudPlay()) {
                itSetting.setTitle("关闭云服务");
            } else {
                itSetting.setTitle("启动云服务");
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.music_player_view, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        itSetting = menu.getItem(0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            musicPlayerBinder.cloudPlayServiceSwitch();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_local) {
            Intent intent = new Intent(this, LocalMusicsList.class);
            startActivity(intent);
        } else if (id == R.id.nav_lists) {
            Intent intent = new Intent(this, LocalListsMgmt.class);
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this, CloudPlayDemo.class);
            startActivity(intent);
            
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void playButtonOnClick(View view) {
        if (musicPlayerBinder.getIsPlayingFlag()) {
            musicPlayerBinder.pausePlaying();
            ibPlay.setBackgroundResource(R.mipmap.play);
        } else {
            musicPlayerBinder.startPlaying();
            ibPlay.setBackgroundResource(R.mipmap.stop);
        }
    }

    public void prevButtonOnClick(View view) {
        musicPlayerBinder.playPrev();
    }

    public void nextButtonOnClick(View view) {
        musicPlayerBinder.playNext();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    public void switchPlayMode(View view) {
        musicPlayerBinder.switchPlayMode();
    }
}
