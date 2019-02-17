package com.sargon.infotainment.Service;

import android.app.Application;
import android.util.Log;

public class MainService extends Application{

    private String TAG = MainService.class.getSimpleName();
    private ConnectionListener cListener;

    public interface ConnectionListener{
        void onConnectionChange(String message);
    }

    public void loadApplicationSettings(){
        Log.d(TAG, "Start load Application");
    }

    public void connect(ConnectionListener c){
        Log.d(TAG, "Connecting");
        this.cListener = c;

        if(this.cListener != null) {
            c.onConnectionChange("Connected");
        }

    }
}
