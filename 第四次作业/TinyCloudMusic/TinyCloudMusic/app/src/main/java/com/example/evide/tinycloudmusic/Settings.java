package com.example.evide.tinycloudmusic;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Settings extends AppCompatActivity {

    private EditText etHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        etHost = findViewById(R.id.etHost);
        SharedPreferences sp = getSharedPreferences("CloudSettings", MODE_PRIVATE);
        etHost.setText(sp.getString("host", "输入 URL"));
    }

    public void saveSetting(View view) {
        SharedPreferences sp = getSharedPreferences("CloudSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("host", etHost.getText().toString());
        editor.commit();
        finish();
    }
}
