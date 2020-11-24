package com.example.my_qr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class DataViewActivity extends AppCompatActivity { //登入成功的地方
    private static String TAG = "DEBUG_LOG";
    ListView lv;
    NavigationView navigationView;

    Button put_cam;
    CheckBox getall, inventory, no_inventory;
    HttpRequest.ItemState searchState;
    List<HttpRequest.ItemInfo> list_view_data = new LinkedList<>();//宣告空的陣列

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);
        lv = findViewById(R.id.lv);
        ItemAdapter adapter = new ItemAdapter(list_view_data);
        lv.setAdapter(adapter);

        SwipeRefreshLayout pullToRefresh = findViewById(R.id.swiperefresh);

        navigationView = findViewById(R.id.nav_view);


        put_cam = navigationView.getHeaderView(0).findViewById(R.id.push_cam);//get 裡面的button4
        put_cam.setOnClickListener((View v) -> {
            push_cam();
        });
        getall = navigationView.getHeaderView(0).findViewById(R.id.fixing);
        inventory = navigationView.getHeaderView(0).findViewById(R.id.discard);
        no_inventory = navigationView.getHeaderView(0).findViewById(R.id.correct);


        getall.setOnCheckedChangeListener((compoundButton, b) -> {
            if (getall.isChecked()) {
                searchState = null;
                inventory.setChecked(false);
                no_inventory.setChecked(false);
            }
            clearAndReloadItems(20);
        });
        inventory.setOnCheckedChangeListener((compoundButton, b) -> {
            if (inventory.isChecked()) {
                searchState = new HttpRequest.ItemState();
                searchState.correct = true;
                getall.setChecked(false);
                no_inventory.setChecked(false);
            }
            clearAndReloadItems(20);
        });

        no_inventory.setOnCheckedChangeListener((compoundButton, b) -> {
            if (no_inventory.isChecked()) {
                Log.i("123", "654");
                searchState = new HttpRequest.ItemState();
                searchState.correct = false;
                getall.setChecked(false);
                inventory.setChecked(false);
            }
            clearAndReloadItems(20);
        });


        //AdapterView 是一個類別 裡面的 intface OnItemClickListener void() 介面
        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            ItemAdapter itemAdapter = (ItemAdapter) adapterView.getAdapter();
            HttpRequest.ItemInfo info = (HttpRequest.ItemInfo) itemAdapter.getItem(i);//第幾個並把資料帶過去

            Toast.makeText(DataViewActivity.this, "選取修改:" + info.name, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(DataViewActivity.this, UpdataContent.class);
            intent.putExtra("item_info", info);

            startActivity(intent);
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //drawer.closeDrawer(GravityCompat.START);

                return false;
            }
        });


        pullToRefresh.setOnRefreshListener(() -> {

            Thread thread = loadAndRefreshItems(5);
            new Thread(() -> {
                try {
                    thread.join();//從中介入
                } catch (InterruptedException ignored) {
                } finally {
                    runOnUiThread(() -> pullToRefresh.setRefreshing(false));
                }
            }).start();
        });

        loadAndRefreshItems(20);//都先拿20個

    }

    Thread clearAndReloadItems(int limit) {
        list_view_data.clear();
        return loadAndRefreshItems(limit);
    }

    Thread loadAndRefreshItems(int limit) {
        Thread thread = new Thread(() -> {
            try {
                List<HttpRequest.ItemInfo> result = HttpRequest.getInstance().GetItem(limit, list_view_data.size(), searchState, searchState);// 我要拿 0~20個

                runOnUiThread(() -> {
                    list_view_data.addAll(result);
                    synchronized (lv.getAdapter()) {
                        ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
                    }
                });
            } catch (IOException | JSONException | HttpRequest.GetDataError e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {//KeyEvent.KEYCODE_BACK=4
            toushgoBack();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void toushgoBack() {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("離開");
        ad.setMessage("離開程式?");
        ad.setPositiveButton("是", (dialogInterface, i) -> {
            System.exit(0); // 離開程式

        }).show();

    }

    public void push_cam() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void opendrawer(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);//  drawer.openDrawer(GravityCompat.START);

    }
}

class ItemAdapter extends BaseAdapter {
    private final List<HttpRequest.ItemInfo> list;
    List<String> listt = new ArrayList<>();

    ItemAdapter(List<HttpRequest.ItemInfo> list) {
        this.list = list;
    }

    public void setData(List<String> list) {
        listt = list;
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int i) {

        return this.list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());//初始一個背後 layout畫板
            view = inflater.inflate(R.layout.list_item, null);
        }
        HttpRequest.ItemInfo info = (HttpRequest.ItemInfo) this.getItem(i);//?

        TextView item_name = view.findViewById(R.id.item_name);
        TextView item_status = view.findViewById(R.id.item_status);
        TextView item_local = view.findViewById(R.id.localitem);

        item_name.setText(info.name);
        item_local.setText(info.location);
        if (info.correct) {
            item_status.setTextColor(Color.argb(255, 0, 255, 0));
            item_status.setText("已盤點");
        } else {
            item_status.setTextColor(Color.argb(255, 255, 0, 0));
            item_status.setText("未盤點");
        }
        return view;
    }


}