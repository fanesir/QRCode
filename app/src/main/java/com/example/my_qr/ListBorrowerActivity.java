package com.example.my_qr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ListBorrowerActivity extends AppCompatActivity {

    ListView lvBorrowerAccount, lvShowAccountRecordItem;
    HttpRequest.BorrowerInfo borrowerInfo;
    List<HttpRequest.BorrowerInfo> brrowAccountData = new LinkedList<>();
    List<HttpRequest.BorrowRecord> borrowAccountRecordData = new LinkedList<>();
    HttpRequest.BorrowerInfo getBorrowerInfo;
    int accountId;
    List<HttpRequest.ItemInfo> itemInfos = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_brrow);

        TextView showBrrowNameTextview = findViewById(R.id.showBrrowNameTextview);
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setVisibility(View.INVISIBLE);

        lvBorrowerAccount = findViewById(R.id.showbrrowlisst);
        SwipeRefreshLayout brswipLayout = findViewById(R.id.brswipLayout);

        lvBorrowerAccount.setAdapter(new ItemAdapter(ListBorrowerActivity.this, borrowerInfo));

        lvShowAccountRecordItem = findViewById(R.id.borrow_record_list_view);
        showThisAccountItemAdapter recordItemAdapter = new showThisAccountItemAdapter(borrowAccountRecordData);
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


        lvBorrowerAccount.setOnItemLongClickListener((adapterView, view, i, l) -> {//長按編輯
            ItemAdapter borrowerListAdapter = (ItemAdapter) adapterView.getAdapter();//getAdapter 方法
            HttpRequest.BorrowerInfo getBorrowerInfo = (HttpRequest.BorrowerInfo) borrowerListAdapter.getItem(i);
            Intent intent = new Intent(ListBorrowerActivity.this, UpdataBrrowContent.class);
            intent.putExtra("Brrow", getBorrowerInfo);

            startActivity(intent);
            return false;
        });

        lvBorrowerAccount.setOnItemClickListener((adapterView, view, i, l) -> {//短按顯示此借出人借過的物品
            ItemAdapter borrowerListAdapter = (ItemAdapter) adapterView.getAdapter();//getAdapter 方法
            getBorrowerInfo = (HttpRequest.BorrowerInfo) borrowerListAdapter.getItem(i);

            imageButton.setVisibility(View.VISIBLE);
            showBrrowNameTextview.setText("" + getBorrowerInfo.name);
            accountId = getBorrowerInfo.id;
            borrowAccountRecordData.clear();
            loadBrrowerAccountData(20);
        });

    }

    Thread loadBrData(int limit) {
        Thread thread = new Thread(() -> {
            try {
                List<HttpRequest.BorrowerInfo> result = null;
                result = HttpRequest.getInstance().GetBorrower(limit, brrowAccountData.size());
                List<HttpRequest.BorrowerInfo> finalResult = result;
                runOnUiThread(() -> {
                    if (limit != 0) {
                        brrowAccountData.addAll(finalResult);
                    }
                    synchronized (lvBorrowerAccount.getAdapter()) {
                        ((BaseAdapter) lvBorrowerAccount.getAdapter()).notifyDataSetChanged();//notify....更新刷新
                    }
                });
            } catch (IOException | JSONException | HttpRequest.GetDataError e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }


    static class ItemAdapter extends BaseAdapter {
        protected final List<HttpRequest.BorrowerInfo> list = new ArrayList<>();
        protected int loadThreshold;
        protected final int limit = 10;
        protected HttpRequest.BorrowerInfo searchState;
        protected Activity activity;
        protected boolean done = false;
        protected int length = 0;
        protected String name;


        private boolean lock = false;

        ItemAdapter(Activity activity, String name) {//search
            this(activity, 20);
            this.name = name;
        }


        ItemAdapter(Activity activity, HttpRequest.BorrowerInfo searchState) {//條件篩選
            this(activity, 20);
            this.searchState = searchState;
        }

        protected ItemAdapter(Activity activity, int loadThreshold) {
            this.loadThreshold = loadThreshold;
            this.activity = activity;
            _loadItems();
        }


        protected void markLoadFinish() {
            done = true;
        }

        protected List<HttpRequest.BorrowerInfo> loadItems(int offset) {
            try {
                List<HttpRequest.BorrowerInfo> result = null;
                //markLoadFinish();
                result = HttpRequest.getInstance().GetBorrower(limit, offset);

                Log.i("result.size()", result.size() + " this.list.size()= " + this.list.size() + "");
                return result;
            } catch (IOException | JSONException e) {//InterruptedException
                e.printStackTrace();
            } catch (HttpRequest.GetDataError getDataError) {
                markLoadFinish();
            }
            return null;
        }

        private void _loadItems() {
            if (done || lock) {
                return;
            }
            synchronized (this) {
                if (lock) {
                    return;
                } else {
                    lock = true;
                }
            }
            new Thread(() -> {
                List<HttpRequest.BorrowerInfo> result = loadItems(length);
                length = length + result.size();
                if (result.size() == 0) {
                    done = true;
                    lock = false;
                } else {
                    activity.runOnUiThread(() -> {
                        this.list.addAll(result);
                        notifyDataSetChanged();
                        lock = false;
                    });
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
            if (!done && i + this.loadThreshold > limit) {
                Log.i("title", "i: " + i + "  this.loadThreshold:" + this.loadThreshold + "" + "  i+this.loadThreshold: " + (i + this.loadThreshold) + " limit" + limit + "");
                this._loadItems();
            }
            HttpRequest.BorrowerInfo info = (HttpRequest.BorrowerInfo) this.getItem(i);

            TextView item_name = view.findViewById(R.id.item_name);
            TextView item_status = view.findViewById(R.id.item_status);
            TextView item_local = view.findViewById(R.id.localitem);

            item_status.setText("");
            item_name.setText(info.name);
            item_local.setText(info.phone_number);

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
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (limit != 0) {
                        borrowAccountRecordData.add(result);
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

    public void CreateBorrowerAccount(View v) {
        Intent intent = new Intent(ListBorrowerActivity.this, NewBorrowerActivity.class);
        startActivity(intent);
    }
}