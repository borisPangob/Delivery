package com.boris.delivery.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.boris.delivery.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("Notif remote", "From: " + remoteMessage.getFrom());
        Log.e("Notif remote", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        getFirebaseMessage(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    }
    private void getFirebaseMessage(String title, String msg) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "delivery")
                .setSmallIcon(R.drawable.ic_notififcation)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("delivery",
                    "Channel for driver notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(101, builder.build());
        }

    }
    @Override
    public void onMessageSent(String messageId) {
        super.onMessageSent(messageId);
        Log.d("Notif remote", "Message sent successfully. Message ID: " + messageId);
    }

    @Override
    public void onSendError(String messageId, Exception exception) {
        super.onSendError(messageId, exception);
        Log.e("Notif remote", "Error sending message. Message ID: " + messageId, exception);
    }

}
