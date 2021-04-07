package com.example.my_qr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class CameraGetItemId extends AppCompatActivity {
    HttpRequest.ItemInfo infoItem;
    CameraSource cameraSource;
    SurfaceView surfaceView;
    BarcodeDetector barcodeDetector;
    TextView brrowItemtext;
    String getBackItemId, getBackItemName = "";
    Integer getItemJsonId;
    static int PUT_HAVE_GET_ITENDATA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_get_item_id);
        getPermission();
        surfaceView = findViewById(R.id.cambrrowsurfaceView);
        brrowItemtext = findViewById(R.id.brrowinfotext);

        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).build();//"相機功能"包含"QR功能"


        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(CameraGetItemId.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());//我這畫布 放入有"QR功能"的"相機"
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

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
                    final String input = qrCode.valueAt(0).displayValue;
                    if (input.equals(lastQr)) {
                        return;
                    } else {
                        lastQr = input;
                    }
                    showBrrowItem(lastQr);
                }
            }

        });


    }

    public void showBrrowItem(String itemId) {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.getInstance();
                HttpRequest.ItemInfo info = request.GetItem(itemId);
                infoItem = info;
                runOnUiThread(() -> brrowItemtext.setText("借出產品:" + info.name + "\n" + "借出ID:" + info.item_id + "\n"));
                getBackItemName = info.name;
                getBackItemId = info.item_id;
                getItemJsonId = info.id;

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } catch (HttpRequest.GetDataError getDataError) {
                getDataError.printStackTrace();
            }

        }).start();
    }

    public void getInfoBack(View v) {

        Intent intent = new Intent(CameraGetItemId.this, NewBorrowerActivity.class);
        intent.putExtra("ItemID", getBackItemId);
        intent.putExtra("ItemName", getBackItemName);
        intent.putExtra("getItemJsonId", getItemJsonId);
        finish();
        startActivity(intent);


    }


    public void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

}
