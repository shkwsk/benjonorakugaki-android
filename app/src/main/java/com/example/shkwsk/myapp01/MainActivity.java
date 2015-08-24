package com.example.shkwsk.myapp01;

import java.io.IOException;
import java.util.*;
import java.util.logging.Handler;
import java.lang.Runnable;
import java.net.URL;
import java.util.concurrent.ThreadFactory;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import android.content.Intent;
import android.content.res.Resources;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

public class MainActivity extends FragmentActivity implements LocationListener {

    private LocationManager locationManager;
    private final String Port = ":3000";
    private final String URL = "http://27.120.85.147" + Port;
    private JSONArray location_list; // サーバから受け取るらくがき位置リスト
    private String board_url, post_url;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    final HashMap<String, String> marker_id = new HashMap();
    // タイトル画像
    //Resources res = this.getContext().getResources();
//    Resources res = this.getApplication().getResources();
//    Bitmap toilet = BitmapFactory.decodeResource(res, R.drawable.toilet);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("System started.");
        System.out.println("onCreate()");
//        ImageView v = (ImageView)findViewById(R.id.gazou);
//        v.setImageBitmap(toilet);

        // LocationManager インスタンス生成
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // final boolean wifiEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        /*
        else if (!wifiEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(settingsIntent);
        }
        */
    }

    @Override
    protected void onResume() {
        System.out.println("onResume()");
        if (locationManager != null) {
            // minTime = 1000msec, minDistance = 50m
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 50, this);
        } else {
            System.out.println("locationManager=null\n");
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (locationManager != null) {
            // update を止める
            locationManager.removeUpdates(this);
        } else {
            System.out.println("onPause()");
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    // LocationListenerインタフェース4つ
    @Override
    public void onLocationChanged(Location location) {
        JSONObject location_info = getLocationInfo(location);
        System.out.println(location_info);
        //Intent intent = new Intent(MainActivity.this, SelectBoardActivity.class);
        requestLocationList(location_info);
//        ImageView v = (ImageView)findViewById(R.id.gazou);
//        v.setImageBitmap(null);
        mapLocationList(location_list);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                System.out.println("LocationProvider.AVAILABLE\n");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                System.out.println("LocationProvider.OUT_OF_SERVICE\n");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                System.out.println("LocationProvider.UNAVAILABLE\n");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    // private methods
    private JSONObject getLocationInfo(Location location) {
        JSONObject location_info = new JSONObject();
        location.getLatitude();
        try {
            location_info.put("Lat", String.valueOf(location.getLatitude()));
            location_info.put("Lon", String.valueOf(location.getLongitude()));
            location_info.put("Acc", String.valueOf(location.getAccuracy()));
            //location_info.put("Latitude", location.getAltitude());
        } catch (JSONException e) {
            System.out.println("Please check key-value pairs.");
        }
        return location_info;
    }

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
                    System.out.println(e);
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

    private void mapLocationList(JSONArray location_list) {
        System.out.println("Mapping location list.");
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }// else { System.exit(1); }

        // 豊橋技術科学大学周辺を表示
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(34.701983, 137.408691), 17);
        mMap.moveCamera(cu);

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
                    public boolean onMarkerClick(final Marker marker) {
                        // 位置IDをサーバに送る。
                        System.out.println("OnMarkerClick is started.");
                        Toast.makeText(getApplicationContext(), marker.getTitle() + "が選択されました。", Toast.LENGTH_LONG).show();
                        final String query = URL + "/api/v1/board/?" +
                                "id=" + marker_id.get(marker.getId());
                        System.out.println(query);
                        Thread th_sendID = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("Thread run.");
                                try {
                                    HttpClient httpClient = new DefaultHttpClient();
                                    // リクエスト送信
                                    HttpGet httpGet = new HttpGet(query);
                                    // 取得
                                    HttpResponse httpResponse = httpClient.execute(httpGet);
                                    System.out.println("Response succeeded!");
                                    String res = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                                    String board_image = new JSONObject(res).get("board_image").toString();
                                    board_url = new JSONObject(board_image).get("url").toString();
                                    System.out.println(URL + board_url);
                                } catch (Exception ex) {
                                    System.out.println(ex);
                                }
                                System.out.println("Thread end.");
                            }
                        });
                        th_sendID.start();
                        // スレッドの終了を待つ
                        try {
                            th_sendID.join();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        // 描画画面へ遷移
                        final Intent intent_draw = new Intent(MainActivity.this, DrawActivity.class);
                        intent_draw.putExtra("url", URL + board_url);
                        intent_draw.putExtra("query", query);

                        try {
                            startActivity(intent_draw);
                        } catch (Exception ex) {
                            System.out.println(ex);
                        }
                        return false;
                    }
                });
    }
}
