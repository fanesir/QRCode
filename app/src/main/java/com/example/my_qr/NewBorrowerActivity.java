package com.example.my_qr;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class NewBorrowerActivity extends AppCompatActivity {
    EditText borrowerName, borrowerPhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_brrow);
        borrowerName = findViewById(R.id.edbrname);
        borrowerPhoneNumber = findViewById(R.id.edbrnumber);
    }

    public void createBorrower(View view) {
        String name = borrowerName.getText().toString();
        String phoneNumber = borrowerPhoneNumber.getText().toString();


        if (name.equals("") || phoneNumber.equals("")) {
            Toast.makeText(NewBorrowerActivity.this, "不可空白", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                HttpRequest.getInstance().CreateBorrower(name, phoneNumber);
                runOnUiThread(() -> {
                    Toast.makeText(NewBorrowerActivity.this, R.string.sign_up_success, Toast.LENGTH_SHORT).show();
                });
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } catch (HttpRequest.SignUpError signUpError) {
                signUpError.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(NewBorrowerActivity.this, "fail", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}

