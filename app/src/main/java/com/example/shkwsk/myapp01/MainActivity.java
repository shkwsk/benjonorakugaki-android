package com.example.shkwsk.myapp01;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import java.util.UUID;

public class MainActivity extends FragmentActivity {
    String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("System start MainActivity.");

        uuid = UUID.randomUUID().toString();
        System.out.println(uuid);

        Intent intent_sb = new Intent(MainActivity.this, SelectBoardActivity.class);
        intent_sb.putExtra("uuid", uuid);
        try {
            startActivity(intent_sb);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        MainActivity.this.finish(); // タイトル画面にはもう戻らない
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        invalidateOptionsMenu();
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
