package com.skillsearch.skillsearch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.facebook.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by COld on 20/10/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public final static String NOTIF_USER_ID = "com.example.cold.skillsearch.NOTIF_USER_ID";
    public final static String NOTIF_CHAT_ID = "com.example.cold.skillsearch.NOTIF_CHAT_ID";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            //Log.d(TAG, "Message name payload: " + remoteMessage.getData().get("title"));
            Log.d(TAG, "Message message payload: " + remoteMessage.getData().get("body"));
//            DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference().child("users")
//                    .child(remoteMessage.getData().get("title")).child("name");
//            nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                   sendNotification(dataSnapshot.getValue(String.class), remoteMessage.getData().get("body"),
//                           remoteMessage.getData().get("title"), 1);
//                }
//                @Override public void onCancelled(DatabaseError databaseError) {}
//            });

            sendNotification(remoteMessage.getData().get("body"));
        }

//        final String userId = remoteMessage.getNotification().getTitle();
//        final String message = remoteMessage.getNotification().getBody();
            //sendNotification("name", "message", "userId", 0);
    }

    private void sendNotification(final String userId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(uid).child("contacts").child(userId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent = new Intent(MyFirebaseMessagingService.this, UserMessage.class);
                intent.putExtra(NOTIF_USER_ID, dataSnapshot.getKey());
                intent.putExtra(NOTIF_CHAT_ID, dataSnapshot.child("chatId").getValue(String.class));
                Log.d(TAG, "Chat ID: " + dataSnapshot.child("chatId").getValue(String.class));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MyFirebaseMessagingService.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(dataSnapshot.child("nameAndSkill").getValue(String.class))
                        .setContentText(dataSnapshot.child("lastMessage").getValue(String.class))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

                //Remove notification from contact and notificationRequests
                if (dataSnapshot.hasChild("notifications")) {
                    String notifKey = dataSnapshot.child("notification").getValue(String.class);
                    FirebaseDatabase.getInstance().getReference().child("notificationRequests")
                            .child(notifKey).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("users").child(Profile.getCurrentProfile().getId())
                            .child("contacts").child(userId).child("notification").removeValue();
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }
}