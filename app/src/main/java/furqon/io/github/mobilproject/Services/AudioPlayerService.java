package furqon.io.github.mobilproject.Services;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaSessionManager;

import furqon.io.github.mobilproject.Furqon;
import furqon.io.github.mobilproject.R;

public class AudioPlayerService extends Service {


    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_STOP = "action_stop";

    private MediaPlayer mediaPlayer;
    private MediaSession mediaSession;
    private MediaSessionManager mediaSessionManager;
    private MediaController controllerCompat;
    static PendingIntent pendingIntentPrev;
    static PendingIntent pendingIntentPlay;
    static PendingIntent pendingIntentNext;
    static PendingIntent pendingIntentFav;

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return null;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        mediaSession.release();
        return super.onUnbind(intent);
    }

    private void handleIntent(Intent intent){
        if(intent == null || intent.getAction() == null){
            return;
        }
        String action = intent.getAction();

        switch (action){
            case ACTION_PLAY:
                controllerCompat.getTransportControls().play();
                break;
            case ACTION_PAUSE:
                controllerCompat.getTransportControls().pause();
                break;
            case ACTION_PREVIOUS:
                controllerCompat.getTransportControls().skipToPrevious();
                break;
            case ACTION_NEXT:
                controllerCompat.getTransportControls().skipToNext();
                break;
            case ACTION_STOP:
                controllerCompat.getTransportControls().stop();
                break;

        }
    }

    private Notification.Action generateAction(int icon, String title, String intentAction){
        Intent intent = new Intent(getApplicationContext(), AudioPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        Notification.Action build;
        build = new Notification.Action(icon, title, pendingIntent);
        return build; //make sure to build where necessary
    }

    private void buildNotification(Notification.Action action){
        Notification.MediaStyle style = new Notification.MediaStyle();
        Intent intent = new Intent(getApplicationContext(), AudioPlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher2)
                .setContentTitle(getString(R.string.audio_player_title))
                .setContentText("Surah name")
                .setDeleteIntent(pendingIntent)
                .setStyle(style);
        builder.addAction(generateAction(R.drawable.ic_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(R.drawable.ic_next, "Next", ACTION_NEXT));
        style.setShowActionsInCompactView(1);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

//    public static void ShowNotification(Context context, int playbutton, String suranomi, int pos) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
//            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");
//
//            int drw_previous;
//            if (pos == 0) {
//                pendingIntentPrev = null;
//                drw_previous = 0;
//            } else {
//                Intent intentPrevious = new Intent(context, NotificationActionService.class)
//                        .setAction(Furqon.ACTION_PREV);
//
//                pendingIntentPrev = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
//            }
//
//            Intent intentPlay = new Intent(context, NotificationActionService.class)
//                    .setAction(Furqon.ACTION_PLAY);
//            pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//            int drw_next;
//            if (pos == 0) {
//                pendingIntentNext = null;
//                drw_next = 0;
//            } else {
//                Intent intentNext = new Intent(context, NotificationActionService.class)
//                        .setAction(Furqon.ACTION_NEXT);
//
//                pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
//            }
//
//
//            Bitmap audio_player_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.nightsky);
//
//            Notification notification;
//            notification = new NotificationCompat.Builder(context, Furqon.AUDIO_PLAYING_NOTIFICATION_CHANNEL)
//                    .setSmallIcon(R.drawable.ic_surah_audio_24dp)
//                    .setLargeIcon(audio_player_icon)
//                    .setContentTitle("Furqon Audio Player")
//                    .setContentText(suranomi)
//                    .addAction(R.drawable.ic_previous, context.getString(R.string.fast_rewind), pendingIntentPrev)
//                    .addAction(playbutton, context.getString(R.string.play), pendingIntentPlay)
//                    .addAction(R.drawable.ic_next, context.getString(R.string.fast_forward), pendingIntentNext)
//                    .addAction(R.drawable.ic_favorite_border_black_24dp, context.getString(R.string.favorites), pendingIntentFav)
//                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
//                            .setShowActionsInCompactView(0, 1, 2)
//                            .setMediaSession(mediaSessionCompat.getSessionToken()))
//                    .setPriority(NotificationCompat.PRIORITY_LOW)
//                    .setAutoCancel(false)
//                    .setOnlyAlertOnce(true)
//                    .build();
//            notificationManagerCompat.notify(1, notification);
//        }
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mediaSessionManager==null){
            initMediaSession();
        }
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaSession(){
        mediaPlayer = new MediaPlayer();
        mediaSession = new MediaSession(getApplicationContext(), getString(R.string.audio_player_title));
        controllerCompat = new MediaController(getApplicationContext(), mediaSession.getSessionToken());
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                buildNotification(generateAction(R.drawable.ic_pause_circle, "Pause", ACTION_PLAY));
            }

            @Override
            public void onPause() {
                super.onPause();
                buildNotification(generateAction(R.drawable.ic_play_circle, "Play", ACTION_PAUSE));
            }

            @Override
            public void onStop() {
                super.onStop();
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
                Intent intent = new Intent(getApplicationContext(), AudioPlayerService.class);
                stopService(intent);

            }
        });
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    }


}
