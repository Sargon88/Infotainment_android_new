package com.sargon.infotainment.constants;

import android.util.Log;

import com.sargon.infotainment.bean.PhoneStatus;

public class PhoneStatusSingleton {

    private static final String TAG = PhoneStatusSingleton.class.getSimpleName();
    private static PhoneStatus instance = null;

    public static PhoneStatus getInstance(){
        Log.d(TAG, "getInstance");

        if(instance == null){
            Log.d(TAG, "New PhoneStatus instance");
            instance = new PhoneStatus();
        }

        return instance;
    }
    public void clearInstance() {
        instance = null;
    }

}
