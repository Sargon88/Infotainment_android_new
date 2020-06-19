package com.sargon.infotainment.service;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.telecom.Call;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.sargon.infotainment.bean.PhoneStatus;
import com.sargon.infotainment.constants.PhoneStatusSingleton;

public class PhoneStatusDataBuilder {

    private static final String TAG = PhoneStatusDataBuilder.class.getSimpleName();
    private PhoneStatus psd;
    CallPageService cpService = new CallPageService();

    private static PhoneStatusDataBuilder instance = null;

    private PhoneStatusDataBuilder(){}

    public static PhoneStatusDataBuilder getInstance(){
        if(instance == null){
            instance = new PhoneStatusDataBuilder();
        }

        return instance;
    }

    public String collectStatusData(Context c){

        updateStatusData(c);

        Gson g = new Gson();
        String json = g.toJson(psd);

        return json;
    }

    public void updateStatusData(Context c){

        if(psd == null) {
            psd = PhoneStatusSingleton.getInstance();
        }

        psd.getNavbar().setBattInt(getBatteryLevel(c));
        psd.getNavbar().setBluetooth(getBluetoothStatus());
        psd.getNavbar().setWifi(getWifiStatus(c));
        psd.getNavbar().setSignal(0);
        if(psd.getStarredContacts() == null){
            psd.setStarredContacts(cpService.getStarredContacts(c));
        }
        psd.setLastCalls(cpService.getLastCalls(c));
        

/*
        psd.setCalling();
        psd.setInCall();
        psd.setCallerId();

 */

    }

    //batteria
    private int getBatteryLevel(Context c){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = c.registerReceiver(null, ifilter);


        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return Math.round((level/(float) scale)*100);

    }

    //bluetooth
    private boolean getBluetoothStatus(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Boolean bluetoothStatus = false;

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            bluetoothStatus = false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                bluetoothStatus = false;
            } else {
                bluetoothStatus = true;
            }
        }

        return bluetoothStatus;
    }

    //wifi
    private boolean getWifiStatus(Context c){
        ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);;
        NetworkInfo wifiCheck = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean wifiStatus = false;


        if (wifiCheck.isConnectedOrConnecting()) {
            //wifi connected
            wifiStatus = true;
        } else {
            //wifi not connected
            wifiStatus = false;
        }

        return wifiStatus;
    }

}
