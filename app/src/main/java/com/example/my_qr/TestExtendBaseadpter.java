package com.example.my_qr;

import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;


public abstract class TestExtendBaseadpter<T> extends BaseAdapter implements Serializable {
    LayoutInflater layoutInflater;
    Context context;
    ClipData.Item items;
    ArrayList extentarray;


    protected TestExtendBaseadpter(Context context, ArrayList extentarray) {//, ArrayList<ClipData.Item> items
        this.context = context;
        this.extentarray = extentarray;
    }

    protected TestExtendBaseadpter(int context, int extentarraym, int aa) {//, ArrayList<ClipData.Item> items
        Log.i("info", context + extentarraym + aa + " ****");
    }

    @Override
    public int getCount() {
        return extentarray.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }


    @Override
    public long getItemId(int i) {
        return i;
    }


}


