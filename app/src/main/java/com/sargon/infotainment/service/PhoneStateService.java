package com.sargon.infotainment.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sargon.infotainment.bean.PhoneStatus;
import com.sargon.infotainment.constants.PhoneStatusSingleton;

public class PhoneStateService extends Service {
    private static String TAG= PhoneStateService.class.getSimpleName();
    private static Context c;
    private static PhoneStatus phoneStatus;

    public PhoneStateService(){}
    public PhoneStateService(Context applicationContext){
        super();
        c = applicationContext; //sempre per primo

        Log.d(TAG, "PhoneStateService");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "On Create");
        startForeground(1, new Notification());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        Log.d(TAG, "On Start Command: Build status Object");
        phoneStatus = PhoneStatusSingleton.getInstance();

        getGpsCoordinates();

        return START_STICKY;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();

        Log.i(TAG, "EXIT, onDestroy");

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //gps
    @SuppressLint("MissingPermission")
    private void getGpsCoordinates(){
        Log.i(TAG, "getGpsCoordinates");

        LocationManager lManager = (LocationManager) getSystemService(c.LOCATION_SERVICE);

        LocationListener lListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                phoneStatus.setLatitude(Double.toString(location.getLatitude()));
                phoneStatus.setLongitude(Double.toString(location.getLongitude()));

                Log.d(TAG, "LATITUDE: " + phoneStatus.getLatitude() + " - LONGITUDE: " + phoneStatus.getLongitude());

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

        };

        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, lListener);
        lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, lListener);

    }
}
