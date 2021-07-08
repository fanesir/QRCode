package com.example.my_qr;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Short3;
import android.text.TextUtils;
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
import java.io.Serializable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class UpdataBrrowContent extends AppCompatActivity {
    TextView   borrow_dateText, reply_datxt;
    String brrowname, brrowphone;
    HttpRequest.BorrowRecord   borrowRecord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_brrow_content);

        TextView brrownameid = findViewById(R.id.brrownameid);
        TextView brrowitedid = findViewById(R.id.brrowitedid);
        CardView cardViewbrrowitem = findViewById(R.id.cardvidwednamebrrowitem);
        CardView cardView = findViewById(R.id.cardvidwedname);

        Intent intent = UpdataBrrowContent.this.getIntent();

        HttpRequest.ItemInfo getrecord_item_id = (HttpRequest.ItemInfo) intent.getSerializableExtra("BrrowRecord_item_id");
        HttpRequest.BorrowerInfo borrowerInfo = (HttpRequest.BorrowerInfo) intent.getSerializableExtra("BrrowInfo");
        borrowRecord = (HttpRequest.BorrowRecord) intent.getSerializableExtra("getBorrowerRecordInfo");

         brrowname = borrowerInfo.name;
         brrowphone = borrowerInfo.phone_number;

        borrow_dateText = findViewById(R.id.borrow_dateText);
        reply_datxt = findViewById(R.id.reply_dateText);

        showThisBrrowInfo();

        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.getInstance();
                HttpRequest.ItemInfo info = request.GetItem(getrecord_item_id.item_id);
                runOnUiThread(() -> brrowitedid.setText(info.name + ""));

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.GetDataError getDataError) {
                getDataError.printStackTrace();
            }
        }).start();

        brrownameid.setText(borrowerInfo.name + "   " + borrowRecord.id + "");


        cardView.setOnClickListener(view -> {
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(UpdataBrrowContent.this);
            LayoutInflater inflater = getLayoutInflater();//create layout
            View dialogView = inflater.inflate(R.layout.brrow_change_data_layout, null);//input for View
            alertdialog.setView(dialogView);

            EditText editTextname = (EditText) dialogView.findViewById(R.id.edaccountname);
            EditText edaccountphone = (EditText) dialogView.findViewById(R.id.edaccountphone);
            Button buttonchangeaccount = (Button) dialogView.findViewById(R.id.buttonchangeaccount);

            editTextname.setText(brrowname + "");
            edaccountphone.setText(brrowphone + "");

            AlertDialog alertDialog = alertdialog.create();
            alertDialog.show();


            buttonchangeaccount.setOnClickListener(view1 -> {
                String name = editTextname.getText().toString();
                String phone = edaccountphone.getText().toString();
                int id = borrowerInfo.id;

                if (name.equals("")) {
                    editTextname.setError("姓名不可空白");
                    return;
                } else if (phone.equals("")) {
                    edaccountphone.setError("電話不可空白");
                    return;
                }

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

                brrowname = name;
                brrowphone = phone;

                brrownameid.setText(name + "");
                alertDialog.cancel();
            });

        });
        cardViewbrrowitem.setOnClickListener(view -> {

            Intent intent1 = new Intent(UpdataBrrowContent.this, UpdateItemContent.class);
            intent1.putExtra("item_info", getrecord_item_id.item_id);//
            UpdateItemContent.fromdataview_int = 0;
            startActivity(intent1);
        });

    }

    public void showThisBrrowInfo() {
        new Thread(() -> {
            try {
                HttpRequest.BorrowRecord info = HttpRequest.getInstance().GetBorrowerRecord(borrowRecord.id);
                runOnUiThread(() -> {
                    borrow_dateText.setText(info.borrow_date.substring(0, 10) + "");
                    reply_datxt.setText(info.reply_date + "");
                });

            } catch (IOException | JSONException | HttpRequest.GetDataError e) {
                e.printStackTrace();
            }
        }).start();
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
