package furqon.io.github.mobilproject;

import android.app.Notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import furqon.io.github.mobilproject.Services.NotificationActionService;
import furqon.io.github.mobilproject.Services.OnClearFromService;

import static furqon.io.github.mobilproject.Furqon.AUDIO_PLAYING_NOTIFICATION_CHANNEL;


public class AyahList extends AppCompatActivity implements ManageSpecials, Playable {

    private ArrayList<JSONObject> jsonArrayResponse;

    private TitleViewModel titleViewModel;
    private AyahListAdapter mAdapter;
    MediaPlayer mediaPlayer;

    boolean isPlaying;

    Integer audio_pos;
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
    private Button tempbut;
    private Context context;
    private boolean httpresponse;
    RecyclerView recyclerView;

    private MenuItem playButton;
    private MenuItem menu_bookmark_btn;

    private NotificationManager notificationManager;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_view);


        tempbut = findViewById(R.id.buttonReload);
        tempbut.setVisibility(View.INVISIBLE);
        titleViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);

        context = this;


        sharedPref = sharedpref.getInstance();
        sharedPref.init(getApplicationContext());




        audiorestore = getString(R.string.audiopos_restored);
        audiostore = getString(R.string.audiopos_stored);
        loadfailed = getString(R.string.audio_load_fail);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromService.class));


        }



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

                recyclerView = findViewById(R.id.chapter_scroll);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));

                mAdapter = new AyahListAdapter(this, suranomi, suranomer);
                recyclerView.setAdapter(mAdapter);

                LoadTheList();


                audio_pos = sharedPref.read(suranomi, 0);


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




        tempbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("CLICK", "clicking");
                RequestQueue queue = Volley.newRequestQueue(context);
                String url = "https://inventivesolutionste.ipage.com/ajax_quran.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //progressBar.setVisibility(View.INVISIBLE);
                                // Convert String to json object
                                httpresponse = true;
                                jsonArrayResponse = new ArrayList<JSONObject>();

                                try {
                                    JSONArray jsonArray = new JSONArray(response);
                                    for (int i=0; i<jsonArray.length();i++)
                                    {
                                        JSONObject object = new JSONObject(jsonArray.getString(i));
                                        jsonArrayResponse.add(object);
                                    }
                                    //PASS to SPINNER
                                    //load auction names and available lot/bid count
                                    populateAyahList(jsonArrayResponse);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.i("error json", "tttttttttttttttt");
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("ERROR RESPONSE", "enable reload button");
                        progressBar.setVisibility(View.INVISIBLE);
                        tempbut.setVisibility(View.VISIBLE);
                    }
                }) {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<String, String>();
                        MyData.put("action", "izohsiz_text_obj"); //Add the data you'd like to send to the server.
                        MyData.put("database_id", "1, 120, 59, 79");
                        MyData.put("surah_id", suranomer);
                        //https://inventivesolutionste.ipage.com/ajax_quran.php
                        //POST
                        //action:names_as_objects
                        //language_id:1
                        return MyData;
                    }
                };
                queue.add(stringRequest);
                tempbut.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
            void populateAyahList(ArrayList<JSONObject> auclist){

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(, android.R.layout.simple_spinner_item, auclist);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //spinner.setAdapter(adapter);
                ChapterTextTable text;

                for (JSONObject i:auclist
                ) {

                    try{
                        //"ID":"31206","VerseID":"7","AyahText":"صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ","DatabaseID":"1","SuraID":"1","OrderNo":"5","SuraType":"Meccan","Note":null
                        Log.d("JSONOBJECT", i.toString());
                        int verse_id = i.getInt("VerseID");
                        int DatabaseID = i.getInt("DatabaseID");
                        int chapter_id = i.getInt("SuraID");
                        int OrderNo = i.getInt("OrderNo");
                        String surah_type = i.getString("SuraType");
                        String AyahText = i.getString("AyahText");


                        //int sura_id, int verse_id, int favourite, int language_id, String ayah_text, String surah_type, int order_no, String comment, int read_count, int shared_count, int audio_position
                        text = new ChapterTextTable(chapter_id, verse_id,0, DatabaseID, OrderNo, AyahText, "", surah_type);
                        titleViewModel.insertText(text);


                    }catch (Exception sx){
                        Log.e("EXCEPTION", sx.getMessage());
                    }
                }




            }
        });
    }
    private void PopulateTrackList() {
        //TODO clean up wrong files
        String path = getExternalFilesDir(null).getAbsolutePath();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files!=null){
            Log.d("FILES", "Size: "+ files.length);

            for (int i = 0; i < files.length; i++)
            {
                Log.d("Files", "FileName:" + files[i].getName());
                String trackname = files[i].getName().toString();
                if(trackname.contains(".")){
                    trackname = trackname.substring(0, trackname.lastIndexOf("."));
                    if(trackname==suranomer){
                        MarkAsDownloaded(Integer.parseInt(trackname));
                        Log.i("TRACKNAME",  "number " + trackname + " track is available");

                    }
                }
            }
        }else{
            Log.d("NULL ARRAY", "no files found");
        }
    }

    private void MarkAsDownloaded(int parseInt) {
    }

    public void playCycle() {
        if (mediaPlayer != null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            audio_pos = mediaPlayer.getCurrentPosition();
            timer = findViewById(R.id.audio_timer);
            timer.setText(AudioTimer.getTimeStringFromMs(audio_pos));

            //Furqon.ShowNotification(this, suranomi, audio_pos);

            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        //startTimer();
                        playCycle();
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
                    OnTrackPrevious();
                    Toast.makeText(context,"PREVCLICKED", Toast.LENGTH_SHORT).show();
                    break;
                case Furqon
                        .ACTION_PLAY:
                    if(isPlaying){
                        OnTrackPause();
                    }else{
                        OnTrackPlay();
                    }

                    break;
                case Furqon
                        .ACTION_NEXT:
                    OnTrackNext();
                    break;
                case Furqon
                        .ACTION_FAV:
                    //TODO create fav action
                    //OnTrackFavourite();
                    break;
            }
        }
    };
    private void LoadTheList() {
        titleViewModel.getChapterText(suranomer).observe(this, new Observer<List<AllTranslations>>() {
            @Override
            public void onChanged(@Nullable List<AllTranslations> surahText) {
                //Toast.makeText(SuraNameList.this, "LOADING TITLES " + surahTitles.size(), Toast.LENGTH_LONG).show();
                int sc = getSurahLength(suranomer);
                Log.e("SURAH AYAH COUNT", sc + " " +surahText.size());
                if(surahText.size()!=sc){
                    if(!httpresponse) {
                        if(surahText.size()!=0){
                            titleViewModel.deleteSurah(Integer.parseInt(suranomer));
                            Toast.makeText(getApplicationContext(), R.string.surah_size_issue, Toast.LENGTH_LONG).show();
                        }
                        tempbut.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }else{
                    tempbut.setVisibility(View.GONE);
                    progressBar.setVisibility(View.INVISIBLE);
                    //Toast.makeText(context, "LOADING TITLES " + surahText.size(), Toast.LENGTH_LONG).show();
                }

                mAdapter.setText(surahText);
            }
        });
    }

    private int getSurahLength(String suranomer) {
        return QuranMap.AYAHCOUNT[Integer.parseInt(suranomer)-1];
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

        playButton = menu.getItem(1);

        ayah_position = sharedPref.read(xatchup + suranomi, 0);

        if (ayah_position > 0) {
            //BOOKMARK FOUND
            Toast.makeText(context, getString(R.string.bookmark_found), Toast.LENGTH_SHORT).show();
            menu_bookmark_btn = menu.getItem(0);
            menu.getItem(0).setVisible(true);
        }else{
            menu.getItem(0).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play:
                if(isPlaying){
                    OnTrackPause();
                    playButton.setIcon(R.drawable.ic_play_arrow_black_24dp);
                }else{
                    OnTrackPlay();
                }
                return true;
            case R.id.stop:
                if (mediaPlayer != null) {
                    pause();
                     playButton.setIcon(R.drawable.ic_play_arrow_black_24dp);
                }
                return true;
            case R.id.menu_bookmark_button:
                recyclerView.scrollToPosition(ayah_position-1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void play() {

        String filePath = "";

        String path = getExternalFilesDir(null).getAbsolutePath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files!=null){
            for (int i = 0; i < files.length; i++)
            {
                String trackname = files[i].getName();
                if(trackname.contains(".")){
                    trackname = trackname.substring(0, trackname.lastIndexOf("."));
                    if(trackname.equals(suranomer)){
                        filePath = path + "/" + trackname + ".mp3";
                    }
                }
            }
        }else{
            //This surah is not available
        }

        //String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suranomer + ".mp3"; // your URL here
        String url = filePath;
        if(!url.isEmpty()){
            Log.i("PLAY", url);
            if (mediaPlayer == null) {

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {

                        seekBar.setMax(mediaPlayer.getDuration());
                        progressBar.setVisibility(View.INVISIBLE);
                        resume();
                        isPlaying = true;
                    }
                });
                //mediaPlayer.setAudioStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
                try{
                    mediaPlayer.setDataSource(url);
                }catch (IOException iox){
                    Log.e("ERROR", iox.getMessage());
                }



                progressBar.setVisibility(View.VISIBLE);
                mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)

                playButton.setIcon(R.drawable.ic_pause);

            } else {
                if(isPlaying){
                    pause();
                }else{
                    mediaPlayer.release();
                    mediaPlayer = null;
                    play();
                }


            }

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
            isPlaying = false;

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
        audio_pos = sharedPref.read(suranomi, 0);

        if (mediaPlayer != null) {

            //seekBar.setProgress(pos);
            mediaPlayer.seekTo(audio_pos);
            mediaPlayer.start();
            Toast.makeText(getBaseContext(), audiorestore, Toast.LENGTH_SHORT).show();
            playCycle();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        pause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            handler.removeCallbacks(runnable);
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            if(notificationManager!=null)
                notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void UpdateSpecialItem(ChapterTextTable text) {

        titleViewModel.updateText(text);

    }

    @Override
    public void OnTrackPrevious() {

    }

    @Override
    public void OnTrackPlay() {
        Furqon.ShowNotification(AyahList.this, R.drawable.ic_pause_circle, suranomi, audio_pos);
        play();
    }

    @Override
    public void OnTrackNext() {

    }

    @Override
    public void OnTrackPause() {
        Furqon.ShowNotification(AyahList.this, R.drawable.ic_play_circle, suranomi, audio_pos);
        isPlaying = false;
        pause();
    }
}
