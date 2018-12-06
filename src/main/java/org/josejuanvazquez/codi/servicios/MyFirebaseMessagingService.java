package org.josejuanvazquez.codi.servicios;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;


import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.josejuanvazquez.codi.Codi;


/**
 * Created by José Juan Vázquez on 03/12/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getNotification() != null){
            mostrarNotificacion(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void mostrarNotificacion(String title, String body) {
        Intent intent = new Intent(this, Codi.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Log.i(this.getClass().getName(), "title: "+ title);
        Log.i(this.getClass().getName(), "body: "+ body);
        /*
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationCompat.Builder notificacionBuilder = new NotificationCompat.Builder(this);
        //notificacionBuilder.setSmallIcon(R.drawable.)
        notificacionBuilder.setContentTitle(title);
        notificacionBuilder.setContentText(body);
        notificacionBuilder.setAutoCancel(true);
        notificacionBuilder.setSound(soundUri);
        notificacionBuilder.setContentIntent(pendingIntent);
        notificacionBuilder.setOnlyAlertOnce(true);
        long[] pattern = {500, 500, 500, 500,500, 500,500, 500,500};
        notificacionBuilder.setVibrate(pattern);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificacionBuilder.build());
        */
    }

}
