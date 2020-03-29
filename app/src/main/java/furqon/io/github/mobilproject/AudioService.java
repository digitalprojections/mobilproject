package furqon.io.github.mobilproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class AudioService extends Service {

    private Handler audio_position_handler = new Handler();
    private MediaPlayer player;
    private String suranomi;

    private AudioRunnable audioRunnable;

    @Override
    public void onCreate() {
        super.onCreate();

        audioRunnable = new AudioRunnable();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        String filepath = intent.getStringExtra("filepath");
        suranomi = intent.getStringExtra("suranomi");

        audioRunnable.start(filepath);

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
        private Timer timer;

        public AudioRunnable() {
            this.timer = new Timer();
        }

        public synchronized void start(String filepath) {
            super.start();
            player = MediaPlayer.create(getBaseContext(), Uri.parse(filepath));
            int audio_pos = sharedpref.getInstance().read(suranomi, 0);
            if(audio_pos>0)
                player.seekTo(audio_pos);
            player.start();
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    cycle(player.getCurrentPosition());
                }
            }, 0, 1000);
        }

        public void cycle(int currentPosition){
            Log.i("TASK", "running " + currentPosition);
        }

        public void stop_play() {
            this.timer.cancel();
            sharedpref.getInstance().write(suranomi, player.getCurrentPosition());
            Toast.makeText(getBaseContext(), getString(R.string.audiopos_stored), Toast.LENGTH_SHORT).show();
            player.getCurrentPosition();
            player.stop();
        }
    }
}

