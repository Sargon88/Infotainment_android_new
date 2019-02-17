package com.sargon.infotainment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sargon.infotainment.Service.MainService;
import com.sargon.infotainment.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private Context context;
    private MainService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        Log.i(TAG, "Application started");
        Activity a = (Activity) context;

        //Load Settings
        Log.d(TAG, "Loading Settings");

        //create interface
        Log.d(TAG, "Loading Interface");
        setContentView(R.layout.activity_main);

        //start services
        Log.d(TAG, "Start Services");
        mService = new MainService();
        mService.loadApplicationSettings();

        Button btn_connect = (Button)findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.connect(new MainService.ConnectionListener(){

                    @Override
                    public void onConnectionChange(String message) {
                        Log.i(TAG, "OnClick Callback: " + message);
                        TextView connStatusView = findViewById(R.id.conn_status);

                        connStatusView.setText(message);
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
        }

        return super.onOptionsItemSelected(item);
    }

}
