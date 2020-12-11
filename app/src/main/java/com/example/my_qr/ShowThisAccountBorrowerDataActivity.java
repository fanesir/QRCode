package com.example.my_qr;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ShowThisAccountBorrowerDataActivity extends AppCompatActivity {
    HttpRequest.BorrowerInfo borrowerInfo;
    List<HttpRequest.BorrowerInfo> lis = new LinkedList<>();
    ListView accountListView;
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_borrow_account_data);
        accountListView = findViewById(R.id.lvshowaccout);
        swipeRefreshLayout = findViewById(R.id.swipshowbrrow);

        Intent intent = ShowThisAccountBorrowerDataActivity.this.getIntent();
        borrowerInfo = (HttpRequest.BorrowerInfo) intent.getSerializableExtra("account_item_info");
        showThisAccountItemAdapter showThisAccountItemAdapter = new showThisAccountItemAdapter(lis);
        accountListView.setAdapter(showThisAccountItemAdapter);


        loadBrData(20);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Thread thread = loadBrData(5);
            new Thread(() -> {
                try {
                    thread.join();//從中介入
                } catch (InterruptedException ignored) {
                } finally {
                    runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
                }
            }).start();
        });


    }

    Thread loadBrData(int limit) {
        Thread thread = new Thread(() -> {
            try {
                List<HttpRequest.BorrowerInfo> result = null;//裝各種物件資料
                if (limit != 0) {
                    HttpRequest.ItemState searchState = new HttpRequest.ItemState();
                    int id = borrowerInfo.id;
                    String name = borrowerInfo.name;
                    result = HttpRequest.getInstance().GetBorrowerRecord(id, name, limit, lis.size());//
                }

                List<HttpRequest.BorrowerInfo> finalResult = result;
                runOnUiThread(() -> {
                    if (limit != 0) {
                        lis.addAll(finalResult);
                    }
                    synchronized (accountListView.getAdapter()) {
                        ((BaseAdapter) accountListView.getAdapter()).notifyDataSetChanged();
                    }
                });
            } catch (IOException | JSONException | HttpRequest.GetDataError e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }


    class showThisAccountItemAdapter extends BaseAdapter {
        List<HttpRequest.BorrowerInfo> list;

        showThisAccountItemAdapter(List<HttpRequest.BorrowerInfo> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                view = layoutInflater.inflate(R.layout.list_item, null);
            }
            HttpRequest.BorrowerInfo info = (HttpRequest.BorrowerInfo) this.getItem(i);
            TextView item_name = view.findViewById(R.id.item_name);
            TextView item_status = view.findViewById(R.id.item_status);
            TextView item_local = view.findViewById(R.id.localitem);

            item_local.setText("");
            item_name.setText("項目名稱");
            item_status.setText("借出日期");
            return view;
        }
    }

    public void toAddThisAccountWantBorrowerItem(View view) {
        Intent intent = new Intent(ShowThisAccountBorrowerDataActivity.this, AddThisAccountWantBorrowerctivity.class);
        startActivity(intent);
    }


}