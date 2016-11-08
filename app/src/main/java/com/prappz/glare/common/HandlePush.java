package com.prappz.glare.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.parse.ParsePushBroadcastReceiver;
import com.prappz.glare.R;
import com.prappz.glare.common.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by RRaju on 6/10/2015.
 */

public class HandlePush extends ParsePushBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ParsePushBroadcastReceiver.ACTION_PUSH_RECEIVE)) {
            try {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                analyzeJson(json, context);
            } catch (JSONException e) {
            }
        }
    }

    private void analyzeJson(final JSONObject json, final Context context) {
        try {
            int type = json.getInt("type");
            //0 for ride request
            if (type == AppConstants.R_ASSIGN_AMBULANCE) {
                generate_message_notification(context, "You have a new emergency situation to handle with!!!", json.getString("id"), json.getInt("type"));
            }
            //1 for accept
            else if (type == 1) {
                String from = json.getString("from");
             /*   String source = json.getString("source");
                String desti = json.getString("desti");
                String url = json.getString("url");
                String id = json.getString("id");*/

                //     createRequest(id, from, type, url, source, desti, null, json.getString("phone"));
                //createRequest(id, from + " accepted your share request for the ride you posted from " + source + " to " + desti, type, url, source, desti);
                generate_notification(context, from + " accepted your share request");
            }
            //2 for reject
            else if (type == 2) {
                String from = json.getString("from");
              /*  String source = json.getString("source");
                String desti = json.getString("desti");
                String url = json.getString("url");
                String id = json.getString("id");*/
                //     createRequest(id, from, type, url, source, desti,null,json.getString("phone"));
                //createRequest(id, from + " rejected your share request for the ride you posted from " + source + " to " + desti, type, url, source, desti);
                generate_notification(context, from + " rejected your share request");
            } else if (type == 3) {
                String from = json.getString("from");
                String id = json.getString("id");
                //generate_message_notification(context, from + " sent you a new message", from, id);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void generate_notification(Context context, String content) throws JSONException {
        Intent i = new Intent(context, MainActivity.class);
        PendingIntent pintent = PendingIntent.getActivity(context, 0, i, 0);

        Notification notif = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pintent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notif);
    }

    private void generate_message_notification(Context context, String content, String id, int type) throws JSONException {
        try {


            Intent i = new Intent(context, MainActivity.class);
            i.putExtra("id", id);
            i.putExtra("type", type);
            i.putExtra("FROMNOTIF", true);
            PendingIntent pintent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notif = new NotificationCompat.Builder(context)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pintent)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .build();

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, notif);
        } catch (NullPointerException ex) {
            Log.i("RESP", ex.getLocalizedMessage());
        }
    }

}

