package furqon.io.github.mobilproject;

import android.app.Notification;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.Objects;

import furqon.io.github.mobilproject.Services.NotificationActionService;

import static furqon.io.github.mobilproject.Furqon.AUDIO_PLAYING_NOTIFICATION_CHANNEL;


public class AyahList extends AppCompatActivity {

    //private NotificationManagerCompat managerCompat;


    //PendingIntent pendingIntent;

    private AyahListAdapter mAdapter;
    public DatabaseAccess mDatabase;
    Cursor ayahcursor;
    MediaPlayer mediaPlayer;

    Integer pos;
    Integer ayah_position;
    public String suranomi;
    public String xatchup = "xatchup";
    String suranomer;
    TextView timer;
    String audiorestore;
    String audiostore;
    String loadfailed;
    ProgressBar progressBar;
    SeekBar seekBar;
    Handler handler;
    Runnable runnable;
    private sharedpref sharedPref;
    PendingIntent pendingIntentPrev;
    PendingIntent pendingIntentPlay;
    PendingIntent pendingIntentNext;
    PendingIntent pendingIntentFav;
    MediaSessionCompat mediaSessionCompat;

    private NotificationManagerCompat managerCompat;


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_view);
        managerCompat = NotificationManagerCompat.from(this);
        //managerCompat = NotificationManagerCompat.from(this);
        //Intent notifintent = new Intent(this, MainActivity.class);
        //pendingIntent = PendingIntent.getActivity(this, 0, notifintent, 0);


        sharedPref = sharedpref.getInstance();
        sharedPref.init(getApplicationContext());


        mediaSessionCompat = new MediaSessionCompat(this, "tag");

        audiorestore = getString(R.string.audiopos_restored);
        audiostore = getString(R.string.audiopos_stored);
        loadfailed = getString(R.string.audio_load_fail);

        progressBar = findViewById(R.id.progressBar);

        Toolbar toolbar = findViewById(R.id.audiobar);
        setSupportActionBar(toolbar);

        Bundle intent = getIntent().getExtras();
        String extratext;
        if(intent!=null){
            try{
                extratext = intent.getString("SURANAME");
                assert extratext != null;
                suranomi = extratext.substring(0, extratext.indexOf(":"));
                suranomer = extratext.substring(extratext.indexOf(":") + 1);

                Log.i("LOADED SURA", suranomer + " " + suranomi);

                Objects.requireNonNull(getSupportActionBar()).setTitle(suranomi);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                mDatabase = DatabaseAccess.getInstance(getApplicationContext());
                if (!mDatabase.isOpen()) {
                    mDatabase.open();
                }

                RecyclerView recyclerView = findViewById(R.id.chapter_scroll);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));


                LoadTheCursor();


                mAdapter = new AyahListAdapter(this, ayahcursor, suranomi, suranomer);
                recyclerView.setAdapter(mAdapter);


                pos = sharedPref.read(suranomi, 0);

                ayah_position = sharedPref.read(xatchup + suranomi, 0);
                if (ayah_position > 4) {

                    recyclerView.scrollToPosition(ayah_position);
                }
                handler = new Handler();

                seekBar = findViewById(R.id.seekBar);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                        if (input) {
                            if (mediaPlayer != null) {
                                mediaPlayer.seekTo(progress);
                            }

                        }

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
            catch (NullPointerException npx){
                Toast.makeText( getBaseContext(), R.string.error_message, Toast.LENGTH_SHORT).show();
            }
        }

        //PREV
        Intent intentPrev;
        intentPrev = new Intent(this, NotificationActionService.class).setAction(Furqon.ACTION_PREV);

        pendingIntentPrev = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);

        //PLAY
        Intent intentPlay;
        intentPlay = new Intent(this, NotificationActionService.class).setAction(Furqon.ACTION_PLAY);

        pendingIntentPlay = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
        //NEXT
        Intent intentNext;
        intentNext = new Intent(this, NotificationActionService.class).setAction(Furqon.ACTION_NEXT);

        pendingIntentNext = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
        //FAV
        Intent intentFav;
        intentFav = new Intent(this, NotificationActionService.class).setAction(Furqon.ACTION_FAV);


        pendingIntentFav = PendingIntent.getBroadcast(this, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void LoadTheCursor() {
        ayahcursor = mDatabase.getSuraText(suranomer);
    }

    public void playCycle() {
        if (mediaPlayer != null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            pos = mediaPlayer.getCurrentPosition();
            timer = findViewById(R.id.audio_timer);
            timer.setText(AudioTimer.getTimeStringFromMs(pos));

            ShowNotification(this, suranomi);

            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        //startTimer();
                        //playCycle();
                        Log.i("TIMER", "tick");
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");

            switch (action){
                case Furqon
                        .ACTION_PREV:
                    Toast.makeText(context,"PREVCLICKED", Toast.LENGTH_SHORT).show();
                    break;
                case Furqon
                        .ACTION_PLAY:
                    break;
                case Furqon
                        .ACTION_NEXT:
                    break;
                case Furqon
                        .ACTION_FAV:
                    break;
            }
        }
    };

    public void ShowNotification(Context context, String suranomi){
        Bitmap audio_player_icon = BitmapFactory.decodeResource(getResources(), R.drawable.nightsky);

        Notification notification;
        notification = new NotificationCompat.Builder(context, AUDIO_PLAYING_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_surah_audio_24dp)
                .setLargeIcon(audio_player_icon)
                .setContentTitle("Furqon Audio Player")
                .setContentText(suranomi)
                .addAction(R.drawable.ic_previous, getString(R.string.fast_rewind), pendingIntentPrev)
                .addAction(R.drawable.ic_play_circle, getString(R.string.play), pendingIntentPlay)
                .addAction(R.drawable.ic_next, getString(R.string.fast_forward), pendingIntentNext)
                .addAction(R.drawable.ic_favorite_border_black_24dp, getString(R.string.favorites), pendingIntentFav)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .build();
        managerCompat.notify(1, notification);
    }
    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //mediaPlayer.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_navigation_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play:
                try {

                    play();

                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), loadfailed, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                return true;
            case R.id.stop:
                if (mediaPlayer != null) {
                    pause();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void play() throws IOException {

        String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suranomer + ".mp3"; // your URL here
        Log.i("PLAY", url);
        if (mediaPlayer == null) {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    seekBar.setMax(mediaPlayer.getDuration());
                    progressBar.setVisibility(View.INVISIBLE);
                    resume();
                }
            });
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);

            if (isNetworkAvailable()) {
                progressBar.setVisibility(View.VISIBLE);
                mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
            } else {
                Toast.makeText(getBaseContext(), loadfailed, Toast.LENGTH_SHORT).show();
            }


        } else {

            mediaPlayer.release();
            mediaPlayer = null;
            play();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void stop() {
        Log.i("STOP", "stop");
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            storeAudioPosition();


        }
    }

    private void storeAudioPosition() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {

                sharedPref.write(suranomi, mediaPlayer.getCurrentPosition());
                Toast.makeText(getBaseContext(), audiostore, Toast.LENGTH_SHORT).show();
                mediaPlayer.pause();
            }
        }
    }

    public void resume() {
        pos = sharedPref.read(suranomi, 0);

        if (mediaPlayer != null) {

            //seekBar.setProgress(pos);
            mediaPlayer.seekTo(pos);
            mediaPlayer.start();
            Toast.makeText(getBaseContext(), audiorestore, Toast.LENGTH_SHORT).show();
            playCycle();
        }
    }

    private void startTimer() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        pause();
        if (mediaPlayer != null) {
            mediaPlayer.release();


            handler.removeCallbacks(runnable);
        }
        if (mDatabase != null) {
            mDatabase.close();
        }
    }


}
