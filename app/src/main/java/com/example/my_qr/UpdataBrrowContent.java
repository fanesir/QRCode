package com.example.my_qr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class UpdataBrrowContent extends AppCompatActivity {
    TextView brrownameid;
    CardView cardView, cardViewbrrowitem;
    HttpRequest.BorrowerInfo borrowerInfo;
    AlertDialog alertDialog;
    AlertDialog.Builder alertdialog;
    String getrecord_item_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_brrow_content);

        cardViewbrrowitem = findViewById(R.id.cardvidwednamebrrowitem);
        brrownameid = findViewById(R.id.brrownameid);
        cardView = findViewById(R.id.cardvidwedname);
        Intent intent = UpdataBrrowContent.this.getIntent();

        getrecord_item_id = intent.getStringExtra("BrrowRecord_item_id");
        borrowerInfo = (HttpRequest.BorrowerInfo) intent.getSerializableExtra("BrrowInfo");

        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.getInstance();
                HttpRequest.ItemInfo info = request.GetItem(getrecord_item_id);
                Log.i("asd",info.name+"");

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.GetDataError getDataError) {
                getDataError.printStackTrace();
            }
        }).start();

        brrownameid.setText(borrowerInfo.name + "");


        cardView.setOnClickListener(view -> {
            alertdialog = new AlertDialog.Builder(UpdataBrrowContent.this);
            LayoutInflater inflater = getLayoutInflater();//create layout
            View dialogView = inflater.inflate(R.layout.brrow_change_data_layout, null);//input for View
            alertdialog.setView(dialogView);

            EditText editTextname = (EditText) dialogView.findViewById(R.id.edaccountname);
            EditText edaccountphone = (EditText) dialogView.findViewById(R.id.edaccountphone);
            Button buttonchangeaccount = (Button) dialogView.findViewById(R.id.buttonchangeaccount);

            editTextname.setText(borrowerInfo.name + "");
            edaccountphone.setText(borrowerInfo.phone_number + "");
            alertDialog = alertdialog.create();
            alertDialog.show();

            buttonchangeaccount.setOnClickListener(view1 -> {
                String name = editTextname.getText().toString();
                String phone = edaccountphone.getText().toString();
                int id = borrowerInfo.id;

                new Thread(() -> {
                    try {
                        HttpRequest.getInstance().UpdateBorrower(name, phone, id);
                        runOnUiThread(() -> Toast.makeText(UpdataBrrowContent.this, "更新借出人成功", Toast.LENGTH_LONG).show());
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    } catch (HttpRequest.UpdateDataError updateDataError) {
                        updateDataError.printStackTrace();
                    }
                }).start();
                brrownameid.setText(name + "");
                alertDialog.cancel();
            });


        });
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            startActivity(new Intent(UpdataBrrowContent.this, ListBorrowerActivity.class));
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}