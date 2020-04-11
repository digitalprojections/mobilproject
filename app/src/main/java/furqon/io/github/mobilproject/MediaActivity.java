package furqon.io.github.mobilproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import furqon.io.github.mobilproject.Services.OnClearFromService;

public class MediaActivity extends AppCompatActivity implements MyListener, ManageCoins, Playable, AdapterView.OnItemSelectedListener, View.OnClickListener, SetSuraNumber {
    private static final int MY_WRITE_EXTERNAL_STORAGE = 101;
    private static final String TAG = "MediaActivity";
    private ArrayList<Track> trackList;
    private TitleViewModel titleViewModel;
    private MediaActivityAdapter mAdapter;
    private RecyclerView recyclerView;
    private Spinner language_spinner;
    private Spinner recitationstyle_spinner;
    private Spinner reciter_spinner;
    private SpinnerAdapter spinnerAdapter;
    private ArrayAdapter<CharSequence> language_adapter;
    private ArrayAdapter<CharSequence> recitationstyle_adapter;
    private ArrayAdapter<CharSequence> reciter_adapter;
    private MediaPlayer mediaPlayer;



    private Context context;
    String newpath;
    long downloadId;
    ImageButton dl_view_btn;
    ImageButton pl_view_btn;
    ImageView play_btn;
    ImageView previous_btn;
    ImageView next_btn;


    //TODO create LL vars
    View special_actions_ll;
    View media_player_ll;
    SeekBar mp_seekBar;

    TextView current_track_tv;
    String suraNumber;
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
    InterstitialAd mInterstitialAd;
    Handler handler;
    private NotificationManager notificationManager;
    boolean isPlaying;

