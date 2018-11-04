package com.example.evide.simpleclient;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.CompletionInfo;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Context ctx = this;
    private String addressVcode = "http://xxx.xxx.xxx/jwweb/sys/ValidateCode.aspx?t=513";
    private String addressRPT = "http://xxx.xxx.xxx/jwweb/ZNPK/TeacherKBFB_rpt.aspx";
    private String referer = "http://xxx.xxx.xxx/jwweb/ZNPK/TeacherKBFB.aspx";
    private ImageView ivVCode;
    private EditText etVCode;
    private AutoCompleteTextView actvName;
    private Spinner spTerm;
    private Spinner spTeacher;
    private boolean isRefreshVCode = false;
    private boolean isVCodePending = false;
    private boolean isDemoInfoGot = false;
    private boolean isDemoInfoPending = false;
    private boolean isUpdateUI = false;
    private String demoInfo = null;
    private SQLiteDatabase db_r;
    private SQLiteDatabase db_w;
    private DBHelper dbHelper = new DBHelper(this, "teachers", null, 1);

    private ArrayList<String> inDBTeachers = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private ArrayList<String> teacherListNameOnly = new ArrayList<>();

    private ArrayList<String> teacherList;

    private String curTeacherCode = "xxxxx";
    private String curCookie = null;
    private String curTerm = "20180";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivVCode = findViewById(R.id.ivVCode);
        etVCode = findViewById(R.id.etVCode);
        actvName = findViewById(R.id.actvName);
        spTeacher = findViewById(R.id.spTeacher);
        //Refresh when start activity
        isRefreshVCode = true;

        //wvMain.setBackgroundColor(Color.TRANSPARENT);
        //wvMain.getSettings().setJavaScriptEnabled(true);

        db_r = dbHelper.getReadableDatabase();
        db_w = dbHelper.getWritableDatabase();
        teacherList =  Util.getTeachers(db_r);
        inDBTeachers = Util.thisTermClassInDB(db_r);

        for (String item : teacherList) {
            teacherListNameOnly.add(item.substring(item.indexOf('\n') + 1));
        }

        /*spTeacher.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, teacherListNameOnly));

        spTeacher.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = teacherList.get(position);
                curTeacherCode  = item.substring(0,item.indexOf('\n'));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, teacherListNameOnly);
        actvName.setAdapter(adapter);

        actvName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = String.valueOf(actvName.getText());
                int pos = teacherListNameOnly.indexOf(name);
                String item = teacherList.get(pos);
                curTeacherCode  = item.substring(0,item.indexOf('\n'));

                if (inDBTeachers.contains(curTeacherCode)) {
                    actvName.setBackgroundColor(Color.GREEN);
                } else {
                    actvName.setBackgroundColor(Color.DKGRAY);
                }
            }
        });

        /*actvName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        final Handler handler = new Handler();

        new Thread(){
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isRefreshVCode) {
                        isRefreshVCode = false;
                        isVCodePending = true;
                        try {
                            getCookie();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (isUpdateUI) {
                        handler.post(upDateUIVCode);
                    }
                    if (isDemoInfoGot) {
                        //handler.post(upDateWebView);
                        isDemoInfoGot = false;
                    }
                    handler.post(isCached);
                }
            }
        }.start();
    }

    private Runnable isCached = new Runnable(){

        @Override
        public void run() {
            String name = String.valueOf(actvName.getText());
            int pos = teacherListNameOnly.indexOf(name);
            if (pos != -1) {
                String item = teacherList.get(pos);
                curTeacherCode = item.substring(0, item.indexOf('\n'));

                if (inDBTeachers.contains(curTeacherCode)) {
                    actvName.setBackgroundColor(Color.GREEN);
                } else {
                    actvName.setBackgroundColor(Color.DKGRAY);
                }
            }
        }
    };

    private Runnable upDateWebView = new Runnable() {
        @Override
        public void run() {
            Log.e("Info", demoInfo);
            //wvMain.loadData(demoInfo, "text/html;", "UTF-8");
            //wvMain.invalidate();
            isDemoInfoGot = false;
        }
    };



    private Runnable upDateUIVCode = new Runnable() {
        @Override
        public void run() {
            Bitmap bmp = BitmapFactory.decodeFile("/data/data/com.example.evide.simpleclient/files/img.png");
            ivVCode.setImageBitmap(bmp);
            isUpdateUI = false;
        }
    };

    private void getCookie() throws Exception {
        isVCodePending = true;
        URL url = new URL(addressVcode);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("Referer", referer);
        connection.setDoInput(true);
        connection.connect();
        curCookie = connection.getHeaderField("Set-Cookie");
        byte[] buf = new byte[512];
        InputStream in = connection.getInputStream();
        FileOutputStream out = openFileOutput("img.png", Context.MODE_PRIVATE);
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        isUpdateUI = true;
        isVCodePending = false;
    }

    private String demoGetInfo(String cookie, String VCode) throws Exception {
        isDemoInfoPending = true;
        URL url = new URL(addressRPT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Referer", referer);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Cookie", cookie);
        connection.setRequestProperty("Referer", referer);
        connection.setRequestMethod("POST");
        StringBuilder builder = new StringBuilder();
        builder.append("Sel_XNXQ=" + curTerm + "&Sel_JS=" + curTeacherCode + "&type=1&txt_yzm=" + VCode);
        Log.e("PARG", builder.toString());
        OutputStream out = connection.getOutputStream();
        out.write(builder.toString().getBytes());
        //int len = connection.getContentLength();
        StringBuilder infoBuilder = new StringBuilder();
        InputStream in = connection.getInputStream();
        int step = 0, off = 0;
        byte[] buf = new byte[512];
        byte[] finalBuf = new byte[1000000];
        while ((step = in.read(buf)) != -1) {
            System.arraycopy(buf, 0, finalBuf, off, step);
            off += step;

        }
        byte[] resultBuf = new byte[off];
        System.arraycopy(finalBuf, 0, resultBuf, 0, off);
        in.close();
        out.close();
        isDemoInfoGot = true;
        isDemoInfoPending = false;
        String result = new String(resultBuf, "GBK");
        if (result.indexOf("验证码错误") != -1) {
            if (!isVCodePending) {
                etVCode.setText("");
                isRefreshVCode = true;
            }
            return "验证码错误";
        }
        return result;
    }

    public void refreshVCode(View view) {
        if (!isVCodePending)
            isRefreshVCode = true;
    }

    public void submit(View view) {
        if (isDemoInfoPending)
            return;
        if (inDBTeachers.contains(curTeacherCode)) {
            Intent intent = new Intent(ctx, showTable.class);
            intent.putExtra("path", curTerm + curTeacherCode);
            startActivity(intent);
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        demoInfo = demoGetInfo(curCookie, etVCode.getText().toString());
                        File f = new File("/data/data/com.example.evide.simpleclient/files/" + curTerm + curTeacherCode);
                        try {
                                f.createNewFile();
                                FileWriter writer = new FileWriter(f);
                                writer.write(demoInfo);
                                writer.flush();
                                writer.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                        if (demoInfo.indexOf("验证码错误") == -1) {
                            Util.addClassToDB(db_w, curTerm, curTeacherCode);
                            inDBTeachers = Util.thisTermClassInDB(db_r);
                        }
                        /*for (String i : inDBTeachers) {
                            Log.e("GET", i + "\t" + curTeacherCode);
                        }*/
                        Intent intent = new Intent(ctx, showTable.class);
                        intent.putExtra("path", curTerm + curTeacherCode);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public void refreshTable(View view) {
        if (isDemoInfoPending)
            return;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        demoInfo = demoGetInfo(curCookie, etVCode.getText().toString());
                        File f = new File("/data/data/com.example.evide.simpleclient/files/" + curTerm + curTeacherCode);
                        try {
                            f.createNewFile();
                            FileWriter writer = new FileWriter(f);
                            writer.write(demoInfo);
                            writer.flush();
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (demoInfo.indexOf("验证码错误") == -1) {
                            Util.addClassToDB(db_w, curTerm, curTeacherCode);
                            inDBTeachers = Util.thisTermClassInDB(db_r);
                        }
                        /*for (String i : inDBTeachers) {
                            Log.e("GET", i + "\t" + curTeacherCode);
                        }*/
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    postData( curTerm + curTeacherCode, demoInfo);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        Intent intent = new Intent(ctx, showTable.class);
                        intent.putExtra("path", curTerm + curTeacherCode);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

    }

    private String postData(String filename, String data) throws Exception {
        URL url = new URL("http://192.168.8.25:8000/demo.lua");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        StringBuilder builder = new StringBuilder();
        builder.append("Filename:" + filename);
        builder.append(data);
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
        Log.e("PARG", new String(finalBuf, "UTF-8"));
        return  new String(finalBuf, "UTF-8");
    }
}
