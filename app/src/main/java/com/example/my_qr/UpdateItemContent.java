package com.example.my_qr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class UpdateItemContent extends AppCompatActivity {
    EditText location, id, note;
    HttpRequest.ItemInfo item_info;//型別 變數
    Button submitButton;
    CheckBox correct, discard, fixing, unlabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_updata_content);
        location = findViewById(R.id.location);
        id = findViewById(R.id.id);
        note = findViewById(R.id.note);
        TextView Title = findViewById(R.id.title);
        submitButton = findViewById(R.id.pushbutton2);

        correct = findViewById(R.id.correct);
        discard = findViewById(R.id.discard);
        fixing = findViewById(R.id.fixing);
        unlabel = findViewById(R.id.unlabel);
        Intent intent = UpdateItemContent.this.getIntent();
        item_info = (HttpRequest.ItemInfo) intent.getSerializableExtra("item_info");

        assert item_info != null;
        correct.setChecked(item_info.correct);
        discard.setChecked(item_info.discard);
        fixing.setChecked(item_info.fixing);
        unlabel.setChecked(item_info.unlabel);
        location.setText(item_info.location);

        Title.setText(String.format("%s\n%s", getString(R.string.item_id), item_info.item_id));
        id.setText(item_info.name);
        note.setText(item_info.note);
        Log.i("我的id", item_info.item_id);
    }

    public void submitData(View v) {
        String location = this.location.getText().toString();
        String note = this.note.getText().toString();

        new Thread(() -> {
            try {
                HttpRequest.getInstance().UpdateItem(item_info.item_id, location, note, null);
                updateItemState();
                runOnUiThread(() -> Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.UpdateDataError e) {
                runOnUiThread(() -> Toast.makeText(this, "更新失敗", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public void updateItemState() {
        new Thread(() -> {
            try {
                runOnUiThread(() -> submitButton.setVisibility(View.VISIBLE));
                HttpRequest.ItemState state = new HttpRequest.ItemState();//會確認說哪邊有按到哪邊沒有
                state.correct = correct.isChecked();//檢查哪邊有按到
                state.fixing = fixing.isChecked();
                state.discard = discard.isChecked();
                state.unlabel = unlabel.isChecked();
                HttpRequest.getInstance().UpdateItem(item_info.item_id, null, null, state);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.UpdateDataError e) {
                runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新失敗", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


}

