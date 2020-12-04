package com.example.my_qr;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class NewBrrowActivity extends AppCompatActivity {
    EditText brrowname, brrownumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_brrow);
        brrowname = findViewById(R.id.edbrname);
        brrownumber = findViewById(R.id.edbrnumber);
    }

    public void updatabrrow(View view) {
        String brname;
        String brnumber;

        brname = brrowname.getText().toString();
        brnumber = brrownumber.getText().toString();
        if (brname.equals("") || brnumber.equals("")) {
            Toast.makeText(NewBrrowActivity.this, "不可空白", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                HttpRequest.getInstance().BrrowSignUp(brname, brnumber);
                runOnUiThread(() -> {
                    Toast.makeText(NewBrrowActivity.this, R.string.sing_up_success, Toast.LENGTH_SHORT).show();
                });
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } catch (HttpRequest.SignUpError signUpError) {
                signUpError.printStackTrace();
                Toast.makeText(NewBrrowActivity.this, "faile", Toast.LENGTH_LONG).show();
            }
        }).start();


    }
}

