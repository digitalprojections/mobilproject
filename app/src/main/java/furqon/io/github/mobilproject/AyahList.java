package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.TimerTask;


public class AyahList extends AppCompatActivity {
    private AyahListAdapter mAdapter;
    private DatabaseAccess mDatabase;
    private Cursor ayahcursor;
    private MediaPlayer mediaPlayer;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Integer pos;
    String suranomi;
    String suranomer;
    TextView timer;
    String audiorestore;
    String audiostore;
    String loadfailed;
    ProgressBar progressBar;
    Thread t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_view);


        audiorestore = getString(R.string.audiopos_restored);
        audiostore = getString(R.string.audiopos_stored);
        loadfailed = getString(R.string.audio_load_fail);

        progressBar = findViewById(R.id.progressBar);

        Toolbar toolbar = findViewById(R.id.audiobar);
        setSupportActionBar(toolbar);

        Bundle intent = getIntent().getExtras();
        String extratext = intent.getString("SURANAME");


        suranomi = extratext.substring(0, extratext.indexOf(":"));
        suranomer = extratext.substring(extratext.indexOf(":") + 1);

        Log.i("LOADED SURA", suranomer + " " + suranomi);

        getSupportActionBar().setTitle(suranomi);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = DatabaseAccess.getInstance(getApplicationContext());


        final RecyclerView recyclerView = findViewById(R.id.chapter_scroll);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatabase.open();

        ayahcursor = mDatabase.getSuraText(suranomer);

        mAdapter = new AyahListAdapter(this, ayahcursor);
        recyclerView.setAdapter(mAdapter);


        sharedPreferences = getSharedPreferences(suranomi, MODE_PRIVATE);
        pos = sharedPreferences.getInt(suranomi, 0);

        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startTimer();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_navigation_items, menu);
        return true;
    }

    public void play() throws IOException {

        String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suranomer + ".mp3"; // your URL here
        Log.i("PLAY", url);
        if (mediaPlayer == null) {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    resume();
                    progressBar.setVisibility(View.INVISIBLE);
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
            mediaPlayer.pause();

        }
    }

    private void storeAudioPosition() {
        if (pos > 0) {
            sharedPreferences = getSharedPreferences(suranomi, MODE_PRIVATE);
            editor = sharedPreferences.edit();
            editor.putInt(suranomi, mediaPlayer.getCurrentPosition());
            editor.apply();
            Toast.makeText(getBaseContext(), audiostore, Toast.LENGTH_SHORT).show();
        }
    }

    public void resume() {
        sharedPreferences = getSharedPreferences(suranomi, MODE_PRIVATE);
        pos = sharedPreferences.getInt(suranomi, 0);

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        } else if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.seekTo(pos);
            Toast.makeText(getBaseContext(), audiorestore, Toast.LENGTH_SHORT).show();
            t.start();
        }
    }

    private void startTimer() {
        pos = mediaPlayer.getCurrentPosition();
        timer = findViewById(R.id.audio_timer);
        timer.setText(AudioTimer.getTimeStringFromMs(pos));
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
                if (mediaPlayer == null) {

                } else {
                    pause();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pause();
    }

}
