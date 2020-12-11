package com.example.my_qr;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ListBorrowerActivity extends AppCompatActivity {
    ListView borrowerListView, lvshowaccoutitem;
    List<HttpRequest.BorrowerInfo> list_view_data = new LinkedList<>();
    List<HttpRequest.BorrowerInfo> lis = new LinkedList<>();
    HttpRequest.BorrowerInfo getBorrowerInfo;
    int Accountid;
    String Accountname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_brrow);

        TextView showBrrowNameTextview = findViewById(R.id.showBrrowNameTextview);
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setVisibility(View.INVISIBLE);

        borrowerListView = findViewById(R.id.showbrrowlisst);
        SwipeRefreshLayout brswipLayout = findViewById(R.id.brswipLayout);
        BorrowerListAdapter adapter = new BorrowerListAdapter(list_view_data);

        lvshowaccoutitem = findViewById(R.id.lvshowaccoutitem);
        SwipeRefreshLayout swipshowbrrow = findViewById(R.id.swipshowbrrow);
        showThisAccountItemAdapter AccountItemAdapter = new showThisAccountItemAdapter(lis);

        borrowerListView.setAdapter(adapter);
        lvshowaccoutitem.setAdapter(AccountItemAdapter);

        loadBrData(20);

        brswipLayout.setOnRefreshListener(() -> {
            Thread thread = loadBrData(5);
            new Thread(() -> {
                try {
                    thread.join();//從中介入
                } catch (InterruptedException ignored) {
                } finally {
                    runOnUiThread(() -> brswipLayout.setRefreshing(false));
                }
            }).start();
        });

        borrowerListView.setOnItemLongClickListener((adapterView, view, i, l) -> {//長按編輯
            BorrowerListAdapter borrowerListAdapter = (BorrowerListAdapter) adapterView.getAdapter();//getAdapter 方法
            HttpRequest.BorrowerInfo getBorrowerInfo = (HttpRequest.BorrowerInfo) borrowerListAdapter.getItem(i);
            Intent intent = new Intent(ListBorrowerActivity.this, UpdataBrrowContent.class);
            intent.putExtra("Brrow", getBorrowerInfo);

            startActivity(intent);
            return false;
        });

        borrowerListView.setOnItemClickListener((adapterView, view, i, l) -> {//短按顯示此借出人借過的物品
            BorrowerListAdapter borrowerListAdapter = (BorrowerListAdapter) adapterView.getAdapter();//getAdapter 方法
            getBorrowerInfo = (HttpRequest.BorrowerInfo) borrowerListAdapter.getItem(i);

            imageButton.setVisibility(View.VISIBLE);
            showBrrowNameTextview.setText("" + getBorrowerInfo.name);
            Accountid = getBorrowerInfo.id;
            Accountname = getBorrowerInfo.name;

            loadBrrowerAccountData(20);
        });

    }

    Thread loadBrData(int limit) {
        Thread thread = new Thread(() -> {
            try {
                List<HttpRequest.BorrowerInfo> result = null;
                result = HttpRequest.getInstance().GetBorrower(limit, list_view_data.size());
                List<HttpRequest.BorrowerInfo> finalResult = result;
                runOnUiThread(() -> {
                    if (limit != 0) {
                        list_view_data.addAll(finalResult);
                    }
                    synchronized (borrowerListView.getAdapter()) {
                        ((BaseAdapter) borrowerListView.getAdapter()).notifyDataSetChanged();
                    }
                });
            } catch (IOException | JSONException | HttpRequest.GetDataError e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }


    static class BorrowerListAdapter extends BaseAdapter {
        private final List<HttpRequest.BorrowerInfo> list;

        public BorrowerListAdapter(List<HttpRequest.BorrowerInfo> lList) {
            this.list = lList;
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
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                view = layoutInflater.inflate(R.layout.list_item, null);
            }
            HttpRequest.BorrowerInfo info = (HttpRequest.BorrowerInfo) this.getItem(i);

            TextView item_name = view.findViewById(R.id.item_name);
            TextView item_status = view.findViewById(R.id.item_status);
            TextView item_local = view.findViewById(R.id.localitem);

            item_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            item_status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            item_name.setText(info.name);
            item_local.setText("");
            item_status.setText(info.phone_number);

            return view;
        }
    }

    Thread loadBrrowerAccountData(int limit) {
        Thread thread = new Thread(() -> {
            try {
                List<HttpRequest.BorrowerInfo> result = null;
                result = HttpRequest.getInstance().GetBorrowerRecord(Accountid, Accountname, limit, lis.size());//從這裡開始 要新增一個ｈｔｔｐ　拿到的方法，並顯示出來
                List<HttpRequest.BorrowerInfo> finalResult = result;
                runOnUiThread(() -> {
                    if (limit != 0) {
                        list_view_data.addAll(finalResult);
                    }
                    synchronized (borrowerListView.getAdapter()) {
                        ((BaseAdapter) borrowerListView.getAdapter()).notifyDataSetChanged();
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
        Intent intent = new Intent(ListBorrowerActivity.this, AddThisAccountWantBorrowerctivity.class);

        intent.putExtra("CreateThisAccountWantBorrowerdata", getBorrowerInfo);
        startActivity(intent);
    }

}