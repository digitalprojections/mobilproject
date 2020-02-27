package furqon.io.github.mobilproject;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Furqon extends Application {
    public static final String CHANNEL_AUDIO_PLAYING_ID = "audio playing";
    public static final String CHANNEL_AUDIO_DOWNLOADING_ID = "downloading a surah";

    @Override
    public void onCreate() {
        super.onCreate();
        CreateNotificationChannels();
    }

    private void CreateNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel_audio_play = new NotificationChannel(CHANNEL_AUDIO_PLAYING_ID, "Audio playing", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel_audio_play.setDescription("Surah audio is playing");
            NotificationChannel notificationChannel_audio_downloaded = new NotificationChannel(CHANNEL_AUDIO_DOWNLOADING_ID, "Audio downloaded", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel_audio_downloaded.setDescription("Surah has been downloaded");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel_audio_play);
            notificationManager.createNotificationChannel(notificationChannel_audio_downloaded);
        }
    }
}
