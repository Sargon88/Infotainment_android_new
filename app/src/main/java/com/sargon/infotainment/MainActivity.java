package com.sargon.infotainment;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.sargon.infotainment.service.ConnectionService;
import com.sargon.infotainment.service.MainService;
import com.sargon.infotainment.constants.Params;
import com.sargon.infotainment.settings.SettingsActivity;

import static android.app.NotificationManager.IMPORTANCE_LOW;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private Context context;
    private MainService mService;
    private ConnectionService cService;
    private Boolean isConnected = false;
    private Boolean connecting = false;
    private int retryConnection = 0;

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
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.ANSWER_PHONE_CALLS}, 1);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Load Settings
        Log.d(TAG, "Loading Settings");
        mService = new MainService(context);
        mService.loadApplicationSettings();

        //create interface
        Log.d(TAG, "Loading Interface");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPadding(0, 56, 0, 0);
        toolbar.setTitleTextColor(getResources().getColor(R.color.textColor));
        setSupportActionBar(toolbar);

        //start services
        Log.d(TAG, "Start Services");
        connectToServer();
    }

    @Override
    protected void onStart(){
        super.onStart();

        //background management
        ConstraintLayout lay = findViewById(R.id.coordinatorLayout);
        lay.setBackgroundResource(R.drawable.background);
        AnimationDrawable backgroundAnimation = (AnimationDrawable) lay.getBackground();
        backgroundAnimation.start();
        //background management
    }

    private void connectToServer(){
        Log.d(TAG, "Connect To Server");
        if(cService == null){
            cService = new ConnectionService(context);
        } else {
            mService.killServices();
            cService.disconnect(null);
        }

        Log.i(TAG, "Socket Disconnected. Trying Connect");
        TextView connStatusView = findViewById(R.id.line_1);
        TextView ipView = findViewById(R.id.line_2);

        connStatusView.setText(R.string.connecting);
        ipView.setText(Params.SOCKET_ADDRESS);
        connecting = true;

        cService.connect(new ConnectionService.ConnectionListener(){

            @Override
            public void onConnected() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        retryConnection = 0;
                        Log.i(TAG, "CONNECTED");
                        TextView connStatusView = findViewById(R.id.line_1);
                        connStatusView.setText(R.string.connected);
                        TextView ipView = findViewById(R.id.line_2);
                        ipView.setText(Params.SOCKET_ADDRESS);
                        TextView line3 = findViewById(R.id.line_3);
                        line3.setText("");
                        isConnected = true;
                        connecting = false;

                        mService.startBgServices();
                    }
                });


            }

            @Override
            public void onDisconnected(String message) {
                Log.i(TAG, "ON DISCONNECTED");

                retryConnection ++;


                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                NotificationManager nm = context.getSystemService(NotificationManager.class);
                nm.cancel(Params.NOTIFICATION_ID);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Params.CONNECTION_CHANNEL_ID)
                        .setChannelId(Params.CONNECTION_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.tachikoma_launcher_foreground)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setContentIntent(pendingIntent)
                        .setContentTitle("Disconnected")
                        .setContentText(message);


                // notificationId is a unique int for each notification that you must define
                nm.notify(Params.NOTIFICATION_ID, builder.build());

                if(retryConnection >= Params.MAX_CONNECTION_RETRY){
                    mService.killServices();
                    nm.cancelAll();
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        TextView ipView = findViewById(R.id.line_3);
                        ipView.setText(message);
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
                        TextView ipView = findViewById(R.id.line_2);
                        connStatusView.setText(R.string.error);
                        ipView.setText("");
                        isConnected = false;
                        connecting = false;

                        mService.killServices();
                        NotificationManager nm = getSystemService(NotificationManager.class);
                        nm.cancelAll();
                    }
                });


            }
        });

    }


    /** MENU **/
    /**
     * Gets called every time the user presses the menu button.
     * Use if your menu is dynamic.
     */
    private static final int MENU_CONNECT = Menu.FIRST + 1;
    private static final int MENU_DISCONNECT = Menu.FIRST + 2;
    private static final int MENU_SETTINGS = Menu.FIRST;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        menu.add(0, Menu.FIRST, Menu.NONE, R.string.action_settings);

        if(isConnected || connecting){
            menu.add(0, MENU_DISCONNECT, Menu.NONE, R.string.action_disconnect);
        }
        if(!isConnected && !connecting){
            menu.add(0, MENU_CONNECT, Menu.NONE, R.string.action_connect);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == MENU_SETTINGS) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if(id == MENU_DISCONNECT){
            cService.disconnect(new ConnectionService.ConnectionListener(){

                @Override
                public void onConnected() { }

                @Override
                public void onDisconnected(String message) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            if(isConnected || connecting) {
                                Log.i(TAG, "OnClick Callback - DISCONNECTED");
                                TextView connStatusView = findViewById(R.id.line_1);
                                connStatusView.setText(R.string.not_connected);
                                TextView ipView = findViewById(R.id.line_2);
                                ipView.setText("");
                                TextView line3 = findViewById(R.id.line_3);
                                line3.setText("");
                                isConnected = false;
                                connecting = false;

                                Intent intent = new Intent(context, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                                NotificationManager nm = context.getSystemService(NotificationManager.class);
                                nm.cancel(Params.NOTIFICATION_ID);
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Params.CONNECTION_CHANNEL_ID)
                                        .setChannelId(Params.CONNECTION_CHANNEL_ID)
                                        .setSmallIcon(R.mipmap.tachikoma_launcher_foreground)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setAutoCancel(false)
                                        .setOngoing(true)
                                        .setContentIntent(pendingIntent)
                                        .setContentTitle("Disconnected")
                                        .setContentText("Disconnected");


                                // notificationId is a unique int for each notification that you must define
                                nm.notify(Params.NOTIFICATION_ID, builder.build());

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

        } else if(id == MENU_CONNECT){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

            String ip = sharedPref.getString(SettingsActivity.KEY_RASPBERRY_IP,"Ip Here");
            String port = sharedPref.getString(SettingsActivity.KEY_RASPBERRY_PORT,"Port Here");


            Params.RASPBERRY = ip;
            Params.SOCKET_PORT = port;
            Params.SOCKET_ADDRESS = "http://"+ ip + ":" + port;

            connectToServer();

        }

        return super.onOptionsItemSelected(item);
    }

    private int getStatusBarHeight() {
        int height;

        Resources myResources = getResources();
        int idStatusBarHeight = myResources.getIdentifier(
                "status_bar_height", "dimen", "android");
        if (idStatusBarHeight > 0) {
            height = getResources().getDimensionPixelSize(idStatusBarHeight);
            Toast.makeText(this,
                    "Status Bar Height = " + height,
                    Toast.LENGTH_LONG).show();
        }else{
            height = 0;
            Toast.makeText(this,
                    "Resources NOT found",
                    Toast.LENGTH_LONG).show();
        }

        return height;
    }

}
