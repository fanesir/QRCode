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

    ExtentBaseAdpter.LoadData<HttpRequest.BorrowerInfo> LoadBrrowerInfo = new ExtentBaseAdpter.LoadData<HttpRequest.BorrowerInfo>() {
        @Override
        public ExtentBaseAdpter.LoadState<HttpRequest.BorrowerInfo> load(int offset) {
            ExtentBaseAdpter.LoadState<HttpRequest.BorrowerInfo> resuls = new ExtentBaseAdpter.LoadState<>();
            try {
                resuls.result = HttpRequest.getInstance().GetBorrower(20, offset);
                return resuls;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.GetDataError getDataError) {
                getDataError.printStackTrace();
            }

            return null;
        }
    };
    ExtentBaseAdpter.LoadData<HttpRequest.BorrowRecord> LoadBrrowerRecord = new ExtentBaseAdpter.LoadData<HttpRequest.BorrowRecord>() {
        @Override
        public ExtentBaseAdpter.LoadState<HttpRequest.BorrowRecord> load(int offset) {
            ExtentBaseAdpter.LoadState<HttpRequest.BorrowRecord> resulsRecord = new ExtentBaseAdpter.LoadState<>();
            try {
                resulsRecord.result = (List<HttpRequest.BorrowRecord>) HttpRequest.getInstance().GetBorrowerRecord(accountId);
                Log.i("ID",accountId+"");
                return resulsRecord;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.GetDataError getDataError) {
                getDataError.printStackTrace();
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_brrow);

        TextView showBrrowNameTextview = findViewById(R.id.showBrrowNameTextview);
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setVisibility(View.INVISIBLE);
        SwipeRefreshLayout brswipLayout = findViewById(R.id.brswipLayout);

        lvBorrowerAccount = findViewById(R.id.showbrrowlisst);
        lvBorrowerAccount.setAdapter(new BorrowerInfoItemAdpter(this, LoadBrrowerInfo));

        lvShowAccountRecordItem = findViewById(R.id.borrow_record_list_view);
        showThisAccountItemAdapter recordItemAdapter = new showThisAccountItemAdapter(borrowAccountRecordData);
        lvShowAccountRecordItem.setAdapter(recordItemAdapter);

        lvBorrowerAccount.setOnItemLongClickListener((adapterView, view, i, l) -> {//長按編輯
            BorrowerInfoItemAdpter borrowerListAdapter = (BorrowerInfoItemAdpter) adapterView.getAdapter();//getAdapter 方法
            HttpRequest.BorrowerInfo getBorrowerInfo = (HttpRequest.BorrowerInfo) borrowerListAdapter.getItem(i);
            Intent intent = new Intent(ListBorrowerActivity.this, UpdataBrrowContent.class);
            intent.putExtra("Brrow", getBorrowerInfo);

            startActivity(intent);
            return false;
        });

        lvBorrowerAccount.setOnItemClickListener((adapterView, view, i, l) -> {//短按顯示此借出人借過的物品
            BorrowerInfoItemAdpter borrowerListAdapter = (BorrowerInfoItemAdpter) adapterView.getAdapter();//getAdapter 方法
            getBorrowerInfo = (HttpRequest.BorrowerInfo) borrowerListAdapter.getItem(i);

            imageButton.setVisibility(View.VISIBLE);
            showBrrowNameTextview.setText("" + getBorrowerInfo.name);
            accountId = getBorrowerInfo.id;

            borrowAccountRecordData.clear();
            loadBrrowerAccountData(20);
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