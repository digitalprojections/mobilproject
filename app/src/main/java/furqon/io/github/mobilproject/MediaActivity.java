package furqon.io.github.mobilproject;

import   androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import furqon.io.github.mobilproject.Services.OnClearFromService;

import static furqon.io.github.mobilproject.BuildConfig.*;
import static java.util.Collections.*;

public class MediaActivity extends AppCompatActivity implements MyListener, Playable, AdapterView.OnItemSelectedListener, View.OnClickListener, SetSuraNumber, ManageDownloadIconState {
    private static final int MY_WRITE_EXTERNAL_STORAGE = 101;
    private static final String TAG = MediaActivity.class.getSimpleName();

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private MediaActivityAdapter mAdapter;
    private LinearLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private Spinner recitationstyle_spinner;
    private Spinner reciter_spinner;
    private SpinnerAdapter spinnerAdapter;

    private TitleViewModel titleViewModel;
    private ArrayList<Track> trackList;
    private ArrayAdapter<CharSequence> language_adapter;
    private ArrayAdapter<CharSequence> recitationstyle_adapter;
    private ArrayAdapter<CharSequence> reciter_adapter;
    private MediaPlayer mediaPlayer;
    private ArrayList<JSONObject> jsonArrayResponse;



    private Context context;
    String newpath;
    long downloadId;
    ImageButton dl_view_btn;
    ImageButton pl_view_btn;
    ImageView play_btn;
    ImageView previous_btn;
    ImageView next_btn;
    ImageView playmode_btn;

    String play_mode = "list";


    //TODO create LL vars
    View special_actions_ll;
    View media_player_ll;
    SeekBar mp_seekBar;

    TextView current_track_tv;
    private String suraNumber2Play;
    private String suraNumber2Download;
    public String suranomi;
    TextView cost_txt;
    TextView coins_txt;
    //audio
    String language;
    String recitation_style;
    String reciter;
    Integer audio_pos;
    Integer ayah_position;

    String audiorestore;
    String audiostore;
    String loadfailed;
    //private MenuItem playButton;
    private int ayah_unlock_cost;
    private int available_coins;
    private int currentStatus;
    private int status = 0;
    InterstitialAd mInterstitialAd = new InterstitialAd(this);
    Handler handler;
    private NotificationManager notificationManager;
    boolean isPlaying;

