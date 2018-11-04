package com.example.evide.simpleclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class showTable extends AppCompatActivity {

    private WebView wvShow;
    private String info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_table);

        String  fname = "/data/data/com.example.evide.simpleclient/files/" + getIntent().getStringExtra("path");

        try {
            FileInputStream fileInputStream = new FileInputStream(fname);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String item;
            while ((item = reader.readLine()) != null) {
                info += item;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        wvShow = findViewById(R.id.wvShowTable);
        wvShow.loadData(info, "text/html", "UTF-8");
}
}
