package furqon.io.github.mobilproject;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Timer;
import java.util.TimerTask;

import furqon.io.github.mobilproject.Services.NotificationActionService;

public class AudioService extends Service {
    NotificationManagerCompat notificationManagerCompat;
    NotificationCompat.Builder notification;
    int progressMax;

    static PendingIntent pendingIntentPrev;
    static PendingIntent pendingIntentPlay;
    static PendingIntent pendingIntentNext;
    static PendingIntent pendingIntentFav;

    private Handler audio_position_handler = new Handler();
    private MediaPlayer player;
    private String suranomi;

    private AudioRunnable audioRunnable;
    private String suraNumber;

    @Override
    public void onCreate() {
        super.onCreate();



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        String filepath = intent.getStringExtra("filepath");
        suranomi = intent.getStringExtra("suranomi");
        suraNumber = intent.getStringExtra("suranomer");
        ShowNotification(getBaseContext(), R.drawable.ic_pause_circle, suranomi, filepath);
        //audioRunnable.start(filepath);
        audioRunnable = new AudioRunnable(filepath);

        //player.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        audioRunnable.stop_play();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class AudioRunnable extends Thread{
        private final String filepath;
        private Timer timer;

        public AudioRunnable(String filename) {
            this.timer = new Timer();
            this.filepath = filename;

        }

        public synchronized void start() {
            super.start();


        }

        @Override
        public void run() {
            //super.run();
            player = MediaPlayer.create(getBaseContext(), Uri.parse(filepath));

            int audio_pos = SharedPreferences.getInstance().read(suranomi, 0);
            if (audio_pos > 0 && audio_pos != player.getDuration()) {
                player.seekTo(audio_pos);
            } else {
                SharedPreferences.getInstance().write(suranomi, 1);
                player.seekTo(1);
            }


            progressMax = player.getDuration();

            player.start();

            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    cycle(player.getCurrentPosition());
                }
            }, 0, 1000);
        }

        public void cycle(int currentPosition){
            Log.i("TASK", "running " + progressMax + " " + currentPosition);
            notification.setProgress(progressMax, currentPosition, false);

            notificationManagerCompat.notify(1, notification.build());

            if (currentPosition == progressMax) {
                this.timer.cancel();
                SharedPreferences.getInstance().write(suranomi, 1);
                stopSelf();
            }
        }

        public void stop_play() {
            this.timer.cancel();
            SharedPreferences.getInstance().write(suranomi, player.getCurrentPosition());
            Toast.makeText(getBaseContext(), getString(R.string.audiopos_stored), Toast.LENGTH_SHORT).show();
            player.getCurrentPosition();
            player.stop();
            ShowNotification(getBaseContext(), R.drawable.ic_play_circle, suranomi, filepath);
            //notification.addAction(R.drawable.ic_play_circle, getString(R.string.play), pendingIntentPlay);
            //notificationManagerCompat.notify(1, notification.build());

        }
    }

    public void ShowNotification(Context context, int playbutton, String suranomi, String filepath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

            Intent ayahlistIntent = new Intent(getApplicationContext(), AyahList.class);
            ayahlistIntent.putExtra("SURANAME", suranomi + ":" + suraNumber);
            PendingIntent ayahlistPending = PendingIntent.getActivity(this, 0, ayahlistIntent, 0);

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

            int audio_pos = SharedPreferences.getInstance().read(suranomi, 0);
            if (audio_pos == 0) {
                pendingIntentPrev = null;
                drw_previous = 0;
            } else {
                Intent intentPrevious = new Intent(context, NotificationActionService.class).setAction(Furqon.ACTION_PREV);
                pendingIntentPrev = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            Intent intentPlay = new Intent(context, NotificationActionService.class).setAction(Furqon.ACTION_PLAY);
            pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);


            int drw_next;
            if (audio_pos == 0) {
                pendingIntentNext = null;
                drw_next = 0;
            } else {
                Intent intentNext = new Intent(context, NotificationActionService.class)
                        .setAction(Furqon.ACTION_NEXT);

                pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
            }


            Bitmap audio_player_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.nightsky);


            notification = new NotificationCompat.Builder(context, Furqon.AUDIO_PLAYING_NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.ic_surah_audio_24dp)
                    .setLargeIcon(audio_player_icon)
                    .setContentTitle("Furqon Audio Player")
                    .setContentText(suranomi)

                    .addAction(R.drawable.ic_previous, context.getString(R.string.fast_rewind), pendingIntentPrev)
                    .addAction(playbutton, context.getString(R.string.play), pendingIntentPlay)
                    .addAction(R.drawable.ic_next, context.getString(R.string.fast_forward), pendingIntentNext)
                    //.setContentIntent(ayahlistPending)
                    .setProgress(progressMax, audio_pos, false)
                    .setOngoing(true)
                    //.addAction(R.drawable.ic_favorite_border_black_24dp, context.getString(R.string.favorites), pendingIntentFav)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(1)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true);

            //notificationManagerCompat.notify(1, notification.build());
            startForeground(1, notification.build());

            if (audioRunnable != null && audioRunnable.isAlive()) {
                audioRunnable.stop_play();
            } else {
                new Thread(audioRunnable).start();
            }


        }


    }
}

