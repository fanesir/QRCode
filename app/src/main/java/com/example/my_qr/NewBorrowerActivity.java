package com.example.my_qr;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.classichu.lineseditview.LinesEditView;

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

public class NewBorrowerActivity extends AppCompatActivity {
    EditText borrowerName, borrowerPhoneNumber;
    Button brrowtimebtn, brrowreturnbtn;
    LinesEditView noteEditView;
    TimePickerDialog timePickerDialog;
    Calendar calendar;
    String sDate, sTime, rDate, rTime, brrowdatatojason, returndatatojason, getItemID, getItemName;
    TextView getBrrowItemInfo;
    Spinner spinner;
    Integer getItemJsonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_brrow);
        borrowerName = findViewById(R.id.edbrname);
        borrowerPhoneNumber = findViewById(R.id.edbrnumber);
        brrowtimebtn = findViewById(R.id.brrowtimebtn);
        brrowreturnbtn = findViewById(R.id.returnbtn);
        noteEditView = findViewById(R.id.linesEditView);
        getBrrowItemInfo = findViewById(R.id.textView11);
        spinner = findViewById(R.id.spinner);
        calendar = Calendar.getInstance();

        Intent intent = getIntent();
        getItemID = intent.getStringExtra("ItemID");
        getItemName = intent.getStringExtra("ItemName");
        getItemJsonId = getIntent().getExtras().getInt("getItemJsonId");

        getBrrowItemInfo.setText("財產ID:" + getItemID + "\n" + "財產名稱:" + getItemName + "\n" + "資料庫項目ID:" + getItemJsonId);


        brrowtimebtn.setOnClickListener(new View.OnClickListener() {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int moth = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            int hour = calendar.get(Calendar.DAY_OF_MONTH);
            int minute = calendar.get(Calendar.MONTH);

            @Override
            public void onClick(View view) {

                timePickerDialog = new TimePickerDialog(NewBorrowerActivity.this, (timePicker, hour, minute) -> {

                    sTime = "" + hourconvertDate(hour) + ":" + minuteconvertDate(minute);
                    runButton(true);

                }, hour, minute, true);
                timePickerDialog.show();

                new DatePickerDialog(NewBorrowerActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        brrowdatatojason = year + "-" + hourconvertDate(month + 1) + "-" + hourconvertDate(day);
                        sDate = year + "年" + (month + 1) + "月" + day + "號";
                        Log.i("stime", sDate);
                    }
                }, year, moth, day).show();
            }

        });


        brrowreturnbtn.setOnClickListener(new View.OnClickListener() {//歸還
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int moth = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            int hour = calendar.get(Calendar.DAY_OF_MONTH);
            int minute = calendar.get(Calendar.MONTH);

            @Override
            public void onClick(View view) {

                timePickerDialog = new TimePickerDialog(NewBorrowerActivity.this, (timePicker, hour, minute) -> {

                    rTime = "" + hourconvertDate(hour) + ":" + minuteconvertDate(minute);
                    runButton(false);
                }, hour, minute, true);
                timePickerDialog.show();

                new DatePickerDialog(NewBorrowerActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        rDate = year + "年" + (month + 1) + "月" + day + "號";
                        returndatatojason = year + "-" + hourconvertDate(month + 1) + "-" + hourconvertDate(day);

                    }
                }, year, moth, day).show();


            }

        });


    }



    public String hourconvertDate(int input) {
        if (input >= 10) {
            return String.valueOf(input);
        } else {
            return "0" + input;
        }
    }

    public String minuteconvertDate(int input) {
        if (input >= 10) {
            return String.valueOf(input);
        } else {
            return "0" + input;
        }
    }

    public void runButton(Boolean choose) {
        if (choose == true) {//借出按鈕
            brrowtimebtn.setText(sDate + "  " + sTime);
            return;
        } else if (choose == false) {//歸還時間
            brrowreturnbtn.setText(rDate + "  " + rTime);
            return;
        }

    }


    public void createBorrower(View view) {
        String name = borrowerName.getText().toString();
        String phoneNumber = borrowerPhoneNumber.getText().toString();
        String brrowtime = brrowdatatojason + "T" + sTime + ":54.236129179+08:00";
        String returntime = returndatatojason + "T" + rTime + ":54.236129179Z";

        String note = noteEditView.getContentText();


        if (name.equals("") || phoneNumber.equals("") || sDate.equals("") || rDate.equals("") || getItemID == "" || getItemName == "") {
            Toast.makeText(NewBorrowerActivity.this, "有欄位空白", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                HttpRequest.getInstance().CreateBorrower(name, phoneNumber);
                HttpRequest.BorrowerInfo info = HttpRequest.getInstance().CreateBorrower(name, phoneNumber);
                Log.i("此借出人的ID", info.id + "");
                HttpRequest.getInstance().CreateThisAccountBorrowerItem(info.id, brrowtime, returntime, note + "借出商品", getItemJsonId);

                runOnUiThread(() -> {
                    Toast.makeText(NewBorrowerActivity.this, R.string.sign_up_success, Toast.LENGTH_SHORT).show();
                    finish();

                    Intent intent = new Intent(NewBorrowerActivity.this, DataViewActivity.class);
                    startActivity(intent);
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

    public void onResume() {
        super.onResume();
    }


}
