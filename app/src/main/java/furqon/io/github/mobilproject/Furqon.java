package furqon.io.github.mobilproject;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import furqon.io.github.mobilproject.Services.NotificationActionService;

public class Furqon extends Application {
    public static final String NOTIFICATION_FROM_AUTHOR = "Muallifdan";
    public static final String AUDIO_PLAYING_NOTIFICATION_CHANNEL = "audio playing";
    public static final String AUDIO_DOWNLOADING_NOTIFICATION_CHANNEL = "downloading a surah";
    public static final String ACTION_PREV = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";
    public static final String ACTION_FAV = "actionfav";


    @Override
    public void onCreate() {
        super.onCreate();
        CreateNotificationChannels();



    }

    private void CreateNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            //these will actually show up in the Apps&Notifications as separate categories
            NotificationChannel notificationChannel_audio_play = new NotificationChannel(AUDIO_PLAYING_NOTIFICATION_CHANNEL, getString(R.string.audio_playing), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel_audio_play.setDescription(getString(R.string.audio_player_description));
            notificationChannel_audio_play.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            //
            NotificationChannel notificationChannel_audio_downloaded = new NotificationChannel(AUDIO_DOWNLOADING_NOTIFICATION_CHANNEL, getString(R.string.audio_downloaded), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel_audio_downloaded.setDescription(getString(R.string.audio_downloaded_ready));
            notificationChannel_audio_downloaded.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel_audio_play);
            notificationManager.createNotificationChannel(notificationChannel_audio_downloaded);


        }

    }



}
