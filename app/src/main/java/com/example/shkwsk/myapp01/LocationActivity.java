package com.example.shkwsk.myapp01;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class LocationActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private TextView textView;
    private String text = "start\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.text_view);

        text += "onCreate()\n";
        textView.setText(text);

        // LocationManager インスタンス生成
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // GPSを設定するように促す
            enableLocationSettings();
        }
    }

    @Override
    protected void onResume() {
        text += "onResume()\n";
        textView.setText(text);

        if (locationManager != null) {
            // minTime = 1000msec, minDistance = 50m
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        else{
            text += "locationManager=null\n";
            textView.setText(text);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {

        if (locationManager != null) {
            // update を止める
            locationManager.removeUpdates(this);
        }
        else{
            text += "onPause()\n";
            textView.setText(text);
        }

        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        text += "----------\n";
        text += "Latitude="+ String.valueOf(location.getLatitude())+"\n";
        text += "Longitude="+ String.valueOf(location.getLongitude())+"\n";

        // Get the estimated accuracy of this location, in meters.
        // We define accuracy as the radius of 68% confidence. In other words,
        // if you draw a circle centered at this location's latitude and longitude,
        // and with a radius equal to the accuracy, then there is a 68% probability
        // that the true location is inside the circle.
        text += "Accuracy="+ String.valueOf(location.getAccuracy())+"\n";

        text += "Altitude="+ String.valueOf(location.getAltitude())+"\n";
        text += "Time="+ String.valueOf(location.getTime())+"\n";
        text += "Speed="+ String.valueOf(location.getSpeed())+"\n";

        // Get the bearing, in degrees.
        // Bearing is the horizontal direction of travel of this device,
        // and is not related to the device orientation.
        // It is guaranteed to be in the range (0.0, 360.0] if the device has a bearing.
        text += "Bearing="+ String.valueOf(location.getBearing())+"\n";
        text += "----------\n";

        textView.setText(text);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                text += "LocationProvider.AVAILABLE\n";
                textView.setText(text);

                break;
            case LocationProvider.OUT_OF_SERVICE:
                text += "LocationProvider.OUT_OF_SERVICE\n";
                textView.setText(text);
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                text += "LocationProvider.TEMPORARILY_UNAVAILABLE\n";
                textView.setText(text);
                break;
        }
    }

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }
}