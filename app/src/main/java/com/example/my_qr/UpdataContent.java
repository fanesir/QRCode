package com.example.my_qr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class UpdataContent extends AppCompatActivity {
    EditText updalocat, upid, upnote;
    HttpRequest.ItemInfo item_info;//型別 變數
    TextView Titlee;
    int Backthedataview;
    Button pushbutton, updatabutton2;
    CheckBox correct, discard, fixing, unlabel;
    int getdataview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_updata_content);
        updalocat = findViewById(R.id.updalocat);//我要get
        upid = findViewById(R.id.upid);
        upnote = findViewById(R.id.upnote);
        Titlee = findViewById(R.id.Titlee);
        updatabutton2 = findViewById(R.id.pushbutton2);

        correct = findViewById(R.id.correctt);
        discard = findViewById(R.id.discardd);
        fixing = findViewById(R.id.fixingg);
        unlabel = findViewById(R.id.unlabell);
        Intent intent = UpdataContent.this.getIntent();
        item_info = (HttpRequest.ItemInfo) intent.getSerializableExtra("item_info");
        Intent intent2 = getIntent();
        Backthedataview = intent2.getIntExtra("Backthedataview", 0);

        assert item_info != null;
        correct.setChecked(item_info.correct);
        discard.setChecked(item_info.discard);
        fixing.setChecked(item_info.fixing);
        unlabel.setChecked(item_info.unlabel);

        updalocat.setText(item_info.location);

        Titlee.setText("財產編號:" + "\n" + item_info.item_id);
        upid.setText(item_info.name);
        upnote.setText(item_info.note);
    }


    public void putupdata(View v) {

        String dalocat_up = updalocat.getText().toString();
        String noteup = upnote.getText().toString();


        new Thread(() -> {
            try {
                HttpRequest.getInstance().UpdateItem(item_info.item_id, dalocat_up, noteup, null);
                updatacheckbox();

                runOnUiThread(() -> Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show());
                if (Backthedataview == 123) {
                    Intent intent = new Intent(this, DataViewActivity.class);
                    finish();
                    Backthedataview = 0;
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    finish();
                    Backthedataview = 0;
                    startActivity(intent);
                }


            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.UpdateDataError e) {
                runOnUiThread(() -> Toast.makeText(this, "更新失敗", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public void updatacheckbox() {
        //if (this.info == null) {//如果什麼都沒輸入
        //    return;
        //}
        new Thread(() -> {
            try {
                runOnUiThread(() -> updatabutton2.setVisibility(View.VISIBLE));
                HttpRequest.ItemState state = new HttpRequest.ItemState();//會確認說哪邊有按到哪邊沒有
                state.correct = correct.isChecked();//檢查哪邊有按到
                state.fixing = fixing.isChecked();
                state.discard = discard.isChecked();
                state.unlabel = unlabel.isChecked();
                HttpRequest.getInstance().UpdateItem(item_info.item_id, null, null, state);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.UpdateDataError e) {
                runOnUiThread(() -> Toast.makeText(UpdataContent.this, "更新失敗", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


}

