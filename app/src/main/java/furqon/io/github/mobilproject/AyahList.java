package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class AyahList extends AppCompatActivity {
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



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_view);
        SharedPref.init(getApplicationContext());



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
        if(!mDatabase.isOpen()) {
            mDatabase.open();
        }

        RecyclerView recyclerView = findViewById(R.id.chapter_scroll);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));





        ayahcursor = mDatabase.getSuraText(suranomer);



        mAdapter = new AyahListAdapter(this, ayahcursor, suranomi, suranomer);
        recyclerView.setAdapter(mAdapter);






        pos = SharedPref.read(suranomi, 0);

        ayah_position = SharedPref.read(xatchup+suranomi, 0);
        if(ayah_position>4) {

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

    public void playCycle() {
        if(mediaPlayer!=null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            pos = mediaPlayer.getCurrentPosition();
            timer = findViewById(R.id.audio_timer);
            timer.setText(AudioTimer.getTimeStringFromMs(pos));

            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        startTimer();
                        playCycle();
                        Log.i("TIMER", "tick");
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        }
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
                if (mediaPlayer == null) {

                } else {
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
        if(mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {

                SharedPref.write(suranomi, mediaPlayer.getCurrentPosition());
                Toast.makeText(getBaseContext(), audiostore, Toast.LENGTH_SHORT).show();
                mediaPlayer.pause();
            }
        }
    }

    public void resume() {
        pos = SharedPref.read(suranomi, 0);

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
        if(mDatabase!=null) {
            mDatabase.close();
        }
    }


}
