package com.example.shkwsk.myapp01;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.InputStream;
import java.net.URL;

public class ViewDrawActivity extends AppCompatActivity {
    private View view;
    private String url, post_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewdraw);
        System.out.println("start ViewDrawActivity.");

        // サーバとの描画通信に用いるURL
        url = getIntent().getExtras().getString("url");
        post_url = getIntent().getExtras().getString("query");

        // 描画画面の振る舞い設定
        this.view = findViewById(R.id.view);

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
                    view.setBackgroundDrawable(ob);
                }
            };
            task.execute(url);
        } catch (Exception e) {
            System.out.println(e); // IOerror, URLerror
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
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
