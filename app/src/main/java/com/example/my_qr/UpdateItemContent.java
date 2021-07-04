package com.example.my_qr;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class UpdateItemContent extends AppCompatActivity {

    // HttpRequest.ItemInfo item_info;//型別 變數
    String item_info;
    CheckBox correct, discard, fixing, unlabel;
    HttpRequest request = HttpRequest.getInstance();
    AlertDialog alertDialog;
    String itemlocation, itemnote;
    static int fromdataview_int;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_content);

        TextView itemitemrextview = findViewById(R.id.itemitemrextview);//item
        CardView cardView = findViewById(R.id.iteninfocardview);
        correct = findViewById(R.id.correct);
        discard = findViewById(R.id.discard);
        fixing = findViewById(R.id.fixing);
        unlabel = findViewById(R.id.unlabel);

        Intent intent = UpdateItemContent.this.getIntent();
        item_info = intent.getStringExtra("item_info");


        new Thread(() -> {
            try {
                runOnUiThread(new Runnable() {
                    HttpRequest.ItemInfo info = request.GetItem(item_info + "");
                    @Override
                    public void run() {
                        assert info != null;
                        itemlocation = info.location;
                        itemnote = info.note;
                        correct.setChecked(info.correct);
                        discard.setChecked(info.discard);
                        fixing.setChecked(info.fixing);
                        unlabel.setChecked(info.unlabel);
                        itemitemrextview.setText("物品名稱:   " + info.name + "\n" + "存放地點:   " + info.location + "\n" +
                                "物品ID:       " + info.item_id + "\n" + "物品備註:   " + info.note);

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.GetDataError getDataError) {
                getDataError.printStackTrace();
            }
        }).start();

        cardView.setOnClickListener(view -> {
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(UpdateItemContent.this);
            LayoutInflater inflater = getLayoutInflater();//create layout
            View dialogView = inflater.inflate(R.layout.iteminfo_change_data_layout, null);//input for View
            alertdialog.setView(dialogView);

            EditText editemlocal = (EditText) dialogView.findViewById(R.id.editemlocal);
            EditText editemnote = (EditText) dialogView.findViewById(R.id.editemnote);//buttonchangeiteminfo
            Button buttonchangeiteminfo = (Button) dialogView.findViewById(R.id.buttonchangeiteminfo);


            editemlocal.setText(itemlocation + "");
            editemnote.setText(itemnote + "");


            alertDialog = alertdialog.create();
            alertDialog.show();

            buttonchangeiteminfo.setOnClickListener(view1 -> {
                String edtextitemlocal = editemlocal.getText().toString();
                String edtextitemnote = editemnote.getText().toString();

                if (edtextitemlocal.equals("")){
                    editemlocal.setError("不可空白");
                    return;
                }else if (edtextitemnote.equals("")) {
                    editemnote.setError("不可空白");
                    return;
                }

                new Thread(() -> {
                    try {
                        HttpRequest.getInstance().UpdateItem(item_info, edtextitemlocal, edtextitemnote, null);
                        runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新成功", Toast.LENGTH_SHORT).show());
                        startActivity(getIntent());
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    } catch (HttpRequest.UpdateDataError e) {
                        runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新失敗", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            });
        });

    }


    public void updateItemState(View view) {
        new Thread(() -> {
            try {
                //runOnUiThread(() -> submitButton.setVisibility(View.VISIBLE));
                HttpRequest.ItemState state = new HttpRequest.ItemState();//會確認說哪邊有按到哪邊沒有
                state.correct = correct.isChecked();//檢查哪邊有按到
                state.fixing = fixing.isChecked();
                state.discard = discard.isChecked();
                state.unlabel = unlabel.isChecked();
                HttpRequest.getInstance().UpdateItem(item_info, null, null, state);
                runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新成功", Toast.LENGTH_SHORT).show());

                Intent intent = new Intent(UpdateItemContent.this,UpdateItemContent.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);


            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.UpdateDataError e) {
                runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新失敗", Toast.LENGTH_SHORT).show());
            }
        }).start();

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            if(fromdataview_int==11){
                DataViewActivity.upLoad(this);
                fromdataview_int=0;
            }

            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