    private SharedPreferences mSharedPref;
    //ProgressBar progressBar;
    private Runnable runnable;
    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
    DownloadManager downloadManager;
    DownloadManager.Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        titleViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);

        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE == "debug") {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        audiorestore = getString(R.string.audiopos_restored);
        audiostore = getString(R.string.audiopos_stored);
        loadfailed = getString(R.string.audio_load_fail);

        dl_view_btn = findViewById(R.id.mp_imageButton_dl);
        pl_view_btn = findViewById(R.id.mp_imageButton_pl);

        play_btn = findViewById(R.id.mp_play_toggle);
        previous_btn = findViewById(R.id.mp_previous);
        next_btn = findViewById(R.id.mp_next);

        current_track_tv = findViewById(R.id.mp_current_title_tv);
        current_track_tv.setText("");

        dl_view_btn.setOnClickListener(this);
        pl_view_btn.setOnClickListener(this);
        play_btn.setOnClickListener(this);
        previous_btn.setOnClickListener(this);
        next_btn.setOnClickListener(this);

        media_player_ll = findViewById(R.id.mp_player_ll);
        special_actions_ll = findViewById(R.id.mp_actions_ll);
        mp_seekBar = findViewById(R.id.mp_seekBar);

        mSharedPref = SharedPreferences.getInstance();
        mSharedPref.init(getApplicationContext());


        //registerReceiver(broadcastReceiverAudio, new IntentFilter("TRACKS_TRACKS"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiverDownload, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            startService(new Intent(getBaseContext(), OnClearFromService.class));
        }

        downloadManager = (DownloadManager) this.getSystemService(this.DOWNLOAD_SERVICE);
        query = new DownloadManager.Query();

        trackList = new ArrayList<Track>();

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
        language_spinner = findViewById(R.id.mp_language_spinner);
        recitationstyle_spinner = findViewById(R.id.mp_recitationstyle_spinner);
        reciter_spinner = findViewById(R.id.mp_reciter_spinner);
        //set the adapter
        language_spinner.setAdapter(language_adapter);

        if(mSharedPref.contains(mSharedPref.SELECTED_AUDIO_LANGUAGE)){
            language_spinner.setSelection(mSharedPref.read(mSharedPref.SELECTED_AUDIO_LANGUAGE, 0));
        }
        language_spinner.setOnItemSelectedListener(this);
        recitationstyle_spinner.setOnItemSelectedListener(this);
        reciter_spinner.setOnItemSelectedListener(this);

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

    private void playTheFileIfExists(String play_item_number) {
        suraNumber = play_item_number;

        play();
    }

    @Override
    protected void onStart() {
        //may have to delete this way
        super.onStart();

    }


    void previousTrack(){
        if(suraNumber!=null && trackList.size()>1){
            for(int i=0;i<trackList.size();i++){
                Log.e(TAG, suraNumber + " " + trackList.get(i).getName());
                if(trackList.get(i).getName().equals(prependZero(suraNumber))){
                    try{
                        suraNumber = String.valueOf(Integer.parseInt(trackList.get(i-1).getName()));
                        break;
                    }catch (IndexOutOfBoundsException iobx){
                        suraNumber=null;
                    }
                }
            }
        }
    }
    void nextTrack(){
        if(suraNumber!=null && trackList.size()>1){
            for(int i=0;i<trackList.size();i++){

                if(trackList.get(i).getName().equals(prependZero(suraNumber))){
                    try{
                        suraNumber = String.valueOf(Integer.parseInt(trackList.get(i+1).getName()));
                        Log.e(TAG, suraNumber + " - next suranumber");
                        break;
                    }catch (IndexOutOfBoundsException iobx){
                        suraNumber=null;
                        Log.e(TAG, suraNumber + " - next suranumber");
                    }
                }
            }
        }
    }

    void play() {
        if(suraNumber!=null) {
            suranomi = "(" + language + ") " + QuranMap.SURAHNAMES[Integer.parseInt(suraNumber) - 1];
            current_track_tv.setText(suranomi);
            String url;
            String filePath = "";

            String path = getExternalFilesDir(null).getAbsolutePath();
            newpath = path + "/quran_audio/" + language + "/by_surah/" + recitation_style + "/" + reciter;
            File directory = new File(newpath);
            File[] files = directory.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    String trackname = files[i].getName();
                    if (trackname.contains(".")) {
                        trackname = trackname.substring(0, trackname.lastIndexOf("."));
                        if (suraNumber != null && trackname.equals(prependZero(suraNumber))) {
                            //filePath = new StringBuilder().append(path).append("/quran_audio/"+language + "/by_surah/" + recitation_style + "/" + reciter+"/").append(prependZero(trackname)).append(".mp3").toString();
                            filePath = newpath + "/" + prependZero(suraNumber) + ".mp3";
                            Log.i(TAG, "Trackname " + trackname + " FP:" + filePath);
                        }
                    }
                }
            } else {
                //This surah is not available
            }
            Log.i(TAG, suraNumber);

            if (TrackDownloaded(suraNumber)) {
                url = filePath;
            } else {
                Toast.makeText(this, "Online audio!", Toast.LENGTH_SHORT).show();
                //url = new StringBuilder().append("https://mobilproject.github.io/furqon_web_express/by_sura/").append(suraNumber).append(".mp3").toString();
                //url = "https://inventivesolutionste.ipage.com/quran_audio/" + language + "/by_surah/" + recitation_style + "/" + reciter  + "/" + prependZero(suraNumber) + ".mp3";
                // /storage/emulated/0/Android/data/furqon.io.github.mobilproject/files/quran_audio/arabic/by_surah/murattal/1/001.mp3
                // /storage/emulated/0/Android/data/furqon.io.github.mobilproject/files/quran_audio/arabic/by_surah/murattal/1
                url = newpath + "/" + prependZero(suraNumber) + ".mp3";

            }
            //Log.i(TAG, "PLAY " + url);
            if (!url.isEmpty()) {
                Log.i(TAG, "PLAY " + url);


                //resume();


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
                } catch (IOException iox) {
                    Log.e(TAG, "ERROR " + iox.getMessage());
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                //progressBar.setVisibility(View.VISIBLE);


                //mediaPlayer.start();

//            else {
//                Log.i(TAG, "Playing");
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
                View view = getLayoutInflater().inflate(R.layout.popup_hint, null);
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

    public void resume() {

        if (mediaPlayer != null) {

            int audio_pos = 0;
//            if(!language.equals("arabic"))
            audio_pos = SharedPreferences.getInstance().read(suranomi, 0);
            if (audio_pos > 0 && audio_pos != mediaPlayer.getDuration()) {
                mediaPlayer.seekTo(audio_pos);
            } else {
                SharedPreferences.getInstance().write(suranomi, 1);
                mediaPlayer.seekTo(1);
                Toast.makeText(getBaseContext(), audiorestore, Toast.LENGTH_SHORT).show();
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
                            //Log.i(TAG, "TIMER tick");
                        }
                    };
                    handler.postDelayed(runnable, 1000);
                } else {
                    //get the position of the item in tracklist

                    Log.e(TAG, "PLAYING STOPPED");
                    current_track_tv.setText("");
                    isPlaying = false;
                    mp_seekBar.setProgress(0);
                    handler.removeCallbacks(runnable);
                    SharedPreferences.getInstance().write(suranomi, 0);
                    stop();
                    nextTrack();
                    play();
                }
            } catch (IllegalStateException isx) {
                isPlaying = false;
                mediaPlayer.release();
                handler.removeCallbacks(runnable);
                Log.e(TAG, isx.getMessage() + "TIMER STOPPED on error???");
            }

        }
    }
    private void PopulateTrackList() {
        trackList = new ArrayList<Track>();
        if (tripletNotNull()) {
            String path = getExternalFilesDir(null).getAbsolutePath();
            //Log.d(TAG, "Files Path: " + path);
            //TODO adding new folder structure
            newpath = path + "/quran_audio/" + language + "/by_surah/" + recitation_style + "/" + reciter;
            Log.d(TAG, "Files Path: " + newpath);
//            File directory = new File(path);
//            File[] files = directory.listFiles();
//            if (files != null) {
//                Log.d(TAG, "MOVE FILES count: " + files.length);
//                MoveFiles(files);
//            }
            File directory = new File(newpath);
            File[] files = directory.listFiles();
            if (files != null) {
                Log.e(TAG, "Files were moved successfully");
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().contains(".")) {
                        String trackname = files[i].getName().substring(0, files[i].getName().lastIndexOf("."));
                        if (!TrackDownloaded(files[i].getName())) {
                            String filePath = new StringBuilder().append(newpath).append("/").append(files[i].getName()).toString();
                            metadataRetriever.setDataSource(filePath);

                            //Date date = new Date();
                            Track track = new Track(AudioTimer.getTimeStringFromMs(Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))), trackname, filePath);
                            trackList.add(track);
                        }
                        //Log.i(TAG, "number " + trackname + " track is available");
                    }
                }
                mAdapter.setTitles(trackList);
                recyclerView.setAdapter(mAdapter);
            } else {
                Log.d(TAG, "NULL ARRAY no files found");
                mAdapter.setTitles(trackList);
                recyclerView.setAdapter(mAdapter);
            }
        }
    }

    private void MoveFiles(File[] files) {
        //TODO only uzbek files exist in old location
        for (int i = 0; i < files.length; i++) {
            Path source = Paths.get(files[i].getPath());
            Path target = Paths.get(getExternalFilesDir(null).getAbsolutePath() + "/quran_audio/uzbek/by_surah/fl/1");
            Log.i(TAG, "file moved from" + source);
            Log.i(TAG, "file moved to " + target);
            try {
                Files.move(source, target.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException iox) {
                Crashlytics.log(iox.getMessage());
            }
        }
    }
    private void LoadTheList() {


        titleViewModel.getAllTitles().observe(this, new Observer<List<ChapterTitleTable>>() {
            @Override
            public void onChanged(@Nullable List<ChapterTitleTable> surahTitles) {
                //Toast.makeText(SuraNameList.this, "LOADING TITLES " + surahTitles.size(), Toast.LENGTH_LONG).show();
                if (surahTitles.size() != 114) {

                } else {
                    //progressBar.setVisibility(View.GONE);
                }
                mAdapter.setTitles(surahTitles);
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        });
    }
/*

    BroadcastReceiver broadcastReceiverAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");

            switch (action) {
                case Furqon
                        .ACTION_PREV:
                    OnTrackPrevious();
                    Toast.makeText(context, "PREVCLICKED", Toast.LENGTH_SHORT).show();
                    break;
                case Furqon
                        .ACTION_PLAY:
                    if (isPlaying) {
                        OnTrackPause();
                    } else {
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
*/

    private void StartDownload() {
        //DownloadThis(suraNumber);

    }

    private boolean TrackDownloaded(String v) {
        v = prependZero(v);
        boolean retval = false;
        if (trackList != null) {
            for (Track i : trackList
            ) {
                if (i.getName().equals(v)) {
                    //match found
                    Log.i("TRACK DOWNLOADED?", String.valueOf(v) + " " + i + " " + (i.getName().equals(v)));
                    retval = true;
                }
                //Log.i("TRACK DOWNLOADED????", String.valueOf(v) + " " + i.getName());
            }
        }

        return retval;
    }

//    private void ShowRewardAdForThisItem() {
//            //String suranomi = suraName.getText().toString();
//            mRewardedVideoAd.SHOW();
//        }

    private void setAyahCost() {
        int ayah_number = Integer.parseInt(suraNumber);
        if (status == 0 && ayah_number > 0) {
            ayah_unlock_cost = QuranMap.AYAHCOUNT[ayah_number - 1];
        } else {
            if (status == 0)
                ayah_unlock_cost = 10;//default
            else
                ayah_unlock_cost = 0; //no need
        }
        available_coins = mSharedPref.read(mSharedPref.COINS, 0);
    }

    private boolean WritePermission() {
        Log.i("MY PERMISSION TO WRITE", this + " granted?");
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_WRITE_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.


            // Permission is not granted
            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(getParent(),
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                Log.i(  "MY PERMISSION TO WRITE", MY_WRITE_EXTERNAL_STORAGE + " explain");
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(getParent(),
//                        new String[]{Manifest.permission.READ_CONTACTS},
//                        MY_WRITE_EXTERNAL_STORAGE);
//                Log.i(  "MY PERMISSION TO WRITE", MY_WRITE_EXTERNAL_STORAGE + " request");
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }

            return false;
        } else {
            // Permission has already been granted
            Log.i("MY PERMISSION TO WRITE", MY_WRITE_EXTERNAL_STORAGE + " already granted");
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if(suraNumber!=null)
                        DownloadThis(suraNumber);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //LoadTheList();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
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

        if (WritePermission()) {

            if (tripletNotNull()) {
                String middle_path = language + "/by_surah/" + recitation_style + "/" + reciter;
                //TODO new path:
            /*
            newpath = "https://inventivesolutionste.ipage.com/quran_audio/" + language + "/by_surah/" + recitation_style + "/" + reciter;
            */

                String url = "https://inventivesolutionste.ipage.com/quran_audio/" + middle_path + "/" + prependZero(suraNumber) + ".mp3";
                Log.e(TAG, newpath);

                //String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suraNumber + ".mp3"; // your URL here
                newpath = getExternalFilesDir(null) + "/quran_audio/" + middle_path;
                File file = new File(newpath, prependZero(suraNumber) + ".mp3");
                DownloadManager.Request request;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    request = new DownloadManager.Request(Uri.parse(url))
                            .setTitle(suraNumber)
                            .setDescription("Downloading " + suranomi)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))
                            .setRequiresCharging(false)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                } else {
                    request = new DownloadManager.Request(Uri.parse(url))
                            .setTitle(suraNumber)
                            .setDescription("Downloading " + suranomi)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                }

                Log.i("PERMISSION OK", "Download start " + url);
                DownloadManager downloadManager = (DownloadManager) this.getSystemService(this.DOWNLOAD_SERVICE);
                downloadId = downloadManager.enqueue(request);
                //MarkAsDownloading(Integer.parseInt(suraNumber));
            } else {
                //path is incomplete
            }
        } else {
            Log.i("PERMISSION NG", "Download fail");
        }


    }

    private BroadcastReceiver broadcastReceiverDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

             if (id == downloadId) {
                Log.i(TAG, "DOWNLOAD COMPLETE Download id " + downloadId);
                //MoveFiles();

            } else {
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
                    Log.i(TAG, "DOWNLOAD COMPLETE, Download id " + id);
                    //Retrieve the saved download id
//                    String suraid = mSharedPref.read("download_" + id, "0");
//                    if (Integer.parseInt(suraid) > 0) {
//                        mSharedPref.write("download_" + id, "0");
//                        //PopulateTrackList();
//                    }
                    if (tripletNotNull()) {
                        PopulateTrackList();
                        if (suraNumber != null) {
                            int sn = Integer.parseInt(suraNumber);
                            MarkAsDownloaded(sn);
                        }

                    }
                } else if (status == DownloadManager.STATUS_FAILED) {
                    Toast.makeText(MediaActivity.this,
                            "FAILED!\n" + "reason of " + reason,
                            Toast.LENGTH_LONG).show();

                } else if (status == DownloadManager.STATUS_PAUSED) {
                    Toast.makeText(MediaActivity.this,
                            "PAUSED!\n" + "reason of " + reason,
                            Toast.LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_PENDING) {
                    Toast.makeText(MediaActivity.this,
                            "PENDING!",
                            Toast.LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_RUNNING) {
                    Toast.makeText(MediaActivity.this,
                            "RUNNING!",
                            Toast.LENGTH_LONG).show();
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
                    if (mAdapter.getTitleAt(i).chapter_id == surah_id) {
                        actual_position = i;
                    }
                } catch (IndexOutOfBoundsException iobx) {
                    Log.e("CANNOT GET POSITION", iobx.getMessage());
                }
            }
//            ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
//            if (!ctitle.status.equals("4")) {
//                ctitle.status = "4";
//                titleViewModel.update(ctitle);
//            }
        }
    }

    @Override
    public void MarkAsAwarded(int surah_id) {
        //Log.e("ACTUAL SURAH ID?", surah_id + " " + suraNumber);

        int coins = mSharedPref.read(mSharedPref.COINS, 0);
        if (coins > ayah_unlock_cost) {
            titleViewModel.updateTitleAsRewarded(suraNumber);
        } else {
            Toast.makeText(this, R.string.not_enough_coins, Toast.LENGTH_LONG).show();
        }
        //downloadText.setText(R.string.down_or_play);
        //playButton.setVisible(true);

    }

    @Override
    public void MarkAsDownloaded(int surah_id) {
        if (mAdapter != null) {
            int actual_position = surah_id;
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                try {
                    if (mAdapter.getTitleAt(i).chapter_id == surah_id) {
                        actual_position = i;
                    }
                } catch (IndexOutOfBoundsException iobx) {
                    Log.e("CANNOT GET POSITION", iobx.getMessage());
                }
            }
//            ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
//            if (!ctitle.status.equals("3")) {
//                ctitle.status = "3";
//                titleViewModel.update(ctitle);
//            }
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

        Toast.makeText(getBaseContext(), audiostore, Toast.LENGTH_SHORT).show();

    }



    @Override
    public void SetCoinValues() {
        Log.d("AYAHLIST:", "setcoinsvalue");


        available_coins = mSharedPref.read(mSharedPref.COINS, 0);


        //int unlock_cost = Integer.parseInt();
        //int total_coins = Integer.parseInt(mycoins) - unlock_cost;

//        if(coins_txt!=null){
//            cost_txt.setText("0");
//            coins_txt.setText(mycoins);
//        }else{
//            TextView cost_txt = findViewById(R.id.required_value_textView);
//            TextView coins_txt = findViewById(R.id.exchange_coins_textView);
//            cost_txt.setText(ayah_unlock_cost);
//            coins_txt.setText(mycoins);
//        }
    }

    @Override
    public void UseCoins(int val) {

        Log.d("AYAHLIST:", "usecoins " + val);
    }

    @Override
    public void EarnCoins() {
        Intent intent;
        intent = new Intent(context, EarnCoinsActivity.class);
        startActivity(intent);
    }

    private void ShowCoinAlert() {
        //SetCoinValues();
        //setAyahCost();
        available_coins = mSharedPref.read(mSharedPref.COINS, 0);
        CoinDialog coinDialog = new CoinDialog(ayah_unlock_cost, available_coins);
        coinDialog.show(getSupportFragmentManager(), "TAG");

//        Button use_coins_btn = findViewById(R.id.use_coin_btn);
//        Button earn_coins_btn = findViewById(R.id.use_coin_btn);
//        Button dismiss_btn = findViewById(R.id.dismiss_button);

//        use_coins_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MarkAsAwarded(Integer.parseInt(suraNumber));
//            }
//        });
//
//        earn_coins_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//                Intent intent;
//                intent = new Intent(context, EarnCoinsActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        dismiss_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
    }
