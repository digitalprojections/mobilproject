package furqon.io.github.mobilproject;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import furqon.io.github.mobilproject.Services.NotificationActionService;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AudioIntentService extends IntentService {

    NotificationManagerCompat notificationManagerCompat;
    NotificationCompat.Builder notification;
    static PendingIntent pendingIntentPrev;
    static PendingIntent pendingIntentPlay;
    static PendingIntent pendingIntentNext;


    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_STOP = "STOP";

    // TODO: Rename parameters
    private static final String SURA_NOMI = "suranomi";
    private static final String FILE_PATH = "filepath";
    private static final String SURA_NOMER = "suranomer";

    private MediaPlayer player;

    public AudioIntentService() {
        super("AudioIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificationManagerCompat = NotificationManagerCompat.from(this);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");

//            Intent ayahlistIntent = new Intent(getApplicationContext(), AyahList.class);
//            ayahlistIntent.putExtra("SURANAME",suranomi+":"+suraNumber);
//            PendingIntent ayahlistPending = PendingIntent.getActivity(this, 0, ayahlistIntent, 0);

            Intent intentPlay = new Intent(this, NotificationActionService.class).setAction(Furqon.ACTION_PLAY);
            pendingIntentPlay = PendingIntent.getBroadcast(this, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

            Bitmap audio_player_icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.nightsky);

            notification = new NotificationCompat.Builder(this, Furqon.AUDIO_PLAYING_NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.ic_surah_audio_24dp)
                    .setLargeIcon(audio_player_icon)
                    .setContentTitle("Furqon Audio Player")
                    //.setContentText(suranomi)
                    .addAction(R.drawable.ic_play_circle, this.getString(R.string.play), pendingIntentPlay)
                    //.addAction(R.drawable.ic_next, this.getString(R.string.fast_forward), pendingIntentNext)
                    //.setContentIntent(ayahlistPending)
                    .setProgress(0, 0, false)
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
        }
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionPlay(Context context, String suranomi, String suranomer) {
        Intent intent = new Intent(context, AudioIntentService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(SURA_NOMI, suranomi);
        intent.putExtra(SURA_NOMER, suranomer);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action STOP with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionStop(Context context) {
        Intent intent = new Intent(context, AudioIntentService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) {
                final String filepath = intent.getStringExtra(FILE_PATH);
                final String suranomer = intent.getStringExtra(SURA_NOMER);
                handleActionPlay(filepath, suranomer);
            } else if (ACTION_STOP.equals(action)) {
                //final String param1 = intent.getStringExtra(SURA_NOMI);
                //final String param2 = intent.getStringExtra(SURA_NOMER);
                handleActionStop();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPlay(String filepath, String suranomer) {
        // TODO: Handle action PLAY
        player = MediaPlayer.create(getBaseContext(), Uri.parse(filepath));
        if (!player.isPlaying())
            player.start();
        else
            player.stop();
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStop() {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
