package com.example.my_qr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;

public class ListBorrowerActivity extends AppCompatActivity {
    String st1[] = {"Name01", "Name02", "Name03", "Name04", "Name05", "Name0123121321", "htc1000000"};
    ListView lvBorrowerAccount;
    HttpRequest.BorrowerInfo getBorrowerInfo;
    SwipyRefreshLayout swipyRefreshLayout;
    int accountId;

    ExtentBaseAdpter.LoadData<HttpRequest.BorrowerInfo> LoadBrrowerInfo = offset -> {
        ExtentBaseAdpter.LoadState<HttpRequest.BorrowerInfo> resuls = new ExtentBaseAdpter.LoadState<>();
        try {
            resuls.result = HttpRequest.getInstance().GetBorrower(30, offset);
            return resuls;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } catch (HttpRequest.GetDataError getDataError) {
            getDataError.printStackTrace();
        }

        return null;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_brrow);

        swipyRefreshLayout = findViewById(R.id.swipyrefreshlayout);
        lvBorrowerAccount = findViewById(R.id.showbrrowlisst);
        lvBorrowerAccount.setAdapter(new BorrowerInfoItemAdpter(this, LoadBrrowerInfo));


        lvBorrowerAccount.setOnItemClickListener((adapterView, view, i, l) -> {//短按顯示此借出人借過的物品
            BorrowerInfoItemAdpter borrowerListAdapter = (BorrowerInfoItemAdpter) adapterView.getAdapter();//getAdapter 方法
            getBorrowerInfo = (HttpRequest.BorrowerInfo) borrowerListAdapter.getItem(i);
            accountId = getBorrowerInfo.id;

        });

        lvBorrowerAccount.setOnItemLongClickListener((adapterView, view, i, l) -> {//長按編輯
            BorrowerInfoItemAdpter borrowerListAdapter = (BorrowerInfoItemAdpter) adapterView.getAdapter();//getAdapter 方法
            HttpRequest.BorrowerInfo getBorrowerInfo = (HttpRequest.BorrowerInfo) borrowerListAdapter.getItem(i);

            Intent intent = new Intent(ListBorrowerActivity.this, UpdataBrrowContent.class);
            intent.putExtra("Brrow", getBorrowerInfo);

            startActivity(intent);
            return false;
        });

        swipyRefreshLayout.setOnRefreshListener(direction -> {
            swipyRefreshLayout.setRefreshing(false);
        });
    }

    static class BorrowerInfoItemAdpter extends ExtentBaseAdpter<HttpRequest.BorrowerInfo> {

        protected BorrowerInfoItemAdpter(Activity activity, LoadData<HttpRequest.BorrowerInfo> callback) {
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
            HttpRequest.BorrowerInfo info = (HttpRequest.BorrowerInfo) this.getItem(i);

            TextView item_name = view.findViewById(R.id.item_name);
            TextView item_status = view.findViewById(R.id.item_status);
            TextView item_local = view.findViewById(R.id.localitem);

            item_status.setText("");
            item_name.setText("ID:" + info.id + "  姓名: " + info.name);
            item_local.setText("電話:" + info.phone_number);

            return view;
        }
    }

    class BorrowerInfoItemAdpterII extends TestExtendBaseadpter {

        protected BorrowerInfoItemAdpterII(Context context, ArrayList extentarray) {
            super(context, extentarray);
        }

        @Override
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

