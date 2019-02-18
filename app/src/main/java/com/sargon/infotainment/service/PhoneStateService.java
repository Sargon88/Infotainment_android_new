package com.sargon.infotainment.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class PhoneStateService extends Service {
    private static String TAG= PhoneStateService.class.getSimpleName();
    private static Context c;

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
}