/*
    private void SetDownloadButtonState(ChapterTitleTable titleTable) {


        PopulateTrackList();

        if (TrackDownloaded(suraNumber)) {
            //set by the actually available audio files
            //playButton.setIcon(R.drawable.ic_play_circle);
            //Log.i("TITLES", " TRUE ");
            //downloadButton.setFocusable(false);
            download_container.setVisibility(View.INVISIBLE);
            //downloadText.setText(R.string.play_local);
            //downloadText.setVisibility(View.VISIBLE);
            downloadButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            if (titleTable != null && titleTable.status.equals("2")) {
                //download allowed. Active within the session only. Forgotten on restart
                downloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);

                //playButton.setIcon(R.drawable.ic_play_circle);
                downloadButton.setFocusable(true);
                downloadButton.setTag(2);
                downloadButton.setVisibility(View.VISIBLE);
                progressBarDownload.setVisibility(View.INVISIBLE);
                //downloadText.setText(R.string.down_or_play);
            } else if (titleTable != null && titleTable.status.equals("4")) {
                //download allowed. Active within the session only. Forgotten on restart
                //downloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                //playButton.setIcon(R.drawable.ic_play_circle);
                //downloadButton.setFocusable(true);
                downloadButton.setTag(4);
                progressBarDownload.setVisibility(View.VISIBLE);
                //downloadText.setText(R.string.down_or_play);
            } else {
                //Lock state

                downloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                downloadButton.setFocusable(true);
                downloadButton.setTag(2);
                downloadButton.setVisibility(View.VISIBLE);
                progressBarDownload.setVisibility(View.INVISIBLE);
                //downloadText.setText(R.string.unlock_or_play);
            }
        }


        Log.i("DOWNLOAD BUTTON", " " + downloadButton.getTag());

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(mContext,"Download surah number " + suraNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                //String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suranomer + ".mp3"; // your URL here
                switch (downloadButton.getTag().toString()) {
                    case "1"://red arrow
                        ShowCoinAlert();

//                            final PopupWindow popupWindow = new PopupWindow(getApplicationContext());
//                            View pop_view = getLayoutInflater().inflate(R.layout.popup_hint, null);
//                            popupWindow.setContentView(pop_view);
//                            popupWindow.showAtLocation(cl, 0, 0,0);
//                            pop_view.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View viewx) {
//                                    popupWindow.dismiss();
//                                }
//                            });
                        break;
                    case "2"://blue arrow
                        StartDownload();
                        break;
                }

            }
        });
        progressBarDownload.setVisibility(View.GONE);
        //recyclerView.scheduleLayoutAnimation();
    }*/

    //    public void pause() {
