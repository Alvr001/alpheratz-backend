package com.alpheratz.notification;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

@Service
public class FCMService {

    public void sendNotification(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .build();
            FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Notificación enviada a: " + fcmToken.substring(0, 20) + "...");
        } catch (FirebaseMessagingException e) {
            System.err.println("❌ Error FCM: " + e.getMessage());
        }
    }

    public void sendToMultiple(List<String> tokens, String title, String body) {
        if (tokens == null || tokens.isEmpty()) return;
        try {
            MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .build();
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            System.out.println("✅ Enviadas: " + response.getSuccessCount() +
                               " Fallidas: " + response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            System.err.println("❌ Error FCM multicast: " + e.getMessage());
        }
    }
}