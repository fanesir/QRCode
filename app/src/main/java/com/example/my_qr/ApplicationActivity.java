package com.example.my_qr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class ApplicationActivity extends AppCompatActivity {
    EditText username_view, account_view, password_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);

        username_view = findViewById(R.id.username);
        account_view = findViewById(R.id.userccount);
        password_view = findViewById(R.id.password);
    }


    public void signIn(View v) {
        String username = username_view.getText().toString();
        String account = account_view.getText().toString();
        String password = password_view.getText().toString();

        if (username.equals("") || account.equals("") || password.equals("")) {
            Toast.makeText(ApplicationActivity.this, "欄位不可空白", Toast.LENGTH_SHORT).show();
            return;
        }
        if (account.length() > 30) {
            Toast.makeText(ApplicationActivity.this, "帳號最多30字", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                HttpRequest.getInstance().SignUp(username, account, password);//傳送給http一組帳密
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.sing_up_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoingMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } catch (HttpRequest.SignUpError e) {//SignUp 丟出來的錯誤訊息
                runOnUiThread(() -> Toast.makeText(this, R.string.sign_up_failed, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}