    private SharedPreferences mSharedPref;
    //ProgressBar progressBar;
    private Runnable runnable;
    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
    DownloadManager downloadManager;
    DownloadManager.Query query;
    private boolean download_attempted;
    Timer myTimer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        titleViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);
        context = this;
        if (BUILD_TYPE.equals("debug")) {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        coordinatorLayout = findViewById(R.id.mp_main_linear_layout);

        audiorestore = getString(R.string.audiopos_restored);
        audiostore = getString(R.string.audiopos_stored);
        loadfailed = getString(R.string.audio_load_fail);

        dl_view_btn = findViewById(R.id.mp_imageButton_dl);
        pl_view_btn = findViewById(R.id.mp_imageButton_pl);

        play_btn = findViewById(R.id.mp_play_toggle);
        previous_btn = findViewById(R.id.mp_previous);
        next_btn = findViewById(R.id.mp_next);
        playmode_btn = findViewById(R.id.mp_playmode_btn);

        current_track_tv = findViewById(R.id.mp_current_title_tv);
        //current_track_tv.setText("");

        dl_view_btn.setOnClickListener(this);
        pl_view_btn.setOnClickListener(this);
        play_btn.setOnClickListener(this);
        previous_btn.setOnClickListener(this);
        next_btn.setOnClickListener(this);
        playmode_btn.setOnClickListener(this);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //Log.i(TAG, currentUser.getEmail());

        media_player_ll = findViewById(R.id.mp_player_ll);
        special_actions_ll = findViewById(R.id.mp_actions_ll);
        mp_seekBar = findViewById(R.id.mp_seekBar);

        mSharedPref = SharedPreferences.getInstance();
        mSharedPref.init(getApplicationContext());
        getPlayMode();


        //registerReceiver(broadcastReceiverAudio, new IntentFilter("TRACKS_TRACKS"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiverDownload, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            try {
                startService(new Intent(getBaseContext(), OnClearFromService.class));
            } catch (IllegalStateException | SecurityException x) {
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.e(TAG, x.getMessage());
            }

        }

        downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
        query = new DownloadManager.Query();

        trackList = new ArrayList<>();

        recyclerView = findViewById(R.id.mp_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MediaActivityAdapter(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        language_adapter = ArrayAdapter.createFromResource(this, R.array.available_languages, android.R.layout.simple_spinner_item);

        recitationstyle_adapter = ArrayAdapter.createFromResource(this, R.array.recitation_styles_arabic, android.R.layout.simple_spinner_item);
        reciter_adapter = ArrayAdapter.createFromResource(this, R.array.arabic_murattal, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        language_adapter.setDropDownViewResource(R.layout.mp_spinner_item);
        recitationstyle_adapter.setDropDownViewResource(R.layout.mp_spinner_item);
        reciter_adapter.setDropDownViewResource(R.layout.mp_spinner_item);
        // Apply the adapter to the spinner
        Spinner language_spinner = findViewById(R.id.mp_language_spinner);
        recitationstyle_spinner = findViewById(R.id.mp_recitationstyle_spinner);
        reciter_spinner = findViewById(R.id.mp_reciter_spinner);
        //set the adapter
        language_spinner.setAdapter(language_adapter);

        if (mSharedPref.contains(SharedPreferences.SELECTED_AUDIO_LANGUAGE)) {
            language_spinner.setSelection(mSharedPref.read(SharedPreferences.SELECTED_AUDIO_LANGUAGE, 0));
        }
        language_spinner.setOnItemSelectedListener(this);
        recitationstyle_spinner.setOnItemSelectedListener(this);
        reciter_spinner.setOnItemSelectedListener(this);

        setSwipeControls();



        //PopulateTrackList();
        handler = new Handler();
        mp_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                if (input) {
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                        SharedPreferences.getInstance().write(suranomi, progress);
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

    private void getPlayMode() {
        try {
            play_mode = mSharedPref.read(SharedPreferences.PLAYMODE, "list");
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.i(TAG, play_mode);
        } catch (NullPointerException x) {
            play_mode = "list";
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.i(TAG, play_mode + x.getMessage());
        }

        switch (play_mode) {
            case "list":
                playmode_btn.setImageResource(R.drawable.ic_playlist_play_black_24dp);
                break;
            case "repeat":
                playmode_btn.setImageResource(R.drawable.ic_repeat_one_black_24dp);
                break;
            case "all":
                playmode_btn.setImageResource(R.drawable.ic_repeat_black_24dp);
                break;
            case "one":
                playmode_btn.setImageResource(R.drawable.ic_looks_one_black_24dp);
                break;
        }
    }

    private void setNextPlayMode() {
        switch (play_mode) {
            case "list":
                play_mode = "repeat";
                playmode_btn.setImageResource(R.drawable.ic_repeat_one_black_24dp);
                break;
            case "repeat":
                play_mode = "all";
                playmode_btn.setImageResource(R.drawable.ic_repeat_black_24dp);
                break;
            case "all":
                play_mode = "one";
                playmode_btn.setImageResource(R.drawable.ic_looks_one_black_24dp);
                break;
            case "one":
                play_mode = "list";
                playmode_btn.setImageResource(R.drawable.ic_playlist_play_black_24dp);
                break;
        }
        mSharedPref.write(SharedPreferences.PLAYMODE, play_mode);
    }

    private void setSwipeControls() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //Toast.makeText(MediaActivity.this, "Track has been deleted", Toast.LENGTH_SHORT).show();
                final int position = viewHolder.getAdapterPosition();
                if (mAdapter.getDownload_view()) {
                    //download view no deleting allowed
                    mAdapter.notifyDataSetChanged();
                } else {
                    //allow delete, if not cancelled
                    //mAdapter.removeItem(position);


                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, R.string.want_to_delete, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            DeleteTheTrack(trackList.get(position).getName());
                            ChapterTitleTable ctitle = mAdapter.getTitleAt(position);
                            if (ctitle != null && ctitle.status.equals("2")) {
                                ctitle.status = "";
                                titleViewModel.update(ctitle);
                            }
                            PopulateTrackList();
                            mAdapter.notifyDataSetChanged();
                            //recyclerView.scrollToPosition(position);
                        }


                    });
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            //super.onDismissed(transientBottomBar, event);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }


    private void playTheFileIfExists(String play_item_number) {

        if (play_item_number != null) {
            suraNumber2Play = play_item_number;
            play();
        }

    }

    @Override
    protected void onStart() {
        //may have to delete this way
        super.onStart();

    }


    void previousTrack(){
        if (suraNumber2Play != null && trackList.size() > 1) {
            for(int i=0;i<trackList.size();i++){
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.e(TAG, suraNumber2Play + " " + trackList.get(i).getName());
                if (trackList.get(i).getName().equals(suraNumber2Play)) {
                    try{
                        suraNumber2Play = String.valueOf(Integer.parseInt(trackList.get(i - 1).getName()));
                        break;
                    }catch (IndexOutOfBoundsException x){
                        suraNumber2Play = null;
                    }
                }
            }
        }
    }
    void nextTrack(){
        if (suraNumber2Play != null && trackList.size() > 1) {
            for(int i=0;i<trackList.size();i++){

                if (trackList.get(i).getName().equals(suraNumber2Play)) {
                    try{
                        suraNumber2Play = String.valueOf(Integer.parseInt(trackList.get(i + 1).getName()));
                        if (BuildConfig.BUILD_TYPE.equals("debug"))
                            Log.e(TAG, suraNumber2Play + " - next suranumber");
                        break;
                    }catch (IndexOutOfBoundsException x){
                        if (play_mode.equals("all")) {
                            //go to the first file
                            suraNumber2Play = String.valueOf(Integer.parseInt(trackList.get(0).getName()));
                        } else {
                            suraNumber2Play = null;
                        }

                    }
                }
            }
        }else{
            suraNumber2Play = null;
        }
    }

    void play() {
        if (suraNumber2Play != null) {
            int tempsn = 0;
            try {
                tempsn = Integer.parseInt(suraNumber2Play);
            }catch (NumberFormatException ignored){

            }

            if(tempsn>0){
                suranomi = "(" + language + ") " + QuranMap.SURAHNAMES[Integer.parseInt(suraNumber2Play) - 1];
                current_track_tv.setText(suranomi);
                String url;
                String filePath = "";

                String path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
                newpath = path + "/quran_audio/" + language + "/by_surah/" + recitation_style + "/" + reciter;
                File directory = new File(newpath);
                File[] files = directory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        String trackname = file.getName();
                        if (trackname.contains(".")) {
                            trackname = trackname.substring(0, trackname.lastIndexOf("."));
                            if (trackname.equals(suraNumber2Play)) {
                                //filePath = new StringBuilder().append(path).append("/quran_audio/"+language + "/by_surah/" + recitation_style + "/" + reciter+"/").append(prependZero(trackname)).append(".mp3").toString();
                                filePath = newpath + "/" + suraNumber2Play + ".mp3";
                                if (BuildConfig.BUILD_TYPE.equals("debug"))
                                    Log.i(TAG, "Trackname " + trackname + " FP:" + filePath);
                            }
                        }
                    }
                }  //This surah is not available

                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.i(TAG, suraNumber2Play);

                if (TrackDownloaded(suraNumber2Play)) {
                    url = filePath;
                } else {
                    //Toast.makeText(this, "Online audio!", Toast.LENGTH_SHORT).show();
                    //url = new StringBuilder().append("https://mobilproject.github.io/furqon_web_express/by_sura/").append(suraNumber2Play).append(".mp3").toString();
                    //url = mFirebaseRemoteConfig.getString("server_link") + "/quran_audio/" + language + "/by_surah/" + recitation_style + "/" + reciter  + "/" + prependZero(suraNumber2Play) + ".mp3";
                    // /storage/emulated/0/Android/data/furqon.io.github.mobilproject/files/quran_audio/arabic/by_surah/murattal/1/001.mp3
                    // /storage/emulated/0/Android/data/furqon.io.github.mobilproject/files/quran_audio/arabic/by_surah/murattal/1
                    url = newpath + "/" + suraNumber2Play + ".mp3";

                }
                if (!url.isEmpty()) {
                    if (mediaPlayer == null)
                        mediaPlayer = new MediaPlayer();
                    else
                        mediaPlayer.reset();

                    //the result of prepareAsync()
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            //Furqon.ShowNotification(AyahList.this, R.drawable.ic_pause_circle, suranomi, audio_pos);
                            mp_seekBar.setMax(mediaPlayer.getDuration());
                            //progressBar.setVisibility(View.INVISIBLE);
                            resume();
                        }
                    });

                    try {
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
                    } catch (IOException x) {
                        if (BuildConfig.BUILD_TYPE.equals("debug"))
                            Log.e(TAG, "ERROR " + x.getMessage());
                        current_track_tv.setText("");
                        Toast.makeText(this, R.string.filenotfound, Toast.LENGTH_SHORT).show();
                        mediaPlayer.release();

                        mediaPlayer = null;
                    }
                    //progressBar.setVisibility(View.VISIBLE);


                    //mediaPlayer.start();

//            else {
//                if(isPlaying){
//                    pause();
//                }else{
//                    mediaPlayer.release();
//                    mediaPlayer = null;
//                    //mediaPlayer.start();
//                    //isPlaying = true;
//                    //resume();
//                }
//            }
                    //trackDownload = true;
                } else {
                    final PopupWindow popupWindow = new PopupWindow(this);
                    View view = getLayoutInflater().inflate(R.layout.popup_hint, coordinatorLayout);
                    popupWindow.setContentView(view);
                    //popupWindow.showAtLocation(cl, 0, 0,0);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popupWindow.dismiss();
                        }
                    });
                    //trackDownload = false;
                }
            }
        }
    }

    public void resume() {

        if (mediaPlayer != null) {

            int audio_pos;
//            if(!language.equals("arabic"))
            audio_pos = SharedPreferences.getInstance().read(suranomi, 0);
            if (audio_pos > 0 && audio_pos != mediaPlayer.getDuration()) {
                mediaPlayer.seekTo(audio_pos);
            } else {
                SharedPreferences.getInstance().write(suranomi, 1);
                mediaPlayer.seekTo(1);
                Toast.makeText(getBaseContext(), audiorestore, Toast.LENGTH_SHORT).show();
                //Snackbar.make(coordinatorLayout, audiorestore, Snackbar.LENGTH_SHORT).show();
            }
            mediaPlayer.start();
            play_btn.setImageResource(R.drawable.ic_pause_circle_60dp);
            isPlaying = true;
            playCycle();
        }
    }

    public void playCycle() {
        if (mediaPlayer != null) {
            try {
                mp_seekBar.setProgress(mediaPlayer.getCurrentPosition());
                //audio_pos = mediaPlayer.getCurrentPosition();
                //timer = findViewById(R.id.audio_timer);
                //timer.setText(AudioTimer.getTimeStringFromMs(audio_pos));

                if (mediaPlayer.isPlaying()) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            //startTimer();
                            playCycle();
                        }
                    };
                    handler.postDelayed(runnable, 1000);
                } else {
                    current_track_tv.setText("");
                    isPlaying = false;
                    mp_seekBar.setProgress(0);
                    handler.removeCallbacks(runnable);
                    SharedPreferences.getInstance().write(suranomi, 0);
                    stop();
                    if (play_mode.equals("list") || play_mode.equals("all")) {
                        nextTrack();
                        play();
                    } else if (play_mode.equals("repeat")) {
                        //playmode repeat one
                        play();
                    }  // playmode a single file
                    //stop


                }
            } catch (IllegalStateException x) {
                isPlaying = false;
                mediaPlayer.release();
                handler.removeCallbacks(runnable);

            }

        }
    }

    private void DeleteTheTrack(String tracktodelete) {
        if (tripletNotNull()) {
            String path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
            newpath = path + "/quran_audio/" + language + "/by_surah/" + recitation_style + "/" + reciter;
            File directory = new File(newpath);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(".")) {
                        String trackname = file.getName().substring(0, file.getName().lastIndexOf("."));
                        if (trackname.equals(tracktodelete)) {
                            file.delete();
                        }
                    }
                }

                mAdapter.setTitles(trackList);
                recyclerView.setAdapter(mAdapter);
            } else {
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.d(TAG, "NULL ARRAY no files found");
                mAdapter.setTitles(trackList);
                recyclerView.setAdapter(mAdapter);
            }
        }
    }
    private void PopulateTrackList() {
        trackList = new ArrayList<>();
        if (tripletNotNull()) {
            String path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();

            //TODO adding new folder structure
            newpath = path + "/quran_audio/" + language + "/by_surah/" + recitation_style + "/" + reciter;
//            File directory = new File(path);
//            File[] files = directory.listFiles();
//            if (files != null) {
//                MoveFiles(files);
//            }
            File directory = new File(newpath);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(".")) {
                        //String trackname = files[i].getName().substring(0, files[i].getName().lastIndexOf("."));
                        String trackname = file.getName();
                        trackname = trackname.substring(0, trackname.lastIndexOf("."));
                        try {
                            int tt = Integer.parseInt(trackname);
                            if (!TrackDownloaded(file.getName())) {
                                String filePath = newpath + "/" + file.getName();
                                try {
                                    metadataRetriever.setDataSource(filePath);
                                    //Date date = new Date();
                                    Track track = new Track(AudioTimer.getTimeStringFromMs(Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))), trackname, filePath);
                                    trackList.add(track);
                                } catch (RuntimeException ignored) {

                                }
                            }
                        } catch (NumberFormatException nfx) {
                            DeleteTheFile(file);
                            //TODO delete the file with x-y.mp3 naming format (dual download)
                        }
                    }
                }
                if (trackList.size() > 1){
                    try{
                        sort(trackList);
                    }catch (ClassCastException | UnsupportedOperationException | IllegalArgumentException ignore){

                    }

                    current_track_tv.setText("");
                }else if(trackList.size()==1){
                    current_track_tv.setText("");
                }
                else{
                    current_track_tv.setText(R.string.tracklist_empty_warning);
                }

                mAdapter.setTitles(trackList);
                recyclerView.setAdapter(mAdapter);
            } else {
                current_track_tv.setText(R.string.tracklist_empty_warning);
                mAdapter.setTitles(trackList);
                recyclerView.setAdapter(mAdapter);
            }
            //mAdapter.notifyDataSetChanged();
        } //Wrong path selected
        //current_track_tv.setText(R.string.tracklist_empty_warning);


    }
    private void DeleteTheFile(File file) {
        try {
            file.delete();
        } catch (SecurityException ignored) {
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void MoveFiles(File[] files) {
        //TODO only uzbek files exist in old location
        for (File file : files) {
            Path source = Paths.get(file.getPath());
            Path target = Paths.get(Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath() + "/quran_audio/uzbek/by_surah/fl/1");

            try {
                Files.move(source, target.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {

            }
        }
    }

    private String languageNo(String lan) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("english", "79");
        map.put("russian", "59");
        map.put("arabic", "1");
        map.put("uzbek", "120");

        return map.get(lan);
    }

    private void LoadTheList() {
        //TODO send language number as found in the database
        titleViewModel.getAllTitles().observe(this, new Observer<List<ChapterTitleTable>>() {
            @Override
            public void onChanged(@Nullable List<ChapterTitleTable> surahTitles) {
                assert surahTitles != null;
                if (surahTitles.size() != 114) {
                    //tempbut.setVisibility(View.VISIBLE);
                    if (!download_attempted) {
                        download_attempted = true;
                        LoadTitles();
                    }

                    //titleViewModel.deleteAll();
                }  //progressBar.setVisibility(View.GONE);

                mAdapter.setTitles(surahTitles);
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void LoadTitles() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/ajax_quran.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        // Convert String to json object
                        jsonArrayResponse = new ArrayList<JSONObject>();

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }

                            //PASS to SPINNER
                            //load auction names and available lot/bid count
                            populateAuctionList(jsonArrayResponse);
                            //progressBar.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //progressBar.setVisibility(View.INVISIBLE);
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("action", "names_as_objects"); //Add the data you'd like to send to the server.
                MyData.put("language_id", "1");
                //https://inventivesolutionste.ipage.com/ajax_quran.php
                //POST
                //action:names_as_objects
                //language_id:1
                return MyData;
            }
        };
        queue.add(stringRequest);
        //progressBar.setVisibility(View.VISIBLE);
    }

    void populateAuctionList(ArrayList<JSONObject> auclist) {

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(, android.R.layout.simple_spinner_item, auclist);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
        ChapterTitleTable title;

        for (JSONObject i : auclist
        ) {

            try {
                //int language_no = i.getInt("language_no");
                int order_no = i.getInt("order_no");
                int chapter_id = i.getInt("chapter_id");
                String surah_type = i.getString("surah_type");
                String uzbek = i.getString("uzbek");
                String arabic = i.getString("arabic");

                title = new ChapterTitleTable(1, order_no, chapter_id, uzbek, arabic, surah_type);
                titleViewModel.insert(title);


            } catch (Exception ignored) {
            }
        }
    }


    private boolean TrackDownloaded(String v) {

        boolean retval = false;
        if (trackList != null) {
            for (Track i : trackList
            ) {
                if (i.getName().equals(v)) {
                    retval = true;
                }
            }
        }

        return retval;
    }

//    private void ShowRewardAdForThisItem() {
//            //String suranomi = suraName.getText().toString();
//            mRewardedVideoAd.SHOW();
//        }

    private void setAyahCost() {
        if (suraNumber2Download != null) {
            int ayah_number = Integer.parseInt(suraNumber2Download);

            ayah_unlock_cost = QuranMap.AYAHCOUNT[ayah_number - 1];

        }
        available_coins = mSharedPref.read(mSharedPref.COINS, 0);
    }

    private boolean WritePermission() {
        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.i("MY PERMISSION TO WRITE", this + " granted?");
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_WRITE_EXTERNAL_STORAGE);
            return false;
        } else {
            // Permission has already been granted
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.i("MY PERMISSION TO WRITE", MY_WRITE_EXTERNAL_STORAGE + " already granted");
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        // other 'case' lines to check for other
        // permissions this app might request.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_WRITE_EXTERNAL_STORAGE) if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            if (suraNumber2Download != null) {
                DownloadThis(suraNumber2Download);

            }  // permission denied, boo! Disable the
            // functionality that depends on this permission.
            //LoadTheList();

        }
    }

    private String prependZero(String s) {
        String retval = s;
        switch (s.length()) {
            case 1:
                retval = "00" + s;
                break;
            case 2:
                retval = "0" + s;
                break;
            case 3:
                retval = s;
                break;
        }
        return retval;
    }

    @Override
    public void DownloadThis(String suraNumber) {
        this.suraNumber2Download = suraNumber;

        if (WritePermission()) {

            if (tripletNotNull()) {
                String middle_path = language + "/by_surah/" + recitation_style + "/" + reciter;
                //TODO new path:
            /*
            newpath = mFirebaseRemoteConfig.getString("server_link") + "/quran_audio/" + language + "/by_surah/" + recitation_style + "/" + reciter;
            */
                try{
                    int tempsn = Integer.parseInt(suraNumber2Download);
                    suranomi = QuranMap.SURAHNAMES[tempsn-1];

                } catch (NumberFormatException ignored) {

                }
                String url = mFirebaseRemoteConfig.getString("server_audio") + "/quran_audio/" + middle_path + "/" + prependZero(suraNumber2Download) + ".mp3";

                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.e(TAG, " DOWNLOAD url " + url);
                //String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suraNumber + ".mp3"; // your URL here
                newpath = getExternalFilesDir(null) + "/quran_audio/" + middle_path;
                File file = new File(newpath, suraNumber2Download + ".mp3");
                DownloadManager.Request request;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    request = new DownloadManager.Request(Uri.parse(url))
                            .setTitle(suraNumber2Download)
                            .setDescription("Downloading " + suranomi)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))
                            .setRequiresCharging(false)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                } else {
                    request = new DownloadManager.Request(Uri.parse(url))
                            .setTitle(suraNumber2Download)
                            .setDescription("Downloading " + suranomi)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                }

                if (mSharedPref.read(SharedPreferences.SIGNATURE, "ERROR").equals("OK")) {
                    if (isNetworkAvailable()) {


                        //query.setFilterById(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
                        Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING));
                        cursor.moveToFirst();
                        if (cursor.getCount() >= 1) {
                            Toast.makeText(getApplicationContext(), "Please, wait", Toast.LENGTH_SHORT).show();
                            mAdapter.notifyDataSetChanged();

                            if(!mSharedPref.read(SharedPreferences.NOMOREADS, false))
                            {
                                mInterstitialAd.show();
                            }
                        } else {
                            if (BuildConfig.BUILD_TYPE.equals("debug"))
                                Log.i("PERMISSION OK", "Download start " + suraNumber2Download);
                            downloadId = downloadManager.enqueue(request);
                            mSharedPref.write("download_" + downloadId, suraNumber2Download); //storing the download id under the right sura reference. We can use the id later to check for download status
                            mSharedPref.write("downloading_surah_" + suraNumber2Download, (int) downloadId);
                            mAdapter.notifyDataSetChanged();

                            myTimer.schedule(new TimerTask() {
                                final Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING));

                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                mAdapter.notifyDataSetChanged();
                                                cursor.moveToFirst();
                                                if (cursor == null || cursor.getCount() == 0) {
                                                    myTimer.cancel();
                                                }
                                            }catch (IllegalStateException x){
                                                if (cursor == null || cursor.getCount() == 0 && myTimer!=null) {
                                                    myTimer.cancel();
                                                }
                                            }
                                        }
                                    });
                                }
                            }, 500, 1000);
                        }
                    } else {
                        if (BuildConfig.BUILD_TYPE.equals("debug"))
                            Log.i(TAG, "NO NETWORK");
                    }
                } else {
                    if (BuildConfig.BUILD_TYPE.equals("debug"))
                        Log.i(TAG, "NO SIGNATURE");
                }

            }  //path is incomplete

        } else {
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.i("PERMISSION NG", "Download fail");
        }


    }

    private final BroadcastReceiver broadcastReceiverDownload = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

             if (id == downloadId) {
                 if (BuildConfig.BUILD_TYPE.equals("debug"))
                     Log.i(TAG, "DOWNLOAD COMPLETE Download id " + downloadId);
                //MoveFiles();

            } else {
                 if (BuildConfig.BUILD_TYPE.equals("debug"))
                     Log.i(TAG, "DOWNLOAD OTHER FILE Download id " + downloadId);
            }
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(columnReason);

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    if (BuildConfig.BUILD_TYPE.equals("debug"))
                        Log.i(TAG, "DOWNLOAD COMPLETE, Download id " + id);
                    if (tripletNotNull()) {
                        PopulateTrackList();
                        if (suraNumber2Download != null) {
                            int sn = Integer.parseInt(suraNumber2Download);
                            MarkAsDownloaded(sn);
                        }

                    }
                } else if (status == DownloadManager.STATUS_FAILED) {
                    Snackbar.make(coordinatorLayout,
                            "error " + reason,
                            Snackbar.LENGTH_LONG).show();
                    //Crashlytics.log("download error - " + reason + "->" + language + "/" + recitation_style + "/" + reciter + "/" + suraNumber2Download);
                    mAdapter.notifyDataSetChanged();
                } else if (status == DownloadManager.STATUS_PAUSED) {
                    Snackbar.make(coordinatorLayout,
                            "PAUSED!\n" + "reason of " + reason,
                            Snackbar.LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_PENDING) {
                    Snackbar.make(coordinatorLayout,
                            "PENDING!",
                            Snackbar.LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_RUNNING) {
                    Snackbar.make(coordinatorLayout,
                            "RUNNING!",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        }

    };
    @Override
    public void LoadTitlesFromServer() {

    }

    @Override
    public void insertTitle(ChapterTitleTable title) {

    }

    @Override
    public void MarkAsDownloading(int surah_id) {
        //mInterstitialAd.show();
        //TODO if quit while downloading, the progressbar is left permanently on
        if (mAdapter != null) {
            int actual_position = surah_id;
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                try {
                    if (mAdapter.getTitleAt(i) != null && mAdapter.getTitleAt(i).chapter_id == surah_id) {
                        actual_position = i;
                    }
                } catch (IndexOutOfBoundsException iobx) {
                    if (BuildConfig.BUILD_TYPE.equals("debug"))
                        Log.e("CANNOT GET POSITION", Objects.requireNonNull(iobx.getMessage()));
                }
            }
            ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
            if (ctitle != null && !ctitle.status.equals("4")) {
                ctitle.status = "4";
                titleViewModel.update(ctitle);
            }
        }
    }

    @Override
    public void MarkAsAwarded(int surah_id) {
        int actual_position = surah_id;
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            if (mAdapter.getTitleAt(i) != null && mAdapter.getTitleAt(i).chapter_id == surah_id) {
                actual_position = i;
            }
        }
        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.e(TAG, "ACTUAL SURAH ID?" + surah_id + " " + actual_position);
        ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
        if (ctitle != null) {
            ctitle.status = "2";
            titleViewModel.update(ctitle);
        }
    }

    @Override
    public void MarkAsDownloaded(int surah_id) {
        if (mAdapter != null) {
            int actual_position = surah_id;
            if (mAdapter.getItemCount() > 0) {
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    try {
                        if (mAdapter.getTitleAt(i) != null && mAdapter.getTitleAt(i).chapter_id == surah_id) {
                            actual_position = i;
                        }
                    } catch (IndexOutOfBoundsException x) {
                        //Crashlytics.log(x.getMessage() + " - " + x.getStackTrace());
                    }
                }
                ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
                if (ctitle != null && !ctitle.status.equals("3")) {
                    ctitle.status = "3";
                    titleViewModel.update(ctitle);
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

    private void storeAudioPosition() {


        //sharedPref.write(suranomi, mediaPlayer.getCurrentPosition());

        Snackbar.make(coordinatorLayout, audiostore, Snackbar.LENGTH_SHORT).show();

    }



    public void pause() {
        if (isPlaying) {
            SharedPreferences.getInstance().write(suranomi, mediaPlayer.getCurrentPosition());
            isPlaying = false;
            stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        updateUI();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            //mediaPlayer.reset();
            //progressBar.setVisibility(View.INVISIBLE);
            play_btn.setImageResource(R.drawable.ic_play_circle_48dp);
            current_track_tv.setText("");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null)
                notificationManager.cancelAll();
        }



        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (broadcastReceiverDownload != null) {
                    unregisterReceiver(broadcastReceiverDownload);
                }

            }
        } catch (IllegalArgumentException x) {
            //Crashlytics.log(x.getMessage() + " - " + Arrays.toString(x.getStackTrace()));
        }
        if(!mSharedPref.read(SharedPreferences.NOMOREADS, false))
            mInterstitialAd.show();
    }

    @Override
    public void OnTrackPrevious() {

    }

    @Override
    public void OnTrackPlay() {
        play();

    }

    @Override
    public void OnTrackNext() {

    }

    @Override
    public void OnTrackPause() {
        pause();
        play_btn.setImageResource(R.drawable.ic_play_circle_48dp);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(view!=null)
            ((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimary));
        switch (parent.getId()) {
            case R.id.mp_language_spinner:
                mSharedPref.write(SharedPreferences.SELECTED_AUDIO_LANGUAGE, position);
                language = Objects.requireNonNull(language_adapter.getItem(position)).toString();
                if (position == 0) {//TODO set index from shared pref, if previously set
                    recitationstyle_adapter = ArrayAdapter.createFromResource(this, R.array.recitation_styles_arabic, android.R.layout.simple_spinner_item);
                } else {
                    recitationstyle_adapter = ArrayAdapter.createFromResource(this, R.array.recitation_style, android.R.layout.simple_spinner_item);
                }
                recitationstyle_spinner.setVisibility(View.VISIBLE);
                recitationstyle_spinner.setAdapter(recitationstyle_adapter);
                recitationstyle_adapter.setDropDownViewResource(R.layout.mp_spinner_item);
                break;
            case R.id.mp_recitationstyle_spinner:
                mSharedPref.write(SharedPreferences.RECITATIONSTYLE, position);
                recitation_style = Objects.requireNonNull(recitationstyle_adapter.getItem(position)).toString();
                switch (Objects.requireNonNull(recitationstyle_adapter.getItem(position)).toString()) {
                    //TODO set index from shared pref, if previously set
                    case "murattal":
                        reciter_adapter = ArrayAdapter.createFromResource(this, R.array.arabic_murattal, android.R.layout.simple_spinner_item);
                        break;
                    case "mujawwad":
                        reciter_adapter = ArrayAdapter.createFromResource(this, R.array.arabic_mujawwad, android.R.layout.simple_spinner_item);
                        break;
                    case "fl":
                        switch (language) {
                            case "english":
                                reciter_adapter = ArrayAdapter.createFromResource(this, R.array.english_fl, android.R.layout.simple_spinner_item);
                                break;
                            case "russian":
                                reciter_adapter = ArrayAdapter.createFromResource(this, R.array.russian_fl, android.R.layout.simple_spinner_item);
                                break;
                            case "uzbek":
                                reciter_adapter = ArrayAdapter.createFromResource(this, R.array.uzbek_fl, android.R.layout.simple_spinner_item);
                                break;
                        }
                        break;
                }
                reciter_spinner.setVisibility(View.VISIBLE);
                reciter_spinner.setAdapter(reciter_adapter);
                reciter_adapter.setDropDownViewResource(R.layout.mp_spinner_item);

                break;
            case R.id.mp_reciter_spinner:
                mSharedPref.write(SharedPreferences.RECITER, position);
                //reciter = Objects.requireNonNull(reciter_adapter.getItem(position)).toString();
                reciter = String.valueOf(position + 1);
                break;
        }
        pause();
        PopulateTrackList();
        if (!mAdapter.getDownload_view()) {
            if(tripletNotNull()){
                Bundle intent = getIntent().getExtras();
                String play_item_number;
                if (intent != null) {
                    try {
                        play_item_number = intent.getString("suranumber");
                        getIntent().removeExtra("suranumber");
                        playTheFileIfExists(play_item_number);
                    } catch (NullPointerException x) {
                        //Crashlytics.log(x.getMessage() + " - " + Arrays.toString(x.getStackTrace()));
                    }
                }
            }

        }

    }

    private boolean tripletNotNull() {
        return language != null && recitation_style != null && reciter != null;

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mp_imageButton_dl:
                //download  view
                mAdapter.setDownload_view(true);
//                bouncer = new AnimatorSet();
//                ValueAnimator fadeAnim = ObjectAnimator.ofFloat(media_player_ll, "alpha", 100f, 0f);
//                ValueAnimator fadeAnim1 = ObjectAnimator.ofFloat(mp_seekBar, "alpha", 100f, 0f);
//                ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(special_actions_ll, "alpha", 100f, 0f);
//                bouncer.play(fadeAnim).with(fadeAnim1);
//                bouncer.play(fadeAnim2).with(fadeAnim1);
//                fadeAnim.setDuration(250);
//                animatorSet = new AnimatorSet();
//                animatorSet.start();
                special_actions_ll.setVisibility(View.GONE);
                mp_seekBar.setVisibility(View.GONE);
                media_player_ll.setVisibility(View.GONE);

                if (mSharedPref.read(mSharedPref.COINS, 0) > 0) {
                    updateUI();
                }

                LoadTheList();

                break;
            case R.id.mp_imageButton_pl:
                //playlist view
                current_track_tv.setText("");
                mAdapter.setDownload_view(false);

//                bouncer = new AnimatorSet();
//                fadeAnim = ObjectAnimator.ofFloat(media_player_ll, "alpha", 0f, 100f);
//                fadeAnim1 = ObjectAnimator.ofFloat(mp_seekBar, "alpha", 0f, 100f);
//                fadeAnim2 = ObjectAnimator.ofFloat(special_actions_ll, "alpha", 0f, 100f);
//                bouncer.play(fadeAnim).with(fadeAnim1);
//                bouncer.play(fadeAnim2).with(fadeAnim1);
//                fadeAnim.setDuration(250);
//                animatorSet = new AnimatorSet();
//                animatorSet.start();
                special_actions_ll.setVisibility(View.VISIBLE);
                media_player_ll.setVisibility(View.VISIBLE);
                mp_seekBar.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.mp_play_toggle:
                if (isPlaying) {
                    OnTrackPause();
                } else {
                    OnTrackPlay();
                }
                break;
            case R.id.mp_previous:
                previousTrack();
                if (isPlaying) {
                    OnTrackPause();
                }
                OnTrackPlay();
                break;
            case R.id.mp_next:
                nextTrack();
                if (isPlaying) {
                    OnTrackPause();
                }
                OnTrackPlay();
                break;
            case R.id.mp_playmode_btn:
                setNextPlayMode();
                break;
        }

    }

    private void updateUI() {
        //current_track_tv.setText(getResources().getText(R.string.coins) + ": " + mSharedPref.read(mSharedPref.COINS, 0));
    }

    @Override
    public void SetSurahNumber(String s) {
        suraNumber2Play = s;
    }

    @Override
    public void SetDownloadIconState(boolean b) {
        mAdapter.setVideoAdLoaded(b);
        mAdapter.notifyDataSetChanged();
    }
}
