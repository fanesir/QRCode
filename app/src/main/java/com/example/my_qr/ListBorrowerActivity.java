package com.example.my_qr;

import android.app.Activity;
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
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class ListBorrowerActivity extends AppCompatActivity {
    ListView lvBorrowerAccount;
    SwipyRefreshLayout swipyRefreshLayout;

    Map<Integer, HttpRequest.BorrowerInfo> borrowerInfoMap = new HashMap<>();
    Map<Integer, HttpRequest.ItemInfo> itemInfoMap = new HashMap<>();


    ExtentBaseAdpter.LoadData<HttpRequest.BorrowRecord> LoadBrrowerInfo = offset -> {
        ExtentBaseAdpter.LoadState<HttpRequest.BorrowRecord> results = new ExtentBaseAdpter.LoadState<>();

        try {
            results.result = HttpRequest.getInstance().GetBorrowerRecord(30, offset);
            for (HttpRequest.BorrowRecord record : results.result) {

                if (!borrowerInfoMap.containsKey(record.borrower_id)) {
                    HttpRequest.BorrowerInfo info = HttpRequest.getInstance().GetBorrower(record.borrower_id);
                    borrowerInfoMap.put(record.borrower_id, info);//(用戶id,用戶內的如電話姓名等)

                }
                if (!itemInfoMap.containsKey(record.item_id)) {
                    HttpRequest.ItemInfo info = HttpRequest.getInstance().GetItem(record.item_id);
                    itemInfoMap.put(record.item_id, info);
                }

            }
            return results;
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


            HttpRequest.BorrowRecord getBorrowerInfo = (HttpRequest.BorrowRecord) borrowerListAdapter.getItem(i);

            HttpRequest.BorrowerInfo brrowinfo = borrowerInfoMap.get(getBorrowerInfo.borrower_id);
            HttpRequest.ItemInfo itemInfo = itemInfoMap.get(getBorrowerInfo.item_id);//給予借出者的項目ID

            Intent intent  = new Intent(ListBorrowerActivity.this,UpdataBrrowContent.class);

            intent.putExtra("BrrowInfo", brrowinfo);
            intent.putExtra("BrrowRecord_item_id",itemInfo);
            intent.putExtra("getBorrowerRecordInfo",getBorrowerInfo);

            startActivity(intent);
            finish();
        });

        lvBorrowerAccount.setOnItemLongClickListener((adapterView, view, i, l) -> {//長按編輯
            return false;
        });

        swipyRefreshLayout.setOnRefreshListener(direction -> {
            swipyRefreshLayout.setRefreshing(false);
        });
    }

    class BorrowerInfoItemAdpter extends ExtentBaseAdpter<HttpRequest.BorrowRecord> {

        protected BorrowerInfoItemAdpter(Activity activity, LoadData<HttpRequest.BorrowRecord> callback) {
            super(activity, callback);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());//初始一個背後 layout畫板
                view = inflater.inflate(R.layout.list_brrowinfo, null);
            }
            if (!done && i + 5 > this.data.size()) {
                this.loadItems();
            }
            HttpRequest.BorrowRecord info = (HttpRequest.BorrowRecord) this.getItem(i);

            TextView item_name = view.findViewById(R.id.infobrrow);
            TextView item_local = view.findViewById(R.id.namebrrow);
            TextView item_id = view.findViewById(R.id.idbrrow);

            HttpRequest.ItemInfo itemInfo = itemInfoMap.get(info.item_id);
            HttpRequest.BorrowerInfo itemInfo2 = borrowerInfoMap.get(info.borrower_id);


            item_name.setText(itemInfo.name);
            item_local.setText(itemInfo2.name + "");
            item_id.setText(info.borrow_date.substring(0, 10));


            return view;
        }
    }


}

