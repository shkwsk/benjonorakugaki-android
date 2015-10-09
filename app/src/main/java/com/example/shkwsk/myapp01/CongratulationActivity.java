package com.example.shkwsk.myapp01;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class CongratulationActivity extends AppCompatActivity {
    String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratulation);
        System.out.println("Congratulation");
        System.out.println("onCreate");

        uuid = AppPref.getUUID(this);
        System.out.println(uuid);

        // uuidからQRコード生成
        String size = getIntent().getExtras().getString("size");
        System.out.println(size);
        if (size != null) {
            try {
                Bitmap bitmap = writeQRCode(uuid, Integer.parseInt(size));
                // QR画面表示
                ImageView imageView = (ImageView) findViewById(R.id.result_view);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            System.out.println(size);
        }
    }

    private Bitmap writeQRCode(String contents, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bm = writer.encode(contents, BarcodeFormat.QR_CODE, size, size);
        int width = bm.getWidth();
        int height = bm.getHeight();
        int[] pixels = new int[width * height];
        // データがあるところだけ黒にする
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bm.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
