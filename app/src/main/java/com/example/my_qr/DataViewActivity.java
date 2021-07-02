package com.example.my_qr;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
    MenuItem getAllItem, menuItemInventory, menuItemNoInventory, discard, fixIng, unlabel;
    Boolean correctboolean;
    private static Context mContext;

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


    LoadData<HttpRequest.ItemInfo> searchName = (int offset) -> {
        try {
            ExtentBaseAdpter.LoadState<HttpRequest.ItemInfo> state = new ExtentBaseAdpter.LoadState<>();
            state.result = HttpRequest.getInstance().SearchItem(DataViewActivity.this.searchField.getText().toString());
            state.has_next = false;
            return state;
        } catch (IOException | JSONException | HttpRequest.GetDataError e) {//InterruptedException
            e.printStackTrace();
        }
        return null;
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);
        lv = findViewById(R.id.lv);
        lv.setAdapter(new ItemAdapter(this, loadItem));
        searchField = findViewById(R.id.item_search_field);

        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener((MenuItem menuItem) -> {
            int id = menuItem.getItemId();
            if (id == R.id.itemcamera) {
                Intent intent = new Intent(DataViewActivity.this, CameraInputData.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                DataViewActivity.this.startActivity(intent);
            }
            if (id == R.id.allbrroweraccount) {
                Intent intent = new Intent(DataViewActivity.this, ListBorrowerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                DataViewActivity.this.startActivity(intent);
            }

            if (id == R.id.addBrrowerAccount) {
                Intent intent = new Intent(DataViewActivity.this, CameraGetItemId.class);
                startActivity(intent);
            }

            return false;
        });

        Menu menu = navigationView.getMenu();
        getAllItem = menu.findItem(R.id.getAllItem);
        View getAllactionView = MenuItemCompat.getActionView(getAllItem);
        getAllactionView.setOnClickListener((View view) -> {
            searchState = null;
            select(null, false, false, false, false);
            CompoundButton checkInventor = (CompoundButton) menuItemInventory.getActionView();//CompoundButton
            CompoundButton checkNoInventor = (CompoundButton) menuItemNoInventory.getActionView();
            checkInventor.setChecked(false);
            checkNoInventor.setChecked(false);
            CompoundButton clreardiscard = (CompoundButton) discard.getActionView();
            clreardiscard.setChecked(false);
            CompoundButton clrearunlabel = (CompoundButton) unlabel.getActionView();
            clrearunlabel.setChecked(false);
            CompoundButton clrearfixIng = (CompoundButton) fixIng.getActionView();
            clrearfixIng.setChecked(false);
            clearAndReloadItems();
        });

        menuItemInventory = menu.findItem(R.id.menuItemInventory);
        View getinventory = MenuItemCompat.getActionView(menuItemInventory);
        getinventory.setOnClickListener((View view) -> {
            searchState = new HttpRequest.ItemState();
            select(searchState, true, false, false, false);
            correctboolean = true;
            CompoundButton checkAllItem = (CompoundButton) getAllItem.getActionView();
            CompoundButton checkNoInventor = (CompoundButton) menuItemNoInventory.getActionView();
            checkAllItem.setChecked(false);
            checkNoInventor.setChecked(false);

        });

        menuItemNoInventory = menu.findItem(R.id.menuItemNoInventory);
        View noInventory = MenuItemCompat.getActionView(menuItemNoInventory);
        noInventory.setOnClickListener((View view) -> {
            searchState = new HttpRequest.ItemState();
            select(searchState, false, false, false, false);
            correctboolean = false;
            CompoundButton checkAllItem = (CompoundButton) getAllItem.getActionView();
            CompoundButton checkInventor = (CompoundButton) menuItemInventory.getActionView();
            checkAllItem.setChecked(false);
            checkInventor.setChecked(false);

        });
        discard = menu.findItem(R.id.scrapped);
        View discardView = MenuItemCompat.getActionView(discard);
        discardView.setOnClickListener((View view) -> {
            select(searchState, correctboolean, true, false, false);
            CompoundButton checkAllItem = (CompoundButton) fixIng.getActionView();
            CompoundButton checkInventor = (CompoundButton) unlabel.getActionView();
            checkAllItem.setChecked(false);
            checkInventor.setChecked(false);
        });
        fixIng = menu.findItem(R.id.fix);
        View fixIngView = MenuItemCompat.getActionView(fixIng);
        fixIngView.setOnClickListener((View view) -> {
            select(searchState, correctboolean, false, true, false);
            CompoundButton checkAllItem = (CompoundButton) discard.getActionView();
            CompoundButton checkInventor = (CompoundButton) unlabel.getActionView();
            checkAllItem.setChecked(false);
            checkInventor.setChecked(false);
        });

        unlabel = menu.findItem(R.id.notposted);
        View unlabelView = MenuItemCompat.getActionView(unlabel);
        unlabelView.setOnClickListener((View view) -> {
            select(searchState, correctboolean, false, false, true);
            CompoundButton checkAllItem = (CompoundButton) discard.getActionView();
            CompoundButton checkInventor = (CompoundButton) fixIng.getActionView();
            checkAllItem.setChecked(false);
            checkInventor.setChecked(false);
        });

        lv.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            ItemAdapter itemAdapter = (ItemAdapter) adapterView.getAdapter();
            HttpRequest.ItemInfo info = (HttpRequest.ItemInfo) itemAdapter.getItem(i);//第幾個並把資料帶過去

            Intent intent = new Intent(DataViewActivity.this, UpdateItemContent.class);
            intent.putExtra("item_info", info.item_id);
            UpdateItemContent.fromdataview_int=11;
            startActivity(intent);
        });

        findViewById(R.id.sideBarButton).setOnClickListener(this::openSideBar);

    }

      static void upLoad(Context context){
          Intent intent = new Intent(context, DataViewActivity.class);
          context.startActivity(intent);
     }

    void clearAndReloadItems() {
        lv.setAdapter(new ItemAdapter(this, loadItem));//當有條件搜尋時就會刷新的物件進去

    }

    public void select(HttpRequest.ItemState searchState, Boolean correct, Boolean discard, Boolean fixIng, Boolean unlabel) {
        if (searchState == null) {
            return;
        }
        searchState.correct = correct;
        searchState.discard = discard;
        searchState.fixing = fixIng;
        searchState.unlabel = unlabel;
        LoadData<HttpRequest.ItemInfo> loadItem = (int offset) -> {
            try {
                ExtentBaseAdpter.LoadState<HttpRequest.ItemInfo> state = new ExtentBaseAdpter.LoadState<>();
                state.result = HttpRequest.getInstance().GetItem(20, offset, searchState);
                state.has_next = true;
                return state;
            } catch (IOException | JSONException | HttpRequest.GetDataError e) {//InterruptedException
                e.printStackTrace();
            }
            return null;
        };
        lv.setAdapter(new ItemAdapter(this, loadItem));

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setTitle("離開");
            ad.setMessage("離開程式?");
            ad.setPositiveButton("是", (DialogInterface dialogInterface, int i) -> {
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
            TextView item_local = view.findViewById(R.id.localitem);
            ImageView item_start = view.findViewById(R.id.item_imageviewstatus);

            item_name.setText(info.name);
            item_local.setText(info.location);
            if (info.correct) {
                item_start.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                item_start.setImageResource(android.R.drawable.btn_star_big_off);
            }
            return view;
        }
    }


}