package com.example.shkwsk.myapp01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.net.URL;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class DrawingView extends View {
    private float posx = 0f; //イベントが起きたX座標
    private float posy = 0f; //イベントが起きたY座標
    private Path path = null; //パス
    private Bitmap bmp = null; //Viewの状態を保存するためのBitmap
    private Paint paint;
    private int color = Color.RED;
    private boolean DRAWING = false;
    Toast msg_please_rakugaki = Toast.makeText(getContext(), "何からくがきしてみてください。", Toast.LENGTH_LONG);
    Toast msg_wait_rakugaki = Toast.makeText(getContext(), "らくがきしています…", Toast.LENGTH_LONG);
    Toast msg_complete_rakugaki = Toast.makeText(getContext(), "らくがきしました！", Toast.LENGTH_LONG);

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(bmp != null){
            canvas.drawBitmap(bmp, 0, 0, null);
        }
        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(6);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        if(path != null){
            canvas.drawPath(path, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        DRAWING = true;
        //イベントのタイプごとに処理を設定
        switch(e.getAction()){
            case MotionEvent.ACTION_DOWN: //最初のポイント
                this.path = new Path();
                posx = e.getX();
                posy = e.getY();
                this.path.moveTo(e.getX(), e.getY());
                break;
            case MotionEvent.ACTION_MOVE: //途中のポイント
                posx += (e.getX()-posx)/1.4;
                posy += (e.getY()-posy)/1.4;
                this.path.lineTo(posx, posy);
                invalidate();
                break;
            case MotionEvent.ACTION_UP: //最後のポイント
                this.path.lineTo(e.getX(), e.getY());
                //キャッシュの中からキャプチャを作成するので、その一瞬の為にキャッシュをON
                setDrawingCacheEnabled(true);
                bmp = Bitmap.createBitmap(getDrawingCache());
                setDrawingCacheEnabled(false);
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

//    public void delete() {
//        this.path.reset();
//        invalidate();
//    }

    public void commit(File dir, String url) {
        if (!DRAWING) {
            msg_please_rakugaki.show();
            return;
        }
        msg_wait_rakugaki.show();
        System.out.println("commit");
        File ext_file = new File(Environment.getExternalStorageDirectory().getPath()+"/drawbm/");
        try{
            if(!ext_file.exists()){
                ext_file.mkdir();
            }
        }catch(SecurityException e){}
        String image_path = "tmp.png";

        //File tmp_file = new File(ext_file, image_path); //テスト
        File tmp_file = new File(dir, image_path); //本番
        // 描画画像保存
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.id.drawing_view);
        System.out.println(tmp_file);
        try {
            FileOutputStream fos = new FileOutputStream(tmp_file);
            //PNG形式で出力
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, 480, 640, false); // 何も描画していないとエラーで落ちる
            resizedBitmap.compress(CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(bmp);

        // 描画画面の背景画像設定
        try {
            System.out.println("DrawActivity: " + url);
            // サブスレッドで実行するタスクを作成
            AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(String... params) {
                    try {
                        URL post_url = new URL(params[0]);
                        System.out.println("post to:" + post_url);
                        File tmp_file = new File(params[1], params[2]);
                        List<NameValuePair> postData = new ArrayList<>();
                        NameValuePair nv = new BasicNameValuePair("comment", "この写真はxxで撮りました。");
                        postData.add(nv);
                        HttpMultipartSender request = new HttpMultipartSender(
                                post_url.toString(),
                                postData,
                                "image",
                                tmp_file.toString());
                        String response = request.send();
                        System.out.println(response);
                        return true;
                    } catch (Exception e) {
                        System.out.println(e);
                        return false;
                    }
                }
            };
            //task.execute(url, ext_file.toString(), image_path); //テスト
            task.execute(url, dir.toString(), image_path); //本番
        } catch (Exception e) {
            System.out.println(e); // IOerror, URLerror
        }
        msg_wait_rakugaki.cancel();
        msg_complete_rakugaki.show();
    }

    public void setColor(int color) {
        System.out.println("color:"+ color);
        this.color = color;
    }
}
