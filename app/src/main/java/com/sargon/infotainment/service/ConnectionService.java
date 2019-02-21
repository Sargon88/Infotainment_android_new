package com.sargon.infotainment.service;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.sargon.infotainment.constants.SocketEvents;
import com.sargon.infotainment.constants.SocketSingleton;

import java.lang.reflect.Method;
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
            }).on(SocketEvents.answerCall_event, new Emitter.Listener() {
                private static final String TAG = "answerCall";

                @Override
                public void call(Object... args) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            String phoneNumber = (String) args[0];
                            vibrate();
                            Log.i(TAG, phoneNumber);

                            TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);


                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            tm.acceptRingingCall();

                        }
                    });
                }
            }).on(SocketEvents.endCall_event, new Emitter.Listener() {
                private static final String TAG = "rejectCall";

                @Override
                public void call(Object... args) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @RequiresApi(api = 28)
                        @Override
                        public void run() {
                            String phoneNumber = (String) args[0];
                            Log.i(TAG, phoneNumber);

                            vibrate();

                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Class c = Class.forName(tm.getClass().getName());
                                Method m = c.getDeclaredMethod("getITelephony");
                                m.setAccessible(true);
                                ITelephony telephonyService = (ITelephony) m.invoke(tm);

                                telephonyService.silenceRinger();
                                telephonyService.endCall();


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }).on(SocketEvents.getStatus_event, new Emitter.Listener() {
                private static final String TAG = "getStatus";

                @Override
                public void call(Object... args) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            String statusJson = PhoneStatusDataBuilder.getInstance().collectStatusData(context);
                            Log.d(TAG, statusJson);

                            try {
                                SocketSingleton.sendDataToRaspberry(SocketEvents.phoneStatus_event, statusJson);

                            } catch (URISyntaxException e) {
                                Log.e(TAG, e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }).on(SocketEvents.startPhoneCall_event, new Emitter.Listener() {
                private static final String TAG = "startPhoneCall";

                @Override
                public void call(Object... args) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            String phoneNumber = (String) args[0];
                            vibrate();
                            Log.i(TAG, phoneNumber);

                            if (!TextUtils.isEmpty(phoneNumber)) {
                                if (checkPermission(Manifest.permission.CALL_PHONE)) {

                                    String dial = "tel:" + phoneNumber;
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_CALL);
                                    intent.setData(Uri.parse(dial));

                                    context.startActivity(intent);

                                }
                            } else {
                                Log.w(TAG, "No phone number");
                            }
                        }

                        private boolean checkPermission(String permission) {
                            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
                        }

                    });
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
    private void vibrate(){
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(500);
        }
    }
}