//        if (isPlaying) {
//            SharedPreferences.getInstance().write(suranomi, mediaPlayer.getCurrentPosition());
//            isPlaying = false;
//            Intent intent = new Intent(this, AudioService.class);
//        }
//    }
    public void pause() {
        if (isPlaying) {
            SharedPreferences.getInstance().write(suranomi, mediaPlayer.getCurrentPosition());
            isPlaying = false;
            stop();
        }
    }

    public void stop() {
        Log.i("STOP", "stop");
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
//            if (broadcastReceiverAudio != null) {
//                unregisterReceiver(broadcastReceiverAudio);
//            }
        } catch (IllegalArgumentException iax) {

        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (broadcastReceiverDownload != null) {
                    unregisterReceiver(broadcastReceiverDownload);
                }

            }
        } catch (IllegalArgumentException iax) {
            Crashlytics.log("MEDIAACTIVITY " + iax.getMessage() + iax.getStackTrace());
        }
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
        ((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimary));
        Log.d(TAG, parent.getId() + " " + R.id.mp_language_spinner);

        switch (parent.getId()) {
            case R.id.mp_language_spinner:
                mSharedPref.write(SharedPreferences.SELECTED_AUDIO_LANGUAGE, position);
                language = language_adapter.getItem(position).toString();
                switch (position) {
                    case 0:
                        //TODO set index from shared pref, if previously set
                        recitationstyle_adapter = ArrayAdapter.createFromResource(this, R.array.recitation_styles_arabic, android.R.layout.simple_spinner_item);
                        break;
                    default:
                        recitationstyle_adapter = ArrayAdapter.createFromResource(this, R.array.recitation_style, android.R.layout.simple_spinner_item);
                        break;
                }
                recitationstyle_spinner.setVisibility(View.VISIBLE);
                recitationstyle_spinner.setAdapter(recitationstyle_adapter);
                recitationstyle_adapter.setDropDownViewResource(R.layout.mp_spinner_item);
                if(mSharedPref.contains(mSharedPref.RECITATIONSTYLE)){
                    recitationstyle_spinner.setSelection(mSharedPref.read(mSharedPref.RECITATIONSTYLE, 0));
                }
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
                if(mSharedPref.contains(mSharedPref.RECITER)){
                    reciter_spinner.setSelection(mSharedPref.read(mSharedPref.RECITER, 0));
                }
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
                    } catch (NullPointerException npx) {
                        Log.e(TAG, npx.getMessage());
                    }
                }
            }

        } else {

        }

    }

    private boolean tripletNotNull() {
        if(language!=null && recitation_style!=null && reciter!=null){
            return true;
        }else{
            return false;
        }

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
                special_actions_ll.setVisibility(View.GONE);
                media_player_ll.setVisibility(View.GONE);
                mp_seekBar.setVisibility(View.GONE);
                LoadTheList();

                break;
            case R.id.mp_imageButton_pl:
                //playlist view
                mAdapter.setDownload_view(false);
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
        }

    }

    @Override
    public void SetSurahNumber(String s) {
        suraNumber = s;
    }
}
