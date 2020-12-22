package com.example.my_qr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_qr.ExtentBaseAdpter.LoadData;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;


public class DataViewActivity extends AppCompatActivity { //登入成功的地方
    ListView lv;
    HttpRequest.ItemState searchState;
    EditText searchField;
    MenuItem getAllItem, menuItemInventory, menuItemNoInventory;

    LoadData<HttpRequest.ItemInfo> loadItem = new LoadData<HttpRequest.ItemInfo>() {
        @Override
        public ExtentBaseAdpter.LoadState<HttpRequest.ItemInfo> load(int offset) {
            try {
                ExtentBaseAdpter.LoadState<HttpRequest.ItemInfo> state = new ExtentBaseAdpter.LoadState<>();
                state.result = HttpRequest.getInstance().GetItem(20, offset, searchState);
                state.has_next = true;
                return state;
            } catch (IOException | JSONException | HttpRequest.GetDataError e) {//InterruptedException
                e.printStackTrace();
            }
            return null;
        }
    };

    LoadData<HttpRequest.ItemInfo> searchName = new LoadData<HttpRequest.ItemInfo>() {
        @Override
        public ExtentBaseAdpter.LoadState<HttpRequest.ItemInfo> load(int offset) {
            try {
                ExtentBaseAdpter.LoadState<HttpRequest.ItemInfo> state = new ExtentBaseAdpter.LoadState<>();
                state.result = HttpRequest.getInstance().SearchItem(DataViewActivity.this.searchField.getText().toString());
                state.has_next = false;
                return state;
            } catch (IOException | JSONException | HttpRequest.GetDataError e) {//InterruptedException
                e.printStackTrace();
            }
            return null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);

        ItemAdapter adapter = new ItemAdapter(this, loadItem);
        lv = findViewById(R.id.lv);

        lv.setAdapter(adapter);
        searchField = findViewById(R.id.item_search_field);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.itemcamera) {
                Intent intent = new Intent(DataViewActivity.this, CameraInputData.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            if (id == R.id.aboutBrrower) {
                Intent intent = new Intent(DataViewActivity.this, ListBorrowerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

            return false;
        });

        Menu menu = navigationView.getMenu();
        getAllItem = menu.findItem(R.id.getAllItem);
        View getAllactionView = MenuItemCompat.getActionView(getAllItem);
        getAllactionView.setOnClickListener(view -> {
            searchState = null;
            CompoundButton checkInventor = (CompoundButton) menuItemInventory.getActionView();//CompoundButton
            CompoundButton checkNoInventor = (CompoundButton) menuItemNoInventory.getActionView();
            checkInventor.setChecked(false);
            checkNoInventor.setChecked(false);
            clearAndReloadItems();
        });

        menuItemInventory = menu.findItem(R.id.menuItemInventory);
        View getinventory = MenuItemCompat.getActionView(menuItemInventory);
        getinventory.setOnClickListener(view -> {
            searchState = new HttpRequest.ItemState();
            searchState.correct = true;
            CompoundButton checkAllItem = (CompoundButton) getAllItem.getActionView();
            CompoundButton checkNoInventor = (CompoundButton) menuItemNoInventory.getActionView();
            checkAllItem.setChecked(false);
            checkNoInventor.setChecked(false);
            clearAndReloadItems();
        });

        menuItemNoInventory = menu.findItem(R.id.menuItemNoInventory);
        View noInventory = MenuItemCompat.getActionView(menuItemNoInventory);
        noInventory.setOnClickListener(view -> {
            searchState = new HttpRequest.ItemState();
            searchState.correct = false;
            CompoundButton checkAllItem = (CompoundButton) getAllItem.getActionView();
            CompoundButton checkInventor = (CompoundButton) menuItemInventory.getActionView();
            checkAllItem.setChecked(false);
            checkInventor.setChecked(false);
            clearAndReloadItems();
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
        lv.setAdapter(new ItemAdapter(this, loadItem));//當有條件搜尋時就會刷新的物件進去
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


    public void openSideBar(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);//  drawer.openDrawer(GravityCompat.START);
    }

    public void search(View view) {

        ItemAdapter adapter = new ItemAdapter(this, searchName);
        lv.setAdapter(adapter);
    }

    static class ItemAdapter extends ExtentBaseAdpter<HttpRequest.ItemInfo> {

        protected ItemAdapter(Activity activity, LoadData<HttpRequest.ItemInfo> callback) {
            super(activity, callback);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());//初始一個背後 layout畫板
                view = inflater.inflate(R.layout.list_item, null);
            }
            if (!done && i + 5 > this.data.size()) {
                this.loadItems();
            }
            HttpRequest.ItemInfo info = (HttpRequest.ItemInfo) this.getItem(i);

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