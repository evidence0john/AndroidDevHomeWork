package com.example.evide.simpleclient;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

public class InitActivity extends AppCompatActivity {

    private String referer = "http://xxx.xxx.xxx/jwweb/ZNPK/TeacherKBFB.aspx";
    private WebView wvInit;
    private String info;
    //private boolean isInitFinished = false;
    private boolean isUpdateWebView = false;
    private boolean isGotBaseInfo = false;
    private Context ctx = this;
    //private SQLiteDatabase db_r;
    private SQLiteDatabase db_w;
    private DBHelper dbHelper = new DBHelper(this, "teachers", null, 1);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        wvInit = findViewById(R.id.wvInit);

        db_w = dbHelper.getWritableDatabase();

        //

        final Handler handler = new Handler();

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    //info = getBaseInfoTable()
                    //info = getTeachers();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isUpdateWebView) {
                        handler.post(upDateWebView);
                    }
                }
            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private Runnable upDateWebView = new Runnable() {
        @Override
        public void run() {
            info = info.replace("</option><option value=", "\n");
            info = info.substring(info.indexOf("<option>") + 9);
            info = info.substring(0, info.indexOf("</option>"));
            String[] infoTable = info.split("\n");
            //info = Integer.toString(info.hashCode());
            //wvInit.loadData(infoTable[0], "text/plain;", "UTF-8");
            wvInit.loadData(info, "text/plain;", "UTF-8");
            //Util.addTeacherToDB(db_w);

            for (String item : infoTable) {
                try {
                    //Log.e(item.substring(0, item.indexOf('>')), item.substring(item.indexOf('>') + 1));
                    Util.addTeacherToDB(db_w, item.substring(0, item.indexOf('>')), item.substring(item.indexOf('>') + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            isUpdateWebView = false;
        }
    };

    private String getTeachers() throws Exception {
        URL url = new URL("http://121.248.70.120/jwweb/ZNPK/Private/List_JS.aspx?xnxq=20180&t=88");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Referer", referer);
        //connection.setRequestProperty("Accept-Encoding", "identity");
        connection.setDoInput(true);
        connection.connect();
        InputStream in = connection.getInputStream();
        //int len = connection.getContentLength();
        int step = 0, off = 0;
        byte[] buf = new byte[512];
        byte[] finalBuf = new byte[1000000];
        while ((step = in.read(buf)) != -1) {
            //infoBuilder.append(new String(infoBuf, "GBK"));
            System.arraycopy(buf, 0, finalBuf, off, step);
            off += step;

        }
        in.close();
        isUpdateWebView = true;
        return new String(finalBuf, "GBK");
    }

    private String getBaseInfoTable() throws Exception {
        URL url = new URL(referer);
        URLConnection connection = url.openConnection();
        connection.setDoInput(true);
        InputStream in = connection.getInputStream();
        int step = 0, off = 0;
        byte[] buf = new byte[512];
        byte[] finalBuf = new byte[1000000];
        while ((step = in.read(buf)) != -1) {
            //infoBuilder.append(new String(infoBuf, "GBK"));
            System.arraycopy(buf, 0, finalBuf, off, step);
            off += step;

        }
        in.close();
        isGotBaseInfo = true;
        return new String(finalBuf, "GBK");
    }

    public void startMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startTest(View view) {
        Intent intent = new Intent(this, Test.class);
        startActivity(intent);
    }
}
