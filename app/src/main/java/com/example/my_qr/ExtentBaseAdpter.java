package com.example.my_qr;

import android.app.Activity;
import android.util.Log;
import android.widget.BaseAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class ExtentBaseAdpter<T> extends BaseAdapter implements Serializable {
    protected List<T> data = new ArrayList<T>();//nd data have info
    protected Activity activity;
    protected boolean done = false;
    protected int length = 0;
    private boolean lock = false;
    protected LoadData<T> callback;

    interface LoadData<T> {
        LoadState<T> load(int offset);
    }

    static class LoadState<T> {
        public List<T> result;
        public boolean has_next;
    }

    protected ExtentBaseAdpter(Activity activity, LoadData<T> callback) {
        this.activity = activity;
        this.callback = callback;//另一個類別進來的資料
        loadItems();
    }

    protected void loadItems() {
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
            LoadState<T> state = callback.load(length);
            Log.i("title", "limit" + this.data.size() + "");
            if (state == null || state.result == null) {
                done = true;
                return;
            }
            done = !state.has_next;
            length = length + state.result.size();
            if (state.result.size() == 0) {
                done = true;
                lock = false;
            } else {
                activity.runOnUiThread(() -> {
                    this.data.addAll(state.result);
                    notifyDataSetChanged();
                    lock = false;
                });
            }
        }).start();
    }

    @Override
    public int getCount() {

        return this.data.size();
    }

    @Override
    public Object getItem(int i) {
        return this.data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}


