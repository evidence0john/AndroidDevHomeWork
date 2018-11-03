package com.example.evide.tinycloudmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CloudPlayDemo extends AppCompatActivity {

    private String host;
    private ListView lvDemo;
    private Context ctx;
    private ArrayList<String> arrayList;
    private boolean isRequestReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_play_demo);
        ctx = this;
        SharedPreferences sp = getSharedPreferences("CloudSettings", MODE_PRIVATE);
        host = sp.getString("host", "default");
        if (host.compareTo("default") == 0) {
            Toast.makeText(this, "请先配置服务器主机信息",Toast.LENGTH_SHORT).show();
            finish();
        }

        host += "/";

        lvDemo = findViewById(R.id.lvCloudPlayDemo);

        arrayList = getCurPlayList();

        //lvDemo.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,arrayList));

        lvDemo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                  playId(arrayList.size() - 1);
                } else
                playId(i - 1);
            }
        });

        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (!isRequestReady);
                handler.post(demoUI);
            }
        }.start();

    }

    private Runnable demoUI = new Runnable() {
        @Override
        public void run() {
            lvDemo.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_expandable_list_item_1, arrayList));
            //lvDemo.notify();
        }
    };

    private void playId(int id) {
        final int i  = id;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(host + ".playid?" + String.valueOf(i));
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    //connection.setConnectTimeout();
                    //connection.setReadTimeout();
                    int status_code = connection.getResponseCode();
                    if (status_code == 200) {
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                } catch (Exception e){
                    //Do sth...
                    Log.e("InternetERR", String.valueOf(e));
                    //Toast.makeText(ctx, String.valueOf(e), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    private ArrayList<String> getCurPlayList() {
        final ArrayList<String> arrayList = new ArrayList<String>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(host + "tplayer/cur_play.list");
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    //connection.setConnectTimeout();
                    //connection.setReadTimeout();
                    int status_code = connection.getResponseCode();
                    if (status_code == 200) {
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            arrayList.add(line);
                        }
                    }
                    isRequestReady = true;
                } catch (Exception e){
                    //Do sth...
                    //Toast.makeText(ctx, String.valueOf(e), Toast.LENGTH_SHORT).show();
                    Log.e("InternetERR", "ERROR INFO" + String.valueOf(e));
                }
            }
        }).start();
        return arrayList;
    }

    /*private Runnable demoUI = new Runnable() {
        @Override
        public void run() {
            //
        }
    };*/

}
