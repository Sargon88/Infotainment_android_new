package com.sargon.infotainment.constants;

import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketSingleton {

    private static final String TAG = SocketSingleton.class.getSimpleName();

    private static SocketSingleton instance = null;
    private Socket socket;
    private boolean connected = false;


    private SocketSingleton() throws URISyntaxException {

        configureSocket();
        connected = false;
    }

    public void configureSocket() throws URISyntaxException {
        Log.d(TAG, "Configure Socket");
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

        if(!Params.DEBUG) {
            getInstance().socket.emit(action, params);
        }

    }

    public static void connect(){
        Log.d(TAG, "Socket.Connect");
        instance.socket.connect();
    }
    public static void disconnect(){
        instance.socket.disconnect();
        instance.connected = false;

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

}
