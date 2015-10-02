package com.example.shkwsk.myapp01;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.RadioButton;
import android.widget.RadioGroup;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;

import java.io.*;
import java.net.URL;

public class ViewDrawActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private String url, post_url;
    private int vHeight, vWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        System.out.println("start DrawActivity.");

        // サーバとの描画通信に用いるURL
        url = getIntent().getExtras().getString("url");
        post_url = getIntent().getExtras().getString("query");

        //ラジオボタンの振る舞い設定
        RadioGroup color_radiogroup = (RadioGroup)findViewById(R.id.color_radiogroup);
        color_radiogroup.setOnCheckedChangeListener(changeColor);

        // 描画画面の振る舞い設定
        this.drawingView = (DrawingView)findViewById(R.id.drawing_view);

        // 描画画面の背景画像設定
        try {
            System.out.println("DrawActivity: " + url);
            // サブスレッドで実行するタスクを作成
            AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
                BitmapDrawable ob;
                @Override
                protected Boolean doInBackground(String... params) {
                    try {
                        URL imgurl = new URL(params[0]);
                        InputStream is = imgurl.openStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        ob = new BitmapDrawable(getResources(), bitmap);
                        return true;
                    } catch (Exception e) {
                        // error
                        return false;
                    }
                }
                @Override
                protected void onPostExecute(Boolean result) {
                    drawingView.setBackground(ob);
                }
            };
            task.execute(url);
        } catch (Exception e) {
            System.out.println(e); // IOerror, URLerror
        }

        // 下部ボタンの振る舞い設定
        findViewById(R.id.commit_button).setOnClickListener(commitDrawing);
        findViewById(R.id.undo_button).setOnClickListener(undo);
    }

    RadioGroup.OnCheckedChangeListener changeColor = new RadioGroup.OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton radioButton = (RadioButton) findViewById(checkedId);
            drawingView.setColor(radioButton.getTextColors().getDefaultColor());
        }
    };
    View.OnClickListener commitDrawing = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            drawingView.commit(getCacheDir(), post_url);
        }
    };
    View.OnClickListener undo = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            drawingView.undo();
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // 描画画面の縦横サイズを取得
        vHeight = drawingView.getHeight();
        vWidth = drawingView.getWidth();
        drawingView.setViewSize(vHeight, vWidth);
        System.out.println("ビューサイズ 縦:" + drawingView.getHeight() + "横:" + drawingView.getWidth());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_draw, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
