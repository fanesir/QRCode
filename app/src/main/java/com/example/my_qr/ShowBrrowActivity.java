package com.example.my_qr;

import android.os.Bundle;
import android.util.TypedValue;
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

public class ShowBrrowActivity extends AppCompatActivity {
    String[] alan = {"1", "2"};

    ListView showbrrowlisstview;
    List<HttpRequest.BrItemInfo> list_view_data = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_brrow);
        showbrrowlisstview = findViewById(R.id.showbrrowlisst);
        SwipeRefreshLayout brswipLayout = findViewById(R.id.brswipLayout);
        BrrowItemAdpter adpter = new BrrowItemAdpter(list_view_data);


        showbrrowlisstview.setAdapter(adpter);

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

    }


    Thread loadBrData(int limit) {
        Thread thread = new Thread(() -> {
            try {
                List<HttpRequest.BrItemInfo> result = null;//裝各種物件資料
                if (limit != 0) {
                    HttpRequest.ItemState searchState = new HttpRequest.ItemState();
                    searchState.correct = false;
                    result = HttpRequest.getInstance().BrGetItem(limit, list_view_data.size());//
                }


                List<HttpRequest.BrItemInfo> finalResult = result;
                runOnUiThread(() -> {
                    if (limit != 0) {
                        list_view_data.addAll(finalResult);
                    }
                    synchronized (showbrrowlisstview.getAdapter()) {
                        ((BaseAdapter) showbrrowlisstview.getAdapter()).notifyDataSetChanged();
                    }
                });
            } catch (IOException | JSONException | HttpRequest.GetDataError e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }


    class BrrowItemAdpter extends BaseAdapter {

        private final List<HttpRequest.BrItemInfo> list;

        public BrrowItemAdpter(List<HttpRequest.BrItemInfo> lList) {
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
            HttpRequest.BrItemInfo info = (HttpRequest.BrItemInfo) this.getItem(i);

            TextView item_name = view.findViewById(R.id.item_name);
            TextView item_status = view.findViewById(R.id.item_status);
            TextView item_local = view.findViewById(R.id.localitem);

            item_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            item_status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            item_name.setText(info.brname);
            item_local.setText("");
            item_status.setText(info.brnumber);

            return view;
        }
    }


}