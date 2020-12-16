package com.example.my_qr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


public class DataViewActivity extends AppCompatActivity { //登入成功的地方
    ListView lv;
    NavigationView navigationView;

    EditText searchField;
    HttpRequest.ItemState searchState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);

        ItemAdapter adapter = new ItemAdapter(this, searchState);
        lv = findViewById(R.id.lv);
        lv.setAdapter(adapter);

        searchField = findViewById(R.id.item_search_field);
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
            clearAndReloadItems();
        });

        inventory.setOnCheckedChangeListener((compoundButton, b) -> {
            if (inventory.isChecked()) {
                searchState = new HttpRequest.ItemState();
                searchState.correct = true;
                getall.setChecked(false);
                no_inventory.setChecked(false);
            }
            clearAndReloadItems();
        });

        no_inventory.setOnCheckedChangeListener((compoundButton, b) -> {
            if (no_inventory.isChecked()) {
                searchState = new HttpRequest.ItemState();
                searchState.correct = false;
                getall.setChecked(false);
                inventory.setChecked(false);
            }
            clearAndReloadItems();
        });

        showbrrow.setOnClickListener(view -> {
            Intent intent = new Intent(DataViewActivity.this, ListBorrowerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        //AdapterView 是一個類別 裡面的 interface OnItemClickListener void() 介面
        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            ItemAdapter itemAdapter = (ItemAdapter) adapterView.getAdapter();
            HttpRequest.ItemInfo info = (HttpRequest.ItemInfo) itemAdapter.getItem(i);//第幾個並把資料帶過去

            Toast.makeText(DataViewActivity.this, "選取修改:" + info.name, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(DataViewActivity.this, UpdateItemContent.class);
            intent.putExtra("item_info", info);

            startActivity(intent);
        });

        findViewById(R.id.sideBarButton).setOnClickListener(this::openSideBar);
    }

    void clearAndReloadItems() {
        lv.setAdapter(new ItemAdapter(this, searchState));
    }

    void searchLoadAndRefreshItems(String name) {
//      List<HttpRequest.ItemInfo> result = HttpRequest.getInstance().SearchItem(name);
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
        clearAndReloadItems();//清掉
    }

    static class ItemAdapter extends BaseAdapter {
        protected final List<HttpRequest.ItemInfo> list = new ArrayList<>();
        protected final int loadThreshold;
        protected final int limit = 20;
        protected final HttpRequest.ItemState searchState;
        protected final Activity activity;
        protected boolean done = false;
        int times = 0;
        int innerTimes = 0;

        ItemAdapter(Activity activity, HttpRequest.ItemState searchState) {
            this(activity, searchState, 20);
        }

        ItemAdapter(Activity activity, HttpRequest.ItemState searchState, int loadThreshold) {
            this.searchState = searchState;
            this.loadThreshold = loadThreshold;
            this.activity = activity;
            loadItems();
        }

        void loadItems() {
            Log.d("loadItems Times", (times += 1) + "");

            new Thread(() -> {
                synchronized (ItemAdapter.this) {
                    if (done) {
                        return;
                    }
                    Log.d("loadItems Thread Times", (innerTimes += 1) + "");
                    try {
                        List<HttpRequest.ItemInfo> result = HttpRequest.getInstance().GetItem(limit, this.list.size(), searchState);
                        if (result.size() == 0) {
                            done = true;
                        } else {
                            activity.runOnUiThread(() -> {
                                this.list.addAll(result);
                                notifyDataSetChanged();
                            });
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    } catch (HttpRequest.GetDataError getDataError) {
                        done = true;
                    }
                }
            }).start();
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
            if (!done && i + this.loadThreshold > this.list.size()) {
                this.loadItems();
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