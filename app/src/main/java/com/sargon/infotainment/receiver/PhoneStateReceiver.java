package com.sargon.infotainment.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.sargon.infotainment.R;
import com.sargon.infotainment.bean.CallBean;
import com.sargon.infotainment.bean.PhoneStatus;
import com.sargon.infotainment.constants.PhoneStatusSingleton;
import com.sargon.infotainment.constants.SocketSingleton;
import com.sargon.infotainment.service.PhoneStatusDataBuilder;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhoneStateReceiver extends BroadcastReceiver {

    private static String TAG = "PhoneStateReceiver";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber = "Unknown Number";
    private static Context context;
    private static Intent intent;

    @Override
    public void onReceive(Context inputContext, Intent inputIntent) {
         Log.i("ccc1 " + TAG, inputIntent.getAction());

         if(inputContext != null){
             Log.i("ccc1 " + TAG, "UPDATE CONTEXT");
             context = inputContext;
         }

         if(inputIntent != null){
             Log.i("ccc1 " + TAG, "UPDATE INTENT");
             intent = inputIntent;
         }

         if(intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")){
             savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
             Log.i("ccc1 " + TAG, "Entered android.intent.action.NEW_OUTGOING_CALL: " + savedNumber);

             String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);

         } else if(intent.getAction().equals("android.intent.action.PHONE_STATE")) {

             /*
             String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
             String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
             Log.i("ccc1 " + TAG, "State: " + stateStr);

             int state = 0;

             if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                 Log.i("ccc1 " + TAG, "Entered " + TelephonyManager.EXTRA_STATE_IDLE);
                 state = TelephonyManager.CALL_STATE_IDLE;
             } else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                 Log.i("ccc1 " + TAG, "Entered " + TelephonyManager.EXTRA_STATE_OFFHOOK);
                 state = TelephonyManager.CALL_STATE_OFFHOOK;
             } else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                 Log.i("ccc1 " + TAG, "Entered " + TelephonyManager.EXTRA_STATE_RINGING);
                 state = TelephonyManager.CALL_STATE_RINGING;
             }

             onCallStateChanged(state, number);
             */
         }


         TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
         telephony.listen(new PhoneStateListener(){
             @Override
             public void onCallStateChanged(int state, String number) {
                 super.onCallStateChanged(state, number);

                 if(lastState == state){
                     Log.i("ccc " + TAG, "No Change");
                     //No change, debounce extras
                     return;
                 }

                 Log.i("ccc " + TAG, "State: " + state);
                 try{
                     switch (state) {

                         case TelephonyManager.CALL_STATE_RINGING:
                             isIncoming = true;
                             callStartTime = new Date();
                             if(number == null || number == ""){
                                 number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                                 if(number == "" || number == null){
                                     number = "Unknown Number";
                                 }
                             }

                             savedNumber = number;
                             onIncomingCallStarted(context, number, callStartTime);
                             break;
                         case TelephonyManager.CALL_STATE_OFFHOOK:
                             //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                             if(lastState != TelephonyManager.CALL_STATE_RINGING){
                                 isIncoming = false;
                                 callStartTime = new Date();
                                 onOutgoingCallStarted(context, savedNumber, callStartTime);
                             } else {

                                 onIncomingCallAnswer(context, savedNumber, callStartTime);

                             }
                             break;
                         case TelephonyManager.CALL_STATE_IDLE:

                             try {
                                 if(number == null){
                                     number = "";
                                 }
                                 SocketSingleton.getInstance().sendDataToRaspberry("call end", number);
                                 PhoneStatusDataBuilder.getInstance().updateStatusData(context);

                             } catch (URISyntaxException e) {
                                 e.printStackTrace();
                             }

                             //Useless at this time
                             //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                             if(lastState == TelephonyManager.CALL_STATE_RINGING){
                                 //Ring but no pickup-  a miss
                                 onMissedCall(context, savedNumber, callStartTime);
                             }
                             else if(isIncoming){
                                 onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                             }
                             else{
                                 onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                             }
                             break;
                     }
                 } catch (URISyntaxException e) {
                     Log.e(TAG, e.getLocalizedMessage());
                 }
                 lastState = state;
             }
         }, PhoneStateListener.LISTEN_CALL_STATE);



    }

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(int state, String number) {

        Log.i("ccc " + TAG, "ENTERED onCallStateChanged");

        if(lastState == state){
            Log.i("ccc " + TAG, "No Change");
            //No change, debounce extras
            return;
        }

        Log.i("ccc " + TAG, "State: " + state);
        try{
            switch (state) {

                case TelephonyManager.CALL_STATE_RINGING:
                    isIncoming = true;
                    callStartTime = new Date();
                    if(number == null || number == ""){
                        number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                        if(number == "" || number == null){
                            number = "Unknown Number";
                        }
                    }

                    savedNumber = number;
                    onIncomingCallStarted(context, number, callStartTime);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                    if(lastState != TelephonyManager.CALL_STATE_RINGING){
                        isIncoming = false;
                        callStartTime = new Date();
                        onOutgoingCallStarted(context, savedNumber, callStartTime);
                    } else {

                        onIncomingCallAnswer(context, savedNumber, callStartTime);

                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:

                    try {
                        if(number == null){
                            number = "";
                        }
                        SocketSingleton.getInstance().sendDataToRaspberry("call end", number);
                        PhoneStatusDataBuilder.getInstance().updateStatusData(context);

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    //Useless at this time
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if(lastState == TelephonyManager.CALL_STATE_RINGING){
                        //Ring but no pickup-  a miss
                        onMissedCall(context, savedNumber, callStartTime);
                    }
                    else if(isIncoming){
                        onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                    }
                    else{
                        onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                    }
                    break;
            }
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        lastState = state;
    }


    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context ctx, String number, Date start) throws URISyntaxException {
        Log.i("ccc " + TAG, "ENTERED onIncomingCallStarted");

        String json = getExtraCallData(ctx, number, "in");
        PhoneStatus status = PhoneStatusSingleton.getInstance();
        status.setInCall(true);
        status.setCalling(false);
        status.setCallerId(json);


        SocketSingleton.getInstance().sendDataToRaspberry("incoming calling", json);
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start) throws URISyntaxException {
        Log.i("ccc " + TAG, "ENTERED onIncomingCallStarted");

        String msg = getExtraCallData(ctx, number, "out");
        PhoneStatus status = PhoneStatusSingleton.getInstance();
        status.setInCall(true);
        status.setCalling(false);
        status.setCallerId(msg);

        SocketSingleton.getInstance().sendDataToRaspberry("outgoing calling", msg);
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){
        Log.i("ccc " + TAG, "ENTERED onIncomingCallEnded");

        PhoneStatus status = PhoneStatusSingleton.getInstance();
        status.setInCall(false);
        status.setCalling(false);
        status.setCallerId("");
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){
        Log.i("ccc " + TAG, "ENTERED onOutgoingCallEnded");
        PhoneStatus status = PhoneStatusSingleton.getInstance();
        status.setInCall(false);
        status.setCalling(false);
        status.setCallerId("");
    }

    protected void onMissedCall(Context ctx, String number, Date start){
        Log.i("ccc " + TAG, "ENTERED onMissedCall");
    }

    private void onIncomingCallAnswer(Context ctx, String number, Date start) throws URISyntaxException {
        Log.i("ccc " + TAG, "ENTERED onIncomingCallAnswer");

        PhoneStatus status = PhoneStatusSingleton.getInstance();
        status.setCalling(true);
        SocketSingleton.getInstance().sendDataToRaspberry("call answer", number);

    }



    /** UTILITY */
    private String getExtraCallData(Context c, String incomingNumber, String type){

        Log.d("ccc " + TAG, "incoming Number: " + incomingNumber + " - type: " + type);

        //inizio
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
        String name = "";
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        String base64Image = "";
        Long contactId;
        Bitmap photo = BitmapFactory.decodeResource(c.getResources(), R.drawable.default_user);

        ContentResolver contentResolver = c.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {
                BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                contactId = contactLookup.getLong(contactLookup.getColumnIndex(BaseColumns._ID));
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //date = contactLookup.getString((contactLookup.getColumnIndex(ContactsContract.Data.LAST_TIME_CONTACTED)));

            }


        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        // fine

        CallBean cb = new CallBean(name, incomingNumber, date, type);
        Gson g = new Gson();
        String json = g.toJson(cb);

        Log.d("ccc " + TAG, json);

        return json;
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
        /*
         * This functions converts Bitmap picture to a string which can be
         * JSONified.
         * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        Log.i(TAG, "image: " + encodedImage);

        return encodedImage;
    }
}
