package com.sargon.infotainment.constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sargon.infotainment.settings.SettingsActivity;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketSingleton {

    private static final String TAG = SocketSingleton.class.getSimpleName();

    private static SocketSingleton instance = null;
    private static Socket socket;
    private boolean connected = false;
    private static Context context;
    private static SharedPreferences sharedPref;


    private SocketSingleton() throws URISyntaxException {
        configureSocket();
        connected = false;
    }

    private static void loadParams(){
        Log.d(TAG, "Load Params");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String ip = sharedPref.getString(SettingsActivity.KEY_RASPBERRY_IP,"Ip Here");
        String port = sharedPref.getString(SettingsActivity.KEY_RASPBERRY_PORT,"Port Here");
        String retry = sharedPref.getString(SettingsActivity.KEY_MAX_CONN,"5");


        Params.RASPBERRY = ip;
        Params.SOCKET_PORT = port;
        Params.SOCKET_ADDRESS = "http://"+ ip + ":" + port;
        Params.MAX_CONNECTION_RETRY = Integer.parseInt(retry);
        Log.d(TAG, "Updated Params");
    }

    public static void configureSocket() throws URISyntaxException {
        Log.d(TAG, "Configure Socket");
        loadParams();

        IO.Options opt = new IO.Options();
        opt.reconnection = true;
        opt.reconnectionAttempts = Params.MAX_CONNECTION_RETRY;

        socket = IO.socket(Params.SOCKET_ADDRESS,  opt);
    }

    public static SocketSingleton getInstance() throws URISyntaxException {
        Log.d(TAG, "GetInstance");
        if(instance == null){
            Log.d(TAG, "NEW Socket");
            instance = new SocketSingleton();
        }

        return instance;
    }

    public static void sendDataToRaspberry(String action, String params) throws URISyntaxException {
        Log.i(TAG + "sendDataToRaspberry", params);

        if(getInstance().connected == false){
            getInstance().socket.connect();
        }

        getInstance().socket.emit(action, params);

    }

    public static void connect() throws URISyntaxException {
        Log.d(TAG, "Socket.Connect");
        instance.socket.connect();
    }
    public static void disconnect(){
        instance.socket.close();
        instance.socket.disconnect();
        instance.connected = false;
        instance = null;

    }

    public Socket getSocket() {
        return socket;
    }

    public static boolean isConnected() throws URISyntaxException {
        return getInstance().connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public static void setContext(Context c) {
        context = c;
    }
}
