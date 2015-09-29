package com.example.shkwsk.myapp01;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SelectBoardActivity extends AppCompatActivity {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private JSONArray location_list; // サーバから受け取るらくがき位置リスト
    private String URL;
    private String board_url, post_url;
    final HashMap<String, String> marker_id = new HashMap();
    Toast msg_please, msg_tap;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String Port = String.format(":%s", getText(R.string.port)); //テスト:3000, 本番:5963
        URL = String.format("http://%s%s", getText(R.string.address), Port);
        setContentView(R.layout.activity_select_board);
        msg_please = Toast.makeText(getApplicationContext(), "地図マーカーをタップしてね。", Toast.LENGTH_SHORT);

        System.out.println("start SelectBoardActivity.");
        String location_str = getIntent().getExtras().getString("location_json");
        try {
            JSONObject location_json = new JSONObject(location_str);
            requestLocationList(location_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mapLocationList();
        msg_please.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_board, menu);
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

    // private method
    private void requestLocationList(final JSONObject location_info) {
        Thread th_http;
        th_http = new Thread(new Runnable() {
            @Override
            public void run() {
                // リクエストクエリ作成
                String query = "";
                try {
                    query = URL + "/api/v1/spot/?" +
                            "lat=" + location_info.get("Lat") + "&" +
                            "lon=" + location_info.get("Lon") + "&" +
                            "acc=" + location_info.get("Acc");
                } catch (JSONException ex) {
                    // エラー処理
                }
                System.out.println(query);

                // リクエスト送信
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(query);
                // 取得
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String res = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                    // 位置情報テストデータ
//                       String res = "[" +
//                               "{\"ID\":\"A1\", \"Lon\":\"137.408691\", \"Lat\":\"34.701983\"}," +
//                               "{\"ID\":\"B1\", \"Lon\":\"137.408563\", \"Lat\":\"34.701406\"}" +
//                               "]"; // 返ってきた文字列データを仮定
                    System.out.println(res);
                    System.out.println("Response succeeded!");
                    location_list = new JSONArray(res);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        });
        th_http.start();
        // スレッドの終了を待つ
        try {
            th_http.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mapLocationList() {
        System.out.println("Mapping location list.");
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        } else { System.exit(1); }

        // 最近傍スポットの周辺を表示
        try {
            JSONObject nearest_spot = location_list.getJSONObject(0);
            double nlat = Double.parseDouble(nearest_spot.get("lat").toString());
            double nlon = Double.parseDouble(nearest_spot.get("lon").toString());
            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(nlat, nlon), 17);
            mMap.moveCamera(cu);
        } catch (JSONException e) {
            // JSONError
        }

        // 現在地表示
        mMap.setMyLocationEnabled(true);

        // マーカーの描画
        for (int i = 0; i < location_list.length(); i++) {
            try {
                JSONObject location = location_list.getJSONObject(i);
                final String ID = location.get("id").toString();
                final String name = location.get("name").toString();
                double lat = Double.parseDouble(location.get("lat").toString());
                double lon = Double.parseDouble(location.get("lon").toString());
                Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(name));
                marker_id.put(m.getId(), ID);
            } catch (Exception json_error) {
                System.out.println(json_error);
            }
        }

        // マーカータップ時の振る舞いを定義
        mMap.setOnMarkerClickListener(
                new OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        System.out.println("OnMarkerClick is started.");
                        msg_please.cancel();
                        msg_tap = Toast.makeText(getApplicationContext(), marker.getTitle() + "が選択されました。", Toast.LENGTH_LONG);
                        msg_tap.show();
                        connectBoard(marker);
                        return false;
                    }
                });
    }

    private void connectBoard(final Marker marker) {
        // 位置IDをサーバに送る。
        final String query = URL + "/api/v1/board/?" + "id=" + marker_id.get(marker.getId());
        System.out.println(query);

        // 描画画面へ遷移
        final Intent intent_draw = new Intent(SelectBoardActivity.this, DrawActivity.class);
        intent_draw.putExtra("url", query);
        intent_draw.putExtra("query", query);
        try {
            startActivity(intent_draw);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

}
