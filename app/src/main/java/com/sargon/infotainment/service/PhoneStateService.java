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
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sargon.infotainment.R;
import com.sargon.infotainment.bean.PhoneStatus;
import com.sargon.infotainment.constants.Params;
import com.sargon.infotainment.constants.PhoneStatusSingleton;
import com.sargon.infotainment.constants.SocketEvents;
import com.sargon.infotainment.constants.SocketSingleton;

import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class PhoneStateService extends Service {
    private static String TAG= PhoneStateService.class.getSimpleName();
    private Context c;
    private static PhoneStatus phoneStatus;

    public PhoneStateService(){}

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
        startTimer();

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

    private Timer timer;
    public void startTimer(){
        Log.d(TAG, "startTimer");
        timer = new Timer();

        initializeStatusTask();
        timer.schedule(statusTask, Params.TASK_DELAY, Params.PHONE_STATUS_TASK_FREQUENCE);

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

                //sendMessage();

                try {

                    Gson g = new Gson();
                    String json = g.toJson(PhoneStatusSingleton.getInstance());

                    SocketSingleton.sendDataToRaspberry(SocketEvents.phoneStatus_event, json);

                } catch (URISyntaxException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
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

        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, lListener);
        lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, lListener);

    }

    //status
    private TimerTask statusTask;
    public void initializeStatusTask(){
        statusTask = new TimerTask() {
            @Override
            public void run() {
                sendMessage();
            }
        };
    }

    private void sendMessage(){
        if(Looper.myLooper() == null){
            Looper.prepare();
        }

        //sarebbe opportuno facesse solo l'invio e non l'update...
        //si potrebbe temporizzare l'update e poi fare l'invio in questo caso
        String statusJson = PhoneStatusDataBuilder.getInstance().collectStatusData(getApplicationContext());

        try {
            SocketSingleton.sendDataToRaspberry(SocketEvents.phoneStatus_event, statusJson);

        } catch (URISyntaxException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }
}
