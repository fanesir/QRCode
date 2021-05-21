package com.example.my_qr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestMainActivity extends AppCompatActivity {
    String st1[] = {"Name01", "Name02", "Name03", "Name04", "Name05", "Name0123121321", "htc10"};
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);
        listView = findViewById(R.id.getlistviewtestt);
        listView.setAdapter(new MainClass(this, new ArrayList<String>(Arrays.asList(st1))));
    }

    class MainClass extends TestExtendBaseadpter {

        protected MainClass(Context context, ArrayList extentarray) {
            super(context, extentarray);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
            }
            TextView item_name = view.findViewById(R.id.item_name);
            item_name.setText(extentarray.get(i) + "");
            return view;

        }

    }

}