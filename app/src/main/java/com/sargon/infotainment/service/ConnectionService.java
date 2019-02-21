package com.sargon.infotainment.service;

import android.content.Context;
import android.util.Log;

import com.sargon.infotainment.constants.SocketSingleton;

import java.net.URISyntaxException;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectionService {

    private String TAG = ConnectionService.class.getSimpleName();
    private ConnectionListener cListener;
    private Context context;

    public ConnectionService(Context c){
        context = c;
    }

    public interface ConnectionListener{
        void onConnected();
        void onDisconnected();
        void onError(String message);
    }

    public void connect(ConnectionListener c){
        Log.d(TAG, "Connecting");
        this.cListener = c;

        try {
            SocketSingleton.setContext(context);
            Socket socket = SocketSingleton.getInstance().getSocket();

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d(TAG, "CONNECTED");
                    eventConnectedAction();
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    eventDisconnectedAction();
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    eventConnectionErrorAction();
                }
            }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    eventConnectionErrorAction();
                }
            });

            SocketSingleton.connect();

        } catch (Exception e){
            Log.e(TAG, e.getLocalizedMessage());
            cListener.onError(e.toString());
        }


    }
    public void disconnect(ConnectionListener c){
        Log.d(TAG, "Disconnecting");
        this.cListener = c;

        try {
            SocketSingleton.disconnect();

        } catch(Exception e){
            Log.e(TAG, e.toString());
        }

        if(this.cListener != null) {
            c.onDisconnected();
        }

    }

    /** SOCKET Event Management **/
    private void eventConnectedAction(){
        try {
            SocketSingleton.getInstance().setConnected(true);
            Log.i(TAG, "SOCKET CONNECTED");
            cListener.onConnected();

        } catch (URISyntaxException e) {
            Log.e(TAG, e.getLocalizedMessage());
            cListener.onError(e.getLocalizedMessage());
        }
    }
    private void eventDisconnectedAction(){
        try {
            SocketSingleton.getInstance().setConnected(false);
            Log.i(TAG, "SOCKET DISCONNECTED");
            cListener.onDisconnected();

        } catch (URISyntaxException e) {
            Log.e(TAG, e.getLocalizedMessage());
            cListener.onError(e.getLocalizedMessage());
        }
    }
    private void eventConnectionErrorAction() {
        try {
            SocketSingleton.getInstance().setConnected(false);
            Log.i(TAG, "SOCKET DISCONNECTED");
            cListener.onDisconnected();

        } catch (URISyntaxException e) {
            Log.e(TAG, e.getLocalizedMessage());
            cListener.onError(e.getLocalizedMessage());
        }
    }
}
