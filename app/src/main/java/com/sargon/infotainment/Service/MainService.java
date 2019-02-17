package com.sargon.infotainment.Service;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sargon.infotainment.constants.Params;
import com.sargon.infotainment.settings.SettingsActivity;

public class MainService extends Application{

    private Context context;
    private String TAG = MainService.class.getSimpleName();

    public MainService(Context c){
        context = c;
    }

    public void loadApplicationSettings(){
        Log.d(TAG, "Start load Application");
        initializeParams();
    }

    private void initializeParams() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String ip = sharedPref.getString(SettingsActivity.KEY_RASPBERRY_IP,"Ip Here");
        String port = sharedPref.getString(SettingsActivity.KEY_RASPBERRY_PORT,"Port Here");
        String status = sharedPref.getString(SettingsActivity.KEY_PHONE_STATUS_TASK_FREQUENCE,"20000");
        String coordinates = sharedPref.getString(SettingsActivity.KEY_COORDINATES_TASK_FREQUENCE,"1000");
        String reconnect = sharedPref.getString(SettingsActivity.KEY_TRY_CONNECT_TASK_FREQUENCE,"30000");
        String delay = sharedPref.getString(SettingsActivity.KEY_TASK_DELAY,"1000");
        String retry = sharedPref.getString(SettingsActivity.KEY_MAX_CONN,"5");


        Params.RASPBERRY = ip;
        Params.SOCKET_PORT = port;
        Params.SOCKET_ADDRESS = "http://"+ ip + ":" + port;
        Params.PHONE_STATUS_TASK_FREQUENCE = Integer.parseInt(status);
        Params.COORDINATES_TASK_FREQUENCE = Integer.parseInt(coordinates);
        Params.TRY_CONNECT_TASK_FREQUENCE = Integer.parseInt(reconnect);
        Params.TASK_DELAY = Integer.parseInt(delay);
        Params.MAX_CONNECTION_RETRY = Integer.parseInt(retry);

    }
}
