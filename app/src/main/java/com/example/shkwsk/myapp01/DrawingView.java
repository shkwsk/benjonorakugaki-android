package com.example.shkwsk.myapp01;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DrawingView extends View {
    private float posx = 0f; //イベントが起きたX座標
    private float posy = 0f; //イベントが起きたY座標
    private Path path = null; //描画パス
    ArrayList<Path> drawList = new ArrayList<Path>();
    ArrayList<String> XYs;
    HashMap<Path, ArrayList<String>> drawXYs = new HashMap<>();
    private Bitmap bmp = null; //Viewの状態を保存するためのBitmap
    private Paint paint;
    private int color = Color.RED;
    HashMap<Path, Integer> drawColor = new HashMap<>();
    private int height, width;
    Toast msg_please_rakugaki = Toast.makeText(getContext(), "何からくがきしてみてください。", Toast.LENGTH_LONG);
    Toast msg_wait_rakugaki = Toast.makeText(getContext(), "らくがきしています…", Toast.LENGTH_LONG);
    Toast msg_complete_rakugaki = Toast.makeText(getContext(), "らくがきしました！", Toast.LENGTH_LONG);

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        if(bmp != null){
//            canvas.drawBitmap(bmp, 0, 0, null);
//        }
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(6);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        // 保持していたパスをキャンバスに描画
        for (int i = 0; i < drawList.size(); i++) {
            paint.setColor(drawColor.get(drawList.get(i)));
            canvas.drawPath(drawList.get(i), paint);
        }
        paint.setColor(color);
        if(path != null){
            canvas.drawPath(path, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //イベントのタイプごとに処理を設定
        switch(e.getAction()){
            case MotionEvent.ACTION_DOWN: //最初のポイント
                this.path = new Path();
                this.XYs = new ArrayList<>();
                posx = e.getX();
                posy = e.getY();
                this.path.moveTo(posx, posy);
                this.XYs.add(String.valueOf(posx) + ' ' + String.valueOf(posy));
                break;
            case MotionEvent.ACTION_MOVE: //途中のポイント
                posx += (e.getX()-posx)/1.4;
                posy += (e.getY()-posy)/1.4;
                this.path.lineTo(posx, posy);
                this.XYs.add(String.valueOf(posx) + ' ' + String.valueOf(posy));
                invalidate();
                break;
            case MotionEvent.ACTION_UP: //最後のポイント
                posx = e.getX();
                posy = e.getY();
                this.path.lineTo(posx, posy);
                drawList.add(path); // 描画パス保持
                this.XYs.add(String.valueOf(posx) +' '+ String.valueOf(posy));
                this.drawXYs.put(this.path, this.XYs);
                this.drawColor.put(this.path, this.color);
                System.out.println(getResources().getDisplayMetrics().density);
                System.out.println(this.path.toString() +'\n'+ this.XYs);
                //キャッシュの中からキャプチャを作成するので、その一瞬の為にキャッシュをON
//                setDrawingCacheEnabled(true);
//                bmp = Bitmap.createBitmap(getDrawingCache());
//                setDrawingCacheEnabled(false);
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    public void undo() {
        System.out.println("undo");
        if (!this.drawList.isEmpty()) {
            Path undo_path = this.drawList.remove(drawList.size() - 1);
            this.drawColor.remove(undo_path);
            this.drawXYs.remove(undo_path);
            this.path.reset();
            invalidate();
        }
    }

    public void commit(File dir, String url) {
        if (drawList.isEmpty()) {
            msg_please_rakugaki.show();
            return;
        }
        msg_wait_rakugaki.show();
        System.out.println("commit");

        // POST通信準備
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        // 情報付与
        ArrayList <NameValuePair> params = new ArrayList<>();
        params.add( new BasicNameValuePair("height", String.valueOf(height)) );
        params.add( new BasicNameValuePair("width", String.valueOf(width)) );
        for (int i = 0; i < drawList.size(); i++) {
            Path path = drawList.get(i);
            ArrayList<String> points = drawXYs.get(path);
            points.add(0, String.valueOf(drawColor.get(path)));
            //System.out.println(points.toString());
            params.add(new BasicNameValuePair("path"+String.valueOf(i), points.toString()));
        }
        System.out.println(url);
        System.out.println(params);

        // 通信結果
        HttpResponse res = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
            res = httpClient.execute(post);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 一時的に保存した画像をサーバにPOST
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
            //task.execute(url, dir.toString(), image_path); //本番
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

    public void setViewSize(int h, int w) {
        this.height = h;
        this.width = w;
    }
}
