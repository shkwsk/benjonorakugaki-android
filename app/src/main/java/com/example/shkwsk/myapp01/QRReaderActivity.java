package com.example.shkwsk.myapp01;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;

public class QRReaderActivity extends AppCompatActivity {
    private static final String TAG = "CameraSample";

    private int CAMERA_ID = 0; // for Nexus7

    private Camera camera;
    private RelativeLayout relativeLayout;
    private SurfaceView surfaceView;
    private View rectView;
    private CameraListener cameraListener = new CameraListener();

    private String url, query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("QRReaderActivity");
        relativeLayout = new RelativeLayout(this);
        surfaceView = new SurfaceView(this);

        rectView = new View(this) {
            @Override
            protected void onDraw(Canvas canvas) {
                Paint bgpaint = new Paint();
                bgpaint.setColor(0x99ffffff);
                canvas.drawRect(0, 0, surfaceView.getWidth(), surfaceView.getHeight(), bgpaint);

                Paint paint = new Paint();
                paint.setColor(Color.TRANSPARENT);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                float widthQuarter = surfaceView.getWidth() / 4;
                float heightQuarter = surfaceView.getHeight() / 4;
                canvas.drawRect(widthQuarter , heightQuarter, widthQuarter * 3, heightQuarter * 3, paint);
            }
        };

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(cameraListener);
        relativeLayout.addView(surfaceView);
        relativeLayout.addView(rectView);
        setContentView(relativeLayout);
    }

    private int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

    private class CameraListener implements
            SurfaceHolder.Callback,
            AutoFocusCallback,
            Camera.PictureCallback,
            Camera.PreviewCallback
    {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open(CAMERA_ID);
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.e(TAG, e.toString(), e);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int width, int height) {

            // カメラのプレビューサイズをViewに設定
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getSupportedPreviewSizes().get(0); // 0=最大サイズ
            parameters.setPreviewSize(size.width, size.height);
            camera.setParameters(parameters);

            // 画面回転補正。
            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                camera.setDisplayOrientation(90);
                layoutParams.width = size.height;
                layoutParams.height = size.width;
            } else {
                camera.setDisplayOrientation(0);
                layoutParams.width = size.width;
                layoutParams.height = size.height;
            }
            surfaceView.setLayoutParams(layoutParams);

            // オートフォーカス設定。
            camera.autoFocus(cameraListener);

            camera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.autoFocus(null);
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                Log.d(TAG, "focus");
                // プレビューのデータ取得。
                camera.setPreviewCallback(cameraListener);
                // フルサイズ画像はTODO
                //camera.takePicture(null,null,cameraListener);
            }
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // フルサイズ画像もやることは同じ。
            onPreviewFrame(data, camera);
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // 処理は1回なのでコールバック取り消し
            camera.setPreviewCallback(null);

            // 基礎データ取得
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(CAMERA_ID, info);
            int w = camera.getParameters().getPreviewSize().width;
            int h = camera.getParameters().getPreviewSize().height;
            boolean isMirror = (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);

            // プレビュー画像の型変換
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                    data, w, h, w/4, h/4, w/2, h/2, isMirror);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // QRコード読み込み。
            Reader reader = new MultiFormatReader();
            try {
                Log.d(TAG, "decode");
                Result result = reader.decode(bitmap);
                String board_id = result.getText();

                // サーバとの描画通信に用いるURL
                url = getIntent().getExtras().getString("url");
                query = getIntent().getExtras().getString("query");
                query = url + "/api/v1/board/?" + "id=" + board_id;
                System.out.println(query);

                Intent intent_draw = new Intent(QRReaderActivity.this, DrawActivity.class);
                intent_draw.putExtra("url", url);
                intent_draw.putExtra("board_id", board_id);
                intent_draw.putExtra("query", query);
                try {
                    startActivity(intent_draw);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                QRReaderActivity.this.finish();

                // Toast.makeText(QRReaderActivity.this, text, Toast.LENGTH_LONG).show();
                Log.i(TAG, "result:" + board_id);
                camera.stopPreview();
                camera.autoFocus(null);
            } catch (Exception e) {
                // QRコード認識失敗でも例外発生する。
                Log.d(TAG, "decode-fail:" + e.toString());
                camera.autoFocus(cameraListener);
            }
        }
    }
}
