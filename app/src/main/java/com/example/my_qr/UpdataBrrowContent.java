package com.example.my_qr;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class UpdataBrrowContent extends AppCompatActivity {
    TextView brrownameid, brrowitedid;
    CardView cardView, cardViewbrrowitem;
    HttpRequest.BorrowerInfo borrowerInfo;
    AlertDialog alertDialog;
    AlertDialog.Builder alertdialog;
    HttpRequest.ItemInfo getrecord_item_id;
    String brrowname,brrowphone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_brrow_content);

        brrownameid = findViewById(R.id.brrownameid);
        brrowitedid = findViewById(R.id.brrowitedid);
        cardViewbrrowitem = findViewById(R.id.cardvidwednamebrrowitem);
        cardView = findViewById(R.id.cardvidwedname);

        Intent intent = UpdataBrrowContent.this.getIntent();

        getrecord_item_id = (HttpRequest.ItemInfo) intent.getSerializableExtra("BrrowRecord_item_id");
        borrowerInfo = (HttpRequest.BorrowerInfo) intent.getSerializableExtra("BrrowInfo");
        brrowname = borrowerInfo.name;
        brrowphone=borrowerInfo.phone_number;

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

        brrownameid.setText(borrowerInfo.name + "");


        cardView.setOnClickListener(view -> {
            alertdialog = new AlertDialog.Builder(UpdataBrrowContent.this);
            LayoutInflater inflater = getLayoutInflater();//create layout
            View dialogView = inflater.inflate(R.layout.brrow_change_data_layout, null);//input for View
            alertdialog.setView(dialogView);

            EditText editTextname = (EditText) dialogView.findViewById(R.id.edaccountname);
            EditText edaccountphone = (EditText) dialogView.findViewById(R.id.edaccountphone);
            Button buttonchangeaccount = (Button) dialogView.findViewById(R.id.buttonchangeaccount);

            editTextname.setText(brrowname+ "");
            edaccountphone.setText(brrowphone + "");

            alertDialog = alertdialog.create();
            alertDialog.show();

            buttonchangeaccount.setOnClickListener(view1 -> {
                String name = editTextname.getText().toString();
                String phone = edaccountphone.getText().toString();
                int id = borrowerInfo.id;

                if (name.equals("")){
                    editTextname.setError("姓名不可空白");
                    return;
                }else if (phone.equals("")) {
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

                brrowname=name;
                brrowphone=phone;

                brrownameid.setText(name + "");
                alertDialog.cancel();
            });

        });

        cardViewbrrowitem.setOnClickListener(view -> {

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