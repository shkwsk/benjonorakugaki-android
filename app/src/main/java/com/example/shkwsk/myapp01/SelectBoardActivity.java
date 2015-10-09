package com.example.shkwsk.myapp01;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectBoardActivity extends AppCompatActivity {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private JSONArray location_list; // サーバから受け取るらくがき位置リスト
    private String URL, query;
    ArrayList<Boolean> locations_flag = new ArrayList<>();;
    final HashMap<String, String> marker_id = new HashMap();
    Toast msg_please, msg_tap, msg_cong;
    LatLng my_location;
    int size_QRcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("start SelectBoardActivity.");
        System.out.println("onCreate()");

        String Port = String.format(":%s", getText(R.string.port)); //テスト:3000, 本番:5963, 文化祭:4649
        URL = String.format("http://%s%s", getText(R.string.address), Port);
        setContentView(R.layout.activity_select_board);

        msg_please = Toast.makeText(getApplicationContext(), "一度らくがきするとマーカータップで確認できます。", Toast.LENGTH_SHORT);
        msg_cong = Toast.makeText(getApplicationContext(), "おめでとうございます！！", Toast.LENGTH_SHORT);

        msg_please.show();

        // 技科大のスポットをリクエスト
        String location_tut = "{\"ID\":\"A1\", \"Lon\":\"137.408691\", \"Lat\":\"34.701983\", \"Acc\":\"1000\"}";
        try {
            JSONObject location_json = new JSONObject(location_tut);
            requestLocationList(location_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // QRボタンの挙動
        findViewById(R.id.QR_button).setOnClickListener(QRReader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume()");
        // スポットのらくがきフラグ
        locations_flag = AppPref.getLocationFlag(this);
        //System.out.println(locations_flag.toString());

        // congratulationボタンの表示・非表示
        if (locations_flag.contains(false)) {
            findViewById(R.id.congratulation).setVisibility(View.INVISIBLE);
        } else {
            msg_cong.show();
            findViewById(R.id.congratulation).setVisibility(View.VISIBLE);
            findViewById(R.id.congratulation).setOnClickListener(QRWriter);
        }

        // 地図描画
        mapLocationList();
    }

    View.OnClickListener QRReader = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Intent intent_draw = new Intent(SelectBoardActivity.this, QRReaderActivity.class);
            intent_draw.putExtra("url", URL);
            intent_draw.putExtra("query", query);
            try {
                startActivity(intent_draw);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    };
    View.OnClickListener QRWriter = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Intent intent_cong = new Intent(SelectBoardActivity.this, CongratulationActivity.class);
            intent_cong.putExtra("size", String.valueOf(size_QRcode));
            try {
                startActivity(intent_cong);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // 描画画面の縦横サイズを取得
        Display display = getWindowManager().getDefaultDisplay();
        int h = display.getHeight();
        int w = display.getWidth();
        size_QRcode = w < h ? w : h;
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
                //System.out.println(query);

                // リクエスト送信
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(query);
                // 取得
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    String res = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                    //System.out.println(res);
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
        if (my_location == null) {
            try {
                JSONObject nearest_spot = location_list.getJSONObject(0);
                double nlat = Double.parseDouble(nearest_spot.get("lat").toString());
                double nlon = Double.parseDouble(nearest_spot.get("lon").toString());
                my_location = new LatLng(nlat, nlon);
            } catch (JSONException e) {
                // JSONError
            }
        }
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(my_location, Integer.parseInt(this.getString(R.string.map_zoom)));
        mMap.moveCamera(cu);

        // 現在地取得
        mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location loc) {
                System.out.println("setOnMyLocationChangeListener");
                my_location = new LatLng(loc.getLatitude(), loc.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(my_location));
                mMap.setOnMyLocationChangeListener(null);
            }
        });

        // 現在地表示
        mMap.setMyLocationEnabled(true);

        // マーカーの描画
        BitmapDescriptor icon_t = BitmapDescriptorFactory.fromResource(R.drawable.virtual_rakugaki);
        BitmapDescriptor icon_f = BitmapDescriptorFactory.fromResource(R.drawable.hatena);
        for (int i = 0; i < location_list.length(); i++) {
            try {
                JSONObject location = location_list.getJSONObject(i);
                final String ID = location.get("id").toString();
                final String name = location.get("name").toString();
                double lat = Double.parseDouble(location.get("lat").toString());
                double lon = Double.parseDouble(location.get("lon").toString());
                MarkerOptions options = new MarkerOptions();
                options.position(new LatLng(lat, lon)).title(name);
                if ( locations_flag.get(Integer.parseInt(ID) - 1) ) {
                    locations_flag.set(Integer.parseInt(ID) - 1, true);
                    options.icon(icon_t);
                } else {
                    options.icon(icon_f);
                }
                Marker m = mMap.addMarker(options);
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
                        int id = Integer.parseInt(marker_id.get(marker.getId()));
                        System.out.println("Marker tapped. at " + id);
                        if (locations_flag.get(id - 1)) {
                            msg_please.cancel();
                            connectBoard(marker);
                        } else {
                            msg_please.cancel();
                        }
                        return false;
                    }
                });
    }

    private void connectBoard(final Marker marker) {
        // 位置IDをサーバに送る。
        query = URL + "/api/v1/board/?" + "id=" + marker_id.get(marker.getId());
        System.out.println(query);

        // 描画画面へ遷移
        final Intent intent_draw = new Intent(SelectBoardActivity.this, ViewDrawActivity.class);
        intent_draw.putExtra("url", query);
        intent_draw.putExtra("query", query);
        try {
            startActivity(intent_draw);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

}
