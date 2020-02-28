package furqon.io.github.mobilproject;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import furqon.io.github.mobilproject.Services.NotificationActionService;

public class Furqon extends Application {
    public static final String CHANNEL_AUDIO_PLAYING_ID = "audio playing";
    public static final String CHANNEL_AUDIO_DOWNLOADING_ID = "downloading a surah";
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
            NotificationChannel notificationChannel_audio_play = new NotificationChannel(CHANNEL_AUDIO_PLAYING_ID, getString(R.string.audio_playing), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel_audio_play.setDescription(getString(R.string.audio_player_description));
            //
            NotificationChannel notificationChannel_audio_downloaded = new NotificationChannel(CHANNEL_AUDIO_DOWNLOADING_ID, getString(R.string.audio_downloaded), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel_audio_downloaded.setDescription(getString(R.string.audio_downloaded_ready));

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel_audio_play);
            notificationManager.createNotificationChannel(notificationChannel_audio_downloaded);

            Intent intentPrev = new Intent(this, NotificationActionService.class).setAction(ACTION_PREV);
            PendingIntent pendingIntentPrev = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);

        }
    }
}
