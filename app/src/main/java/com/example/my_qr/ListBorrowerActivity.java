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
    ListView borrowerListView, lvShowAccountRecordItem;
    List<HttpRequest.BorrowerInfo> list_view_data = new LinkedList<>();
    HttpRequest.BorrowerInfo getBorrowerInfo;

    List<HttpRequest.BorrowRecord> borrowRecordData = new LinkedList<>();
    int accountId;

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
        borrowerListView.setAdapter(adapter);


        lvShowAccountRecordItem = findViewById(R.id.borrow_record_list_view);
        SwipeRefreshLayout swipshowbrrow = findViewById(R.id.swipshowbrrowitem);
        showThisAccountItemAdapter recordItemAdapter = new showThisAccountItemAdapter(borrowRecordData);
        lvShowAccountRecordItem.setAdapter(recordItemAdapter);


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
            accountId = getBorrowerInfo.id;
            borrowRecordData.clear();
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
                        ((BaseAdapter) borrowerListView.getAdapter()).notifyDataSetChanged();//notify....更新刷新
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


//Show出此人借閱紀錄

    Thread loadBrrowerAccountData(int limit) {
        Thread thread = new Thread(() -> {
            try {
                HttpRequest.BorrowRecord result =
                        HttpRequest.getInstance().GetBorrowerRecord(accountId);//從這裡開始 要新增一個ｈｔｔｐ　拿到的方法，並顯示出來

                runOnUiThread(() -> {
                    if (limit != 0) {
                        borrowRecordData.add(result);
                    }
                    synchronized (lvShowAccountRecordItem.getAdapter()) {
                        ((BaseAdapter) lvShowAccountRecordItem.getAdapter()).notifyDataSetChanged();
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
        List<HttpRequest.BorrowRecord> list;

        showThisAccountItemAdapter(List<HttpRequest.BorrowRecord> list) {
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
            HttpRequest.BorrowRecord info = (HttpRequest.BorrowRecord) this.getItem(i);
            TextView item_name = view.findViewById(R.id.item_name);
            TextView item_status = view.findViewById(R.id.item_status);
            TextView item_local = view.findViewById(R.id.localitem);

            item_local.setText("");
            item_name.setText("" + info.id);
            item_status.setText("" + info.borrow_date);

            return view;
        }
    }

    public void toAddThisAccountWantBorrowerItem(View view) {
        Intent intent = new Intent(ListBorrowerActivity.this, AddThisAccountWantBorrowerctivity.class);

        intent.putExtra("CreateThisAccountWantBorrowerdata", getBorrowerInfo);
        startActivity(intent);
    }

}