package furqon.io.github.mobilproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class AudioService extends Service {

    private MediaPlayer player;
    private String suranomi;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        String filepath = intent.getStringExtra("filepath");
        suranomi = intent.getStringExtra("suranomi");

        player = MediaPlayer.create(this, Uri.parse(filepath));

        player.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        sharedpref.getInstance().write(suranomi, player.getCurrentPosition());
        Toast.makeText(this, getString(R.string.audiopos_stored), Toast.LENGTH_SHORT).show();
        player.getCurrentPosition();
        player.stop();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
