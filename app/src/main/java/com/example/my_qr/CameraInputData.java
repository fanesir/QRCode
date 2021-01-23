package com.example.my_qr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class CameraInputData extends AppCompatActivity {
    TextView itemDescription;
    SurfaceView surfaceView;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    HttpRequest.ItemInfo info;

    CheckBox correct, discard, fixing, unlabel;
    Button confirmUpdate, updateButton;

    OnClickListener checkBoxUpdate = view -> {
        if (info == null)
            return;
        if (info.correct != correct.isChecked()) {//按下
            confirmUpdate.setVisibility(View.VISIBLE);//顯示
            return;
        }

        if (info.discard != discard.isChecked()) {
            confirmUpdate.setVisibility(View.VISIBLE);
            return;
        }

        if (info.fixing != fixing.isChecked()) {
            confirmUpdate.setVisibility(View.VISIBLE);
            return;
        }

        if (info.unlabel != unlabel.isChecked()) {
            confirmUpdate.setVisibility(View.VISIBLE);
            return;
        }

        confirmUpdate.setVisibility(View.INVISIBLE);
    };

    OnClickListener confirmUpdateCallback = view -> {//pushbutton 與 chenkbox
        if (CameraInputData.this.info == null) {//如果什麼都沒輸入
            return;
        }
        new Thread(() -> {
            try {
                HttpRequest.ItemState state = new HttpRequest.ItemState();//會確認說哪邊有按到哪邊沒有
                state.correct = correct.isChecked();//檢查哪邊有按到
                state.fixing = fixing.isChecked();
                state.discard = discard.isChecked();
                state.unlabel = unlabel.isChecked();
                HttpRequest.getInstance().UpdateItem(info.item_id, null, null, state);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.UpdateDataError e) {
                runOnUiThread(() -> {
                    Toast.makeText(CameraInputData.this, "更新失敗", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getPermission();
        surfaceView = findViewById(R.id.cam_surfaceView);
        itemDescription = findViewById(R.id.show_qr_text);
        correct = findViewById(R.id.correct);
        discard = findViewById(R.id.discard);
        fixing = findViewById(R.id.fixing);
        unlabel = findViewById(R.id.unlabel);
        confirmUpdate = findViewById(R.id.update_confirm);
        updateButton = findViewById(R.id.updatabutton);//更新訂單內容

        updateButton.setVisibility(View.INVISIBLE);
        updateButton.setOnClickListener(this.toItemDetail);

        confirmUpdate.setVisibility(View.INVISIBLE);
        confirmUpdate.setOnClickListener(confirmUpdateCallback);

        correct.setOnClickListener(this.checkBoxUpdate);//設定按下之後做什麼?
        discard.setOnClickListener(this.checkBoxUpdate);
        fixing.setOnClickListener(this.checkBoxUpdate);
        unlabel.setOnClickListener(this.checkBoxUpdate);

        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(CameraInputData.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            String lastQr = "";

            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> qrCode = detections.getDetectedItems();
                if (qrCode.size() != 0) {
                    final String input = qrCode.valueAt(0).displayValue;//讀到的字串
                    if (input.equals(lastQr)) {//防止重複
                        return;
                    } else {
                        lastQr = input;
                    }

                    itemDescription.post(() -> { // 設定textview
                        CameraInputData.this.getAndUpdateItem(input);
                    });
                }
            }
        });
    }

    public void getAndUpdateItem(String item_id) {//pro
        new Thread(() -> {
            try {
                runOnUiThread(() -> updateButton.setVisibility(View.VISIBLE));//改由ui的執行序來執行

                HttpRequest request = HttpRequest.getInstance();
                HttpRequest.ItemInfo info = request.GetItem(item_id);
                info.correct = true;
                this.info = info;//全域info = getinfo
                updateViewer();

                HttpRequest.ItemState state = new HttpRequest.ItemState();
                state.correct = true;
                state.discard = info.discard;// this長什麼樣子 = 資料來源
                state.fixing = info.fixing;
                state.unlabel = info.unlabel;
                request.UpdateItem(item_id, null, null, state);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.GetDataError e) {
                runOnUiThread(() -> {
                    Toast.makeText(CameraInputData.this, "無此產品", Toast.LENGTH_SHORT).show();
                    itemDescription.setText("");
                });
            } catch (HttpRequest.UpdateDataError e) {
                runOnUiThread(() -> {
                    Toast.makeText(CameraInputData.this, "更新失敗", Toast.LENGTH_SHORT).show();
                    itemDescription.setText("");
                });
            }
        }).start();
    }

    protected void updateViewer() {//先掃文件秀出東西 確認更新

        if (this.info == null)
            return;
        runOnUiThread(() -> {
            confirmUpdate.setVisibility(View.INVISIBLE);

            itemDescription.setText(
                    String.format(getString(R.string.item_result_template),
                            this.info.item_id,
                            this.info.item_id,
                            this.info.name,
                            this.info.location
                    )
            );
            this.correct.setChecked(this.info.correct);
            this.discard.setChecked(this.info.discard);
            this.fixing.setChecked(this.info.fixing);
            this.unlabel.setChecked(this.info.unlabel);
        });

    }

    final View.OnClickListener toItemDetail = view -> {
        Intent intent = new Intent(this, UpdateItemContent.class);
        intent.putExtra("item_info", this.info);
        startActivity(intent);
    };

    public void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent(this, DataViewActivity.class);
            finish();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}