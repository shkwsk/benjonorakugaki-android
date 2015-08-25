package com.example.shkwsk.myapp01;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.Intent;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity implements LocationListener {

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("System start MainActivity.");

        System.out.println("onCreate()");
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
        Toast.makeText(getApplicationContext(), "位置情報を取得しています。", Toast.LENGTH_LONG).show();
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

    // LocationListenerインタフェース4つ
    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "位置情報を取得しました！", Toast.LENGTH_LONG).show();

        JSONObject location_json = getLocationInfo(location);
        System.out.println(location_json);
        Intent intent_sb = new Intent(MainActivity.this, SelectBoardActivity.class);
        intent_sb.putExtra("location_json", location_json.toString());
        try {
            startActivity(intent_sb);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        MainActivity.this.finish(); // タイトル画面にはもう戻らない
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

    // private method
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
}
