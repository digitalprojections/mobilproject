package furqon.io.github.mobilproject;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private ChapterTitleDatabase database;

    private static final String TAG = "FIREBASE Messages";
    private sharedpref sharedPref;

    public MyFirebaseMessagingService() {
        sharedPref = sharedpref.getInstance();
        //Log.e("MyFirebaseMessage", "initiated");

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage);

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            //scheduleJob();
            //database = ChapterTitleDatabase.getDatabase(this);
            //database.SaveMessage(remoteMessage.getData());
            handleNow(remoteMessage);
        }


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
            handleNow(remoteMessage);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(@NonNull String s) {
        super.onMessageSent(s);
    }

    @Override
    public void onSendError(@NonNull String s, @NonNull Exception e) {
        super.onSendError(s, e);
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sharedPref.init(getApplicationContext());
        sharedPref.write(sharedPref.TOKEN, token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void scheduleJob(){

    }
    private void handleNow(RemoteMessage s){
        Log.d(TAG, "text received: " + s);
        sendNotification(s.getNotification().getBody());

        database = ChapterTitleDatabase.getDatabase(this);
        database.SaveMessage(s);

        if(s.getData().size()>0){
            if(s.getData().get("title").equals("Thanks for sharing!")){
                int existingCoins = sharedpref.getInstance().read(sharedpref.getInstance().COINS, 0);
                int totalCoins = existingCoins + sharedpref.getInstance().read(sharedpref.getInstance().SHAREWARD, 50);
                sharedpref.getInstance().write(sharedpref.getInstance().COINS, totalCoins);
            }
        }
    }
    private void sendNotification(String messageBody) {



        Intent intent = new Intent(this, MessageList.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.notification_from_author_channel);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_asset_1furqon_logo)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

        //TODO add to database
        //TODO open a special activity
    }

    private String getString(String notificationFromAuthor) {
        return Furqon.NOTIFICATION_FROM_AUTHOR;
    }

    private void sendRegistrationToServer(String token){



    }
}
