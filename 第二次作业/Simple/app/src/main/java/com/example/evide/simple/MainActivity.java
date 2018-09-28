package com.example.evide.simple;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView lv;
    private EditText eName;
    private EditText eSalary;
    private EditText eDelete;
    private SQLiteDatabase db_w;
    private SQLiteDatabase db_r;
    private ArrayList<HashMap<String, Object>> litem;
    private SimpleAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        Initialize Layout Objs
        */
        eName = findViewById(R.id.input_name);
        eSalary = findViewById(R.id.input_salary);
        eDelete = findViewById(R.id.eDelArg);
        lv = (ListView) findViewById(R.id.lv);
        /*
        Initialize DS Objs
        */
        myHelper helper = new myHelper(this, "demo", null, 1);
        db_w = helper.getWritableDatabase();
        db_r = helper.getReadableDatabase();

        litem = new ArrayList<HashMap<String, Object>>();
        adapter = new SimpleAdapter(this, litem, R.layout.item,
                new String[]{"id", "cname", "salary"},
                new int[]{R.id.uid, R.id.cname, R.id.salary});
        lv_refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db_w.close();
        db_r.close();
    }

    private void setListViewRefresh(ListView lv) {
        ListAdapter lvadp = lv.getAdapter();
        int h = 0;
        for (int i = 0; i < lvadp.getCount(); i++) {
            View litem = lvadp.getView(i, null, lv);
            litem.measure(0, 0);
            h += litem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = lv.getLayoutParams();
        //params.height = h + (lv.getDividerHeight() * lvadp.getCount());
        lv.setLayoutParams(params);
    }

    public void submit(View view) {
        double salary = 0;
        String cname;
        try {
            cname = String.valueOf(eName.getText());
            salary = Double.parseDouble(String.valueOf(eSalary.getText()));
        }catch (Exception e){
            Toast.makeText(this, "输入参数非法" + e, Toast.LENGTH_SHORT).show();
            return;
        }
        if (salary < 0) {
            Toast.makeText(this, "请输入正确的工资数额" , Toast.LENGTH_SHORT).show();
            return;
        }
        String sql="insert into customers (cname,salary) values(?,?)";
        db_w.execSQL(sql,new Object[]{cname, salary});
        lv_refresh();
    }

    public void lv_refresh() {
        String sql="select * from customers";
        Cursor c = db_r.rawQuery(sql, null);
        HashMap<String, Object> map;
        litem.clear();
        while (c.moveToNext()){
            map = new HashMap<String, Object>();
            String cname=c.getString(c.getColumnIndex("cname"));
            double salary=c.getDouble(c.getColumnIndex("salary"));
            int id = c.getInt(c.getColumnIndex("id"));
            map.put("id", id);
            map.put("cname", cname);
            map.put("salary", salary);
            litem.add(map);
        }
        lv.setAdapter(adapter);
        setListViewRefresh(lv);
    }

    public void delete_record(View view) {
        String Arg = String.valueOf(eDelete.getText());
        int start, end;
        try {
            if (Arg.indexOf('-') == -1) {
                start = Integer.parseInt(Arg);
                end = start;
            } else {
                start = Integer.parseInt(Arg.split("-")[0]);
                end = Integer.parseInt(Arg.split("-")[1]);
            }
        } catch (Exception e) {
            Toast.makeText(this, "输入参数非法" + e, Toast.LENGTH_SHORT).show();
            return;
        }
        int n = end - start + 1;
        String sql="delete from customers where id = ?";
        for (int i = 0; i < n ; ++i)
            db_w.execSQL(sql,new Object[]{start + i});
        lv_refresh();
    }
}