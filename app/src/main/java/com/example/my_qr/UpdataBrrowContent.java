package com.example.my_qr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class UpdataBrrowContent extends AppCompatActivity {
    EditText edupdname, edupdphone;
    HttpRequest.BorrowerInfo borrowerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_brrow_content);
        edupdname = findViewById(R.id.updname);
        edupdphone = findViewById(R.id.updphone);
        Intent intent = UpdataBrrowContent.this.getIntent();

        borrowerInfo = (HttpRequest.BorrowerInfo) intent.getSerializableExtra("Brrow");

        edupdname.setText("" + borrowerInfo.name);
        edupdphone.setText("" + borrowerInfo.phone_number);


    }

    public void putbrdata(View v) {
        String name = edupdname.getText().toString();
        String phone = edupdphone.getText().toString();

        int id = borrowerInfo.id;
        new Thread(() -> {
            try {
                HttpRequest.getInstance().UpdateBorrower(name, phone, id);
                runOnUiThread(() -> Toast.makeText(UpdataBrrowContent.this, "更新借出人成功", Toast.LENGTH_LONG).show());
                Intent intent = new Intent(UpdataBrrowContent.this, DataViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.UpdateDataError updateDataError) {
                updateDataError.printStackTrace();
            }
        }).start();

    }
}