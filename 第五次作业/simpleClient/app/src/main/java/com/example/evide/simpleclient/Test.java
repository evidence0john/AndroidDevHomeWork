package com.example.evide.simpleclient;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Test extends AppCompatActivity {

    private boolean isDataGot = false;
    private TextView tvTest;
    private String page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        tvTest = findViewById(R.id.tvTest);

        final Handler handler = new Handler();

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    page = demoGetInfo("cookie");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isDataGot) {
                        isDataGot = false;
                        handler.post(gotData);
                    }
                }
            }
        }.start();

    }

    private Runnable gotData = new Runnable() {
        @Override
        public void run() {
            tvTest.setText(page);
        }
    };


    private String demoGetInfo(String cookie) throws Exception {
        URL url = new URL("http://192.168.8.25:8000/demo.lua");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Cookie", cookie);
        connection.setRequestMethod("POST");
        StringBuilder builder = new StringBuilder();
        builder.append("sssssssssssssssssssssssssssss");
        Log.e("PARG", builder.toString());
        OutputStream out = connection.getOutputStream();
        out.write(builder.toString().getBytes());
        int len = connection.getContentLength();
        StringBuilder infoBuilder = new StringBuilder();
        InputStream in = connection.getInputStream();
        int step = 0, off = 0;
        byte[] buf = new byte[512];
        byte[] finalBuf = new byte[len];
        while ((step = in.read(buf)) != -1) {
            System.arraycopy(buf, 0, finalBuf, off, step);
            off += step;

        }
        in.close();
        out.close();
        isDataGot = true;
        return  new String(finalBuf, "UTF-8");
    }

}
