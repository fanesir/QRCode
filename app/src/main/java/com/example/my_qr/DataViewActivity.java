package com.example.my_qr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class DataViewActivity extends AppCompatActivity { //登入成功的地方
    private static String TAG = "DEBUG_LOG";
    ListView lv;
    NavigationView navigationView;

    EditText searchField;
    HttpRequest.ItemState searchState;
    public static List<HttpRequest.ItemInfo> list_view_data = new LinkedList<>();//宣告空的陣列

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);

        ItemAdapter adapter = new ItemAdapter(list_view_data);
        lv = findViewById(R.id.lv);
        lv.setAdapter(adapter);

        searchField = findViewById(R.id.item_search_field);
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.swiperefresh);
        navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        Button showbrrow = headerView.findViewById(R.id.showbrrow);
        CheckBox getall = headerView.findViewById(R.id.fixing);
        CheckBox inventory = headerView.findViewById(R.id.discard);
        CheckBox no_inventory = headerView.findViewById(R.id.correct);
        headerView.findViewById(R.id.start_camera_activity).setOnClickListener(this::startCameraActivity);
        headerView.findViewById(R.id.create_borrower).setOnClickListener(view -> {
            Intent intent = new Intent(DataViewActivity.this, NewBorrowerActivity.class);
            startActivity(intent);
        });

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
                searchState = new HttpRequest.ItemState();
                searchState.correct = false;
                getall.setChecked(false);
                inventory.setChecked(false);
            }
            clearAndReloadItems(20);
        });

        showbrrow.setOnClickListener(view -> {
            Intent intent = new Intent(DataViewActivity.this, ListBorrowerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        //AdapterView 是一個類別 裡面的 intface OnItemClickListener void() 介面
        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            ItemAdapter itemAdapter = (ItemAdapter) adapterView.getAdapter();
            HttpRequest.ItemInfo info = (HttpRequest.ItemInfo) itemAdapter.getItem(i);//第幾個並把資料帶過去

            Toast.makeText(DataViewActivity.this, "選取修改:" + info.name, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(DataViewActivity.this, UpdateItemContent.class);
            intent.putExtra("item_info", info);

            startActivity(intent);
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

        findViewById(R.id.sideBarButton).setOnClickListener(this::openSideBar);

        loadAndRefreshItems(20);//都先拿20個
    }

    Thread clearAndReloadItems(int limit) {
        list_view_data.clear();
        return loadAndRefreshItems(limit);
    }

    Thread loadAndRefreshItems(int limit) {
        Thread thread = new Thread(() -> {
            try {
                List<HttpRequest.ItemInfo> result = null;//裝各種物件資料
                if (limit != 0) {
                    result = HttpRequest.getInstance().GetItem(limit, list_view_data.size(), searchState);
                }

                List<HttpRequest.ItemInfo> finalResult = result;
                runOnUiThread(() -> {
                    if (limit != 0) {
                        list_view_data.addAll(finalResult);
                    }
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

    Thread searchLoadAndRefreshItems(String name) {
        Thread thread = new Thread(() -> {
            try {
                List<HttpRequest.ItemInfo> result = HttpRequest.getInstance().SearchItem(name);

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
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle("離開");
            ad.setMessage("離開程式?");
            ad.setPositiveButton("是", (dialogInterface, i) -> {
                System.exit(0); // 離開程式
            }).show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void startCameraActivity(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void openSideBar(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);//  drawer.openDrawer(GravityCompat.START);
    }

    public void search(View view) {
        String name = searchField.getText().toString();
        searchLoadAndRefreshItems(name);
        clearAndReloadItems(0);//清掉

    }

    static class ItemAdapter extends BaseAdapter {
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
}