package com.example.evide.demo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private TextView tv_red;
    private TextView tv_green;
    private TextView tv_blue;
    private SeekBar seekbar_red;
    private SeekBar seekbar_green;
    private SeekBar seekbar_blue;
    private TextView tv_out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_red = (TextView) findViewById(R.id.tv_red);
        tv_green = (TextView) findViewById(R.id.tv_green);
        tv_blue = (TextView) findViewById(R.id.tv_blue);
        tv_out = (TextView) findViewById(R.id.tv_out);
        tv_red.setText("Red: 0");
        tv_green.setText("Green: 0");
        tv_blue.setText("Blue: 0");
        seekbar_red = (SeekBar) findViewById(R.id.sb_red);
        seekbar_green = (SeekBar) findViewById(R.id.sb_green);
        seekbar_blue= (SeekBar) findViewById(R.id.sb_blue);
        seekbar_red.setOnSeekBarChangeListener(this);
        seekbar_green.setOnSeekBarChangeListener(this);
        seekbar_blue.setOnSeekBarChangeListener(this);
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        switch(seekBar.getId()) {
            case R.id.sb_red:{
                tv_red.setText("Red: "+String.valueOf(seekBar.getProgress()));
                break;
            }
            case R.id.sb_green:{
                tv_green.setText("Green: "+String.valueOf(seekBar.getProgress()));
                break;
            }
            case R.id.sb_blue:{
                tv_blue.setText("Blue: "+String.valueOf(seekBar.getProgress()));
                break;
            }
            default:
                break;
        }
        tv_out.setBackgroundColor(Color.rgb(seekbar_red.getProgress(), seekbar_green.getProgress(), seekbar_blue.getProgress()));
    }
}
