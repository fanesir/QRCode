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

public class UpdateItemContent extends AppCompatActivity {
    EditText location, id, note;
    // HttpRequest.ItemInfo item_info;//型別 變數
    Button submitButton;
    String item_info;
    CheckBox correct, discard, fixing, unlabel;
    HttpRequest request = HttpRequest.getInstance();


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
        item_info = intent.getStringExtra("item_info");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        HttpRequest.ItemInfo info = request.GetItem(item_info + "");

                        @Override
                        public void run() {
                            assert info != null;
                            correct.setChecked(info.correct);
                            discard.setChecked(info.discard);
                            fixing.setChecked(info.fixing);
                            unlabel.setChecked(info.unlabel);
                            location.setText(info.location);
                            Title.setText(String.format("%s\n%s", getString(R.string.item_id), item_info));
                            id.setText(info.name);
                            note.setText(info.note);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (HttpRequest.GetDataError getDataError) {
                    getDataError.printStackTrace();
                }
            }
        }).start();

    }

    public void submitData(View v) {
        String location = this.location.getText().toString();
        String note = this.note.getText().toString();

        new Thread(() -> {
            try {
                HttpRequest.getInstance().UpdateItem(item_info, location, note, null);
                updateItemState();
                runOnUiThread(() -> Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show());
                startActivity(new Intent(this, DataViewActivity.class));
//                finish();
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
                HttpRequest.getInstance().UpdateItem(item_info, null, null, state);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.UpdateDataError e) {
                runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新失敗", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


}

