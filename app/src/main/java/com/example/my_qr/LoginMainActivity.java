package com.example.my_qr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import androidx.appcompat.app.AppCompatActivity;


public class LoginMainActivity extends AppCompatActivity {
    EditText login_accout, login_password;

    final String COOKIE_FILE_NAME = "accfile";//不會動的大寫加底線


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);
        login_accout = findViewById(R.id.login_accout);
        login_password = findViewById(R.id.login_password);
        try {
            FileInputStream fis = this.openFileInput(COOKIE_FILE_NAME);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                // { "account": "1", "password": "1" }
                String account = (String) jsonObject.get("account");
                String password = (String) jsonObject.get("password");
                login_accout.setText(account);
                login_password.setText(password);
                this.login(login_accout);
                // success
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    public void login(View v) {//按下登入
        new Thread(() -> {//http結合
            try {
                HttpRequest.getInstance().Login(
                        login_accout.getText().toString(),
                        login_password.getText().toString()
                );
                // save
                runOnUiThread(() -> {
                    try (FileOutputStream fos = this.openFileOutput(COOKIE_FILE_NAME, Context.MODE_PRIVATE)) {
                        JSONObject accountInfo = new JSONObject();
                        accountInfo.put("account", login_accout.getText().toString());
                        accountInfo.put("password", login_password.getText().toString());
                        Log.d("hello", accountInfo.toString());
                        fos.write(accountInfo.toString().getBytes());
                        //Toast.makeText(this,ACCANDPAS,Toast.LENGTH_SHORT).show();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, DataViewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                });
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } catch (HttpRequest.LoginError e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginMainActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();

    }

    public void appli(View v) {//申請  當按鈕被觸發 callback 方法啟動
        Intent intent = new Intent(this, ApplicationActivity.class);
        startActivity(intent);
    }


}