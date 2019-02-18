package com.sargon.infotainment.service;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sargon.infotainment.constants.Params;
import com.sargon.infotainment.settings.SettingsActivity;

public class MainService extends Application{

    private Context context;
    private String TAG = MainService.class.getSimpleName();
    SharedPreferences sharedPref;
    private PhoneStateService phoneStateService;
    Intent psServiceIntent;

    public MainService(Context c){
        context = c;
    }

    public void loadApplicationSettings(){
        Log.d(TAG, "Start load Application");
        initializeParams();
    }

    private void initializeParams() {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

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

    public void startBgServices(){
        Log.i(TAG, "Start Background Services");


        phoneStateService = new PhoneStateService(context);
        psServiceIntent = new Intent(context, phoneStateService.getClass());

        if(!isMyServiceRunning(phoneStateService.getClass(), context)){
            Log.i(TAG, "Starting PhoneStateService");
            context.startForegroundService(psServiceIntent);
        }

        /*
        Log.i(TAG, "Raspbian Socket connected - Start SocketEventsListenersService");
        socketEventsListeners = new SocketEventsListenersService(context);
        socketEventsLisIntent = new Intent(context, socketEventsListeners.getClass());

        if(!isMyServiceRunning(socketEventsListeners.getClass(), context)){
            Log.i(TAG, "Starting SocketEventsListenersService");
            context.startForegroundService(socketEventsLisIntent);
        }
        */
    }
    public void killServices(){
        Log.i(TAG, "Kill Background Services");

        Intent mainServiceIntent = new Intent(context, MainService.class);
        context.stopService(mainServiceIntent);

        Intent phoneStateServiceIntent = new Intent(context, PhoneStateService.class);
        context.stopService(phoneStateServiceIntent);

        /*
        try {
            SocketSingleton.getInstance().getSocket().close();
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
        */
    }


    public boolean isMyServiceRunning(Class<?> serviceClass, Context c){
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())){

                Log.i(TAG,"is " + serviceClass.getName() + " Running? "+ true);
                return true;
            }
        }

        Log.i(TAG,"is " + serviceClass.getName() + " Running? "+false);
        return false;
    }
}
