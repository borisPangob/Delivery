package com.boris.delivery.service;

import android.os.Message;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class FirebaseNotificationSender {
    public FirebaseNotificationSender(){}
    public static void sendNotificationToTopic(String topic, String title, String message) {
        try {
            // Construction du contenu du message
            Map<String, String> messageData = new HashMap<>();
            messageData.put("title", title);
            messageData.put("body", message);

            // Envoie du message au topic spécifié
            FirebaseMessaging.getInstance().send(new RemoteMessage.Builder("199842494584"+"@fcm.googleapis.com")
                    .setData(messageData)
                    .build());
            Log.d("Notif remote", "Notification envoyée avec succès au topic: " + topic);
        } catch (Exception e) {
            Log.d("Notif remote", "Notification non envoyée au topic: " + topic);
            throw new RuntimeException(e);
        }
    }
}
