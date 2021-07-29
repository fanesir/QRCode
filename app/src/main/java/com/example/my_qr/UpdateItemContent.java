package com.example.my_qr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class UpdateItemContent extends AppCompatActivity {

    // HttpRequest.ItemInfo item_info;//型別 變數
    String item_info;
    CheckBox correct, discard, fixing, unlabel;
    HttpRequest request = HttpRequest.getInstance();
    AlertDialog alertDialog;
    String itemlocation, itemnote;
    static int fromdataview_int;

    Map<Integer, HttpRequest.BorrowerInfo> borrowerInfoMap = new HashMap<>();
    Map<Integer, HttpRequest.ItemInfo> itemInfoMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_content);

        TextView itemitemrextview = findViewById(R.id.itemitemrextview);//item
        CardView cardView = findViewById(R.id.iteninfocardview);
        ListView showbrrowitemlv = findViewById(R.id.showbrrowitemlv);

        correct = findViewById(R.id.correct);
        discard = findViewById(R.id.discard);
        fixing = findViewById(R.id.fixing);
        unlabel = findViewById(R.id.unlabel);

        Intent intent = UpdateItemContent.this.getIntent();
        item_info = intent.getStringExtra("item_info");

        new Thread(() -> {
            try {
                HttpRequest.ItemInfo info = request.GetItem(item_info);
                runOnUiThread(() -> {
                    assert info != null;
                    itemlocation = info.location;
                    itemnote = info.note;
                    correct.setChecked(info.correct);
                    discard.setChecked(info.discard);
                    fixing.setChecked(info.fixing);
                    unlabel.setChecked(info.unlabel);
                    itemitemrextview.setText("物品名稱:   " + info.name + "\n" + "存放地點:   " + info.location + "\n" +
                            "物品ID:       " + info.item_id + "\n" + "物品備註:   " + info.note);
                    ExtentBaseAdpter.LoadData<HttpRequest.BorrowRecord> LoadItemInfo = offset -> {
                        try {
                            ExtentBaseAdpter.LoadState<HttpRequest.BorrowRecord> state = new ExtentBaseAdpter.LoadState<>();

                            state.result = HttpRequest.getInstance().ItemidListview(20, offset, info.id);
                            state.has_next = true;
                            for (HttpRequest.BorrowRecord record : state.result) {

                                if (!borrowerInfoMap.containsKey(record.borrower_id)) {
                                    HttpRequest.BorrowerInfo result = HttpRequest.getInstance().GetBorrower(record.borrower_id);
                                    borrowerInfoMap.put(record.borrower_id, result);//(用戶id,用戶內的如電話姓名等)
                                }

                                if (!itemInfoMap.containsKey(record.item_id)) {
                                    HttpRequest.ItemInfo result = HttpRequest.getInstance().GetItem(record.item_id);
                                    itemInfoMap.put(record.item_id, result);
                                }


                            }
                            return state;
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        } catch (HttpRequest.GetDataError getDataError) {
                            getDataError.printStackTrace();
                        }

                        return null;
                    };
                    showbrrowitemlv.setAdapter(new ItemInfoItemAdpter(this, LoadItemInfo));//(this, LoadBrrowerInfo)
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.GetDataError getDataError) {
                getDataError.printStackTrace();
            }
        }).start();

        cardView.setOnClickListener(view -> {
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(UpdateItemContent.this);
            LayoutInflater inflater = getLayoutInflater();//create layout
            View dialogView = inflater.inflate(R.layout.iteminfo_change_data_layout, null);//input for View
            alertdialog.setView(dialogView);

            EditText editemlocal = (EditText) dialogView.findViewById(R.id.editemlocal);
            EditText editemnote = (EditText) dialogView.findViewById(R.id.editemnote);//buttonchangeiteminfo
            Button buttonchangeiteminfo = (Button) dialogView.findViewById(R.id.buttonchangeiteminfo);


            editemlocal.setText(itemlocation + "");
            editemnote.setText(itemnote + "");


            alertDialog = alertdialog.create();
            alertDialog.show();

            buttonchangeiteminfo.setOnClickListener(view1 -> {
                String edtextitemlocal = editemlocal.getText().toString();
                String edtextitemnote = editemnote.getText().toString();

                if (edtextitemlocal.equals("")) {
                    editemlocal.setError("不可空白");
                    return;
                } else if (edtextitemnote.equals("")) {
                    editemnote.setError("不可空白");
                    return;
                }

                new Thread(() -> {
                    try {
                        HttpRequest.getInstance().UpdateItem(item_info, edtextitemlocal, edtextitemnote, null);
                        runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新成功", Toast.LENGTH_SHORT).show());
                        startActivity(getIntent());
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    } catch (HttpRequest.UpdateDataError e) {
                        runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新失敗", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            });
        });

    }


    public void updateItemState(View view) {
        new Thread(() -> {
            try {
                HttpRequest.ItemState state = new HttpRequest.ItemState();//會確認說哪邊有按到哪邊沒有
                state.correct = correct.isChecked();//檢查哪邊有按到
                state.fixing = fixing.isChecked();
                state.discard = discard.isChecked();
                state.unlabel = unlabel.isChecked();
                HttpRequest.getInstance().UpdateItem(item_info, null, null, state);
                runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新成功", Toast.LENGTH_SHORT).show());
                startActivity(new Intent(UpdateItemContent.this, UpdateItemContent.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.UpdateDataError e) {
                runOnUiThread(() -> Toast.makeText(UpdateItemContent.this, "更新失敗", Toast.LENGTH_SHORT).show());
            }
        }).start();

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            if (fromdataview_int == 11) {
                DataViewActivity.upLoad(UpdateItemContent.this);
                fromdataview_int = 0;
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    class ItemInfoItemAdpter extends ExtentBaseAdpter<HttpRequest.BorrowRecord> {

        protected ItemInfoItemAdpter(Activity activity, LoadData<HttpRequest.BorrowRecord> callback) {
            super(activity, callback);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());//初始一個背後 layout畫板
                view = inflater.inflate(R.layout.list_brrowinfo, null);
            }
            if (!done && i + 5 > this.data.size()) {
                this.loadItems();
            }

            HttpRequest.BorrowRecord info = (HttpRequest.BorrowRecord) this.getItem(i);
            HttpRequest.ItemInfo itemInfo = itemInfoMap.get(info.item_id);

            CheckBox checkBox = view.findViewById(R.id.checkBoxdata);
            TextView item_name = view.findViewById(R.id.infobrrow);
            TextView item_local = view.findViewById(R.id.namebrrow);
            TextView item_id = view.findViewById(R.id.idbrrow);

            HttpRequest.BorrowerInfo borrowinfo = borrowerInfoMap.get(info.borrower_id);


            item_name.setText(itemInfo.name + " " + info.id + "" + " Itemid:" + info.item_id + "");
            item_local.setText(borrowinfo.name + "");
            item_id.setText(info.borrow_date.substring(0, 10));


            checkBox.setChecked(!info.reply_date.equals("null"));


            checkBox.setOnClickListener(view1 -> {
                if (checkBox.isChecked()) {
                    new Thread(() -> {
                        try {
                            HttpRequest.getInstance().ThisAccountBorrowerItemUpreturned(info.id, true);

                            runOnUiThread(() -> {
                                Toast.makeText(UpdateItemContent.this, borrowinfo.name + "已還", Toast.LENGTH_SHORT).show();
                            });
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        } catch (HttpRequest.SignUpError signUpError) {
                            signUpError.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(UpdateItemContent.this, "fail", Toast.LENGTH_LONG).show();
                            });
                        }
                    }).start();


                } else {
                    new Thread(() -> {
                        try {
                            HttpRequest.getInstance().ThisAccountBorrowerItemUpreturned(info.id, false);
                            runOnUiThread(() -> {
                                Toast.makeText(UpdateItemContent.this, borrowinfo.name + "未還", Toast.LENGTH_SHORT).show();
                            });
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        } catch (HttpRequest.SignUpError signUpError) {
                            signUpError.printStackTrace();
                            runOnUiThread(() -> {
                                Toast.makeText(UpdateItemContent.this, "fail", Toast.LENGTH_LONG).show();
                            });
                        }
                    }).start();
                }
            });

            return view;
        }
    }


}

