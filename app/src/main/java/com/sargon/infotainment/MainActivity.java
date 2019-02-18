package com.sargon.infotainment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.sargon.infotainment.service.ConnectionService;
import com.sargon.infotainment.service.MainService;
import com.sargon.infotainment.constants.Params;
import com.sargon.infotainment.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private Context context;
    private MainService mService;
    private ConnectionService cService;
    private Boolean isConnected = false;
    private String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        Log.i(TAG, "Application started");
        Activity a = (Activity) context;

        Log.d(TAG, "verifyPermissions");
        ActivityCompat.requestPermissions(a,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ANSWER_PHONE_CALLS}, 1);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Load Settings
        Log.d(TAG, "Loading Settings");
        mService = new MainService(context);
        mService.loadApplicationSettings();

        //create interface
        Log.d(TAG, "Loading Interface");
        setContentView(R.layout.activity_main);

        //start services
        Log.d(TAG, "Start Services");
        initializeListeners();

    }

    private void initializeListeners(){
        cService = new ConnectionService();

        Log.i(TAG, "Socket Disconnected. Trying Connect");
        TextView connStatusView = findViewById(R.id.line_1);
        TextView ipView = findViewById(R.id.line_2);

        connStatusView.setText(R.string.connecting);
        ipView.setText(Params.SOCKET_ADDRESS);

        cService.connect(new ConnectionService.ConnectionListener(){

            @Override
            public void onConnected() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        Log.i(TAG, "CONNECTED");
                        TextView connStatusView = findViewById(R.id.line_1);
                        connStatusView.setText(R.string.connected);
                        isConnected = true;

                        mService.startBgServices();
                    }
                });


            }

            @Override
            public void onDisconnected() { }

            @Override
            public void onError(String message) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        Log.i(TAG, "OnClick Callback - ERROR");
                        TextView connStatusView = findViewById(R.id.line_1);
                        connStatusView.setText(R.string.error);
                        isConnected = false;

                        mService.killServices();
                    }
                });


            }
        });
    }


    /** MENU **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if(id == R.id.action_disconnect){
            cService.disconnect(new ConnectionService.ConnectionListener(){

                @Override
                public void onConnected() { }

                @Override
                public void onDisconnected() {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            if(isConnected) {
                                Log.i(TAG, "OnClick Callback - DISCONNECTED");
                                TextView connStatusView = findViewById(R.id.line_1);
                                connStatusView.setText(R.string.not_connected);
                                isConnected = false;

                                mService.killServices();
                            }

                        }
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            Log.i(TAG, "OnClick Callback - ERROR");
                            TextView connStatusView = findViewById(R.id.line_1);
                            connStatusView.setText(R.string.error);
                            isConnected = false;
                            mService.killServices();
                        }
                    });
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

}
