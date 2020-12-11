package com.example.my_qr;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class AddThisAccountWantBorrowerctivity extends AppCompatActivity {
    HttpRequest.BorrowerInfo getBorrowerAccountInfo;
    EditText editTextEndTime, endTimeDateEdtext, noteEdtext, BrrowIDEdtext;
    Calendar c;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    String forJsonEndTime, sDate, sTime;
    TimePicker tp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_this_account_want_borrowerctivity);
        Intent intent = AddThisAccountWantBorrowerctivity.this.getIntent();
        getBorrowerAccountInfo = (HttpRequest.BorrowerInfo) intent.getSerializableExtra("CreateThisAccountWantBorrowerdata");
        TextView tixtviewTitle = findViewById(R.id.tixtviewTitle);
        tixtviewTitle.setText("ID:" + getBorrowerAccountInfo.id + "新增" + getBorrowerAccountInfo.name + "要借出的財產");
        TextView nowtimetextview = findViewById(R.id.nowtimetextview);
        nowtimetextview.setText("" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.TAIWAN).format(new Date()));


        noteEdtext = findViewById(R.id.noteEdtext);
        BrrowIDEdtext = findViewById(R.id.BrrowIDEdtext);

        endTimeDateEdtext = findViewById(R.id.editTextEndDate);
        editTextEndTime = findViewById(R.id.endTimeTimeEdtext);
        c = Calendar.getInstance();
        //日期
        endTimeDateEdtext.setOnClickListener(new View.OnClickListener() {
            int day = c.get(Calendar.DAY_OF_MONTH);
            int moth = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);

            @Override
            public void onClick(View view) {
                datePickerDialog = new DatePickerDialog(AddThisAccountWantBorrowerctivity.this, (datePicker, mYear, mMonth, mDay) -> {

                    if (mMonth + 1 < 9) {
                        sDate = "" + mYear + "-" + "0" + (mMonth + 1) + "-" + mDay;
                    } else {
                        sDate = "" + mYear + "-" + (mMonth + 1) + "-" + mDay;
                    }


                    Log.i("Date", mDay + "/" + (mMonth + 1) + "/" + mYear);
                    endTimeDateEdtext.setText(sDate);

                }, day, moth, year);
                datePickerDialog.show();
            }
        });

        editTextEndTime.setOnClickListener(new View.OnClickListener() {
            int hour = c.get(Calendar.DAY_OF_MONTH);
            int minute = c.get(Calendar.MONTH);

            @Override
            public void onClick(View view) {
                timePickerDialog = new TimePickerDialog(AddThisAccountWantBorrowerctivity.this, (timePicker, hour, minute) -> {
                    Log.i("Time", "" + hour + ":" + minute);
                    sTime = "" + hour + ":" + minute;
                    editTextEndTime.setText("" + sTime);
                }, hour, minute, true);
                timePickerDialog.show();
            }
        });

    }

    public void PutThisAccountItem(View v) {
        forJsonEndTime = sDate + "T" + sTime + ":27.322Z";
        Log.i("xxx2", forJsonEndTime);

        String forJsonDateTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        Log.i("xxx1", forJsonDateTime);

        int borrower_id = getBorrowerAccountInfo.id;
        String Note = noteEdtext.getText().toString();
        String BrrowIDStr = BrrowIDEdtext.getText().toString();
        int BrrowItemID = Integer.parseInt(BrrowIDStr);

        new Thread(() -> {
            try {
                HttpRequest.getInstance().CreateThisAccountBorrowerItem(borrower_id, forJsonDateTime, forJsonEndTime, Note, BrrowItemID);
                runOnUiThread(() -> {
                    Toast.makeText(AddThisAccountWantBorrowerctivity.this, R.string.sign_up_success, Toast.LENGTH_SHORT).show();
                });
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } catch (HttpRequest.SignUpError signUpError) {
                signUpError.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(AddThisAccountWantBorrowerctivity.this, "fail", Toast.LENGTH_LONG).show();
                });
            }
        }).start();

    }


}
