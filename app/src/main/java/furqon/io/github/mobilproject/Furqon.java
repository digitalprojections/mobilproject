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

    static PendingIntent pendingIntentPrev;
    static PendingIntent pendingIntentPlay;
    static PendingIntent pendingIntentNext;
    static PendingIntent pendingIntentFav;


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
    public static void ShowNotification(Context context, int playbutton, String suranomi, int pos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

//            pendingIntentPrev = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            //PLAY
//            Intent intentPlay;
//            intentPlay = new Intent(this, NotificationActionService.class).setAction(Furqon.ACTION_PLAY);
//
//            pendingIntentPlay = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
//            //NEXT
//            Intent intentNext;
//            intentNext = new Intent(this, NotificationActionService.class).setAction(Furqon.ACTION_NEXT);
//
//            pendingIntentNext = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
//            //FAV
//            Intent intentFav;
//            intentFav = new Intent(this, NotificationActionService.class).setAction(Furqon.ACTION_FAV);
//
//
//            pendingIntentFav = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);

            int drw_previous;
            if (pos == 0) {
                pendingIntentPrev = null;
                drw_previous = 0;
            } else {
                Intent intentPrevious = new Intent(context, NotificationActionService.class)
                        .setAction(Furqon.ACTION_PREV);

                pendingIntentPrev = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            Intent intentPlay = new Intent(context, NotificationActionService.class)
                    .setAction(Furqon.ACTION_PLAY);
            pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);


            int drw_next;
            if (pos == 0) {
                pendingIntentNext = null;
                drw_next = 0;
            } else {
                Intent intentNext = new Intent(context, NotificationActionService.class)
                        .setAction(Furqon.ACTION_NEXT);

                pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
            }


            Bitmap audio_player_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.nightsky);

            Notification notification;
            notification = new NotificationCompat.Builder(context, AUDIO_PLAYING_NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.ic_surah_audio_24dp)
                    .setLargeIcon(audio_player_icon)
                    .setContentTitle("Furqon Audio Player")
                    .setContentText(suranomi)
                    .addAction(R.drawable.ic_previous, context.getString(R.string.fast_rewind), pendingIntentPrev)
                    .addAction(playbutton, context.getString(R.string.play), pendingIntentPlay)
                    .addAction(R.drawable.ic_next, context.getString(R.string.fast_forward), pendingIntentNext)
                    //.addAction(R.drawable.ic_favorite_border_black_24dp, context.getString(R.string.favorites), pendingIntentFav)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(1)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true)
                    .build();
            notificationManagerCompat.notify(1, notification);
        }
    }


}
