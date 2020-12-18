package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import furqon.io.github.mobilproject.Services.OnClearFromService;

import static com.google.android.material.snackbar.Snackbar.*;

public class MemorizeActivity extends AppCompatActivity implements View.OnClickListener, MyListener, AdapterView.OnItemSelectedListener, Playable {
    private static final int MY_WRITE_EXTERNAL_STORAGE = 101;
    public static final String TAG = MemorizeActivity.class.getSimpleName();

    //DEFINE UI ELEMENTS
    private Spinner suranames_spinner;
    private ImageButton playVerse;
    private Button commitBtn;
    private TextView startValue;
    private TextView endValue;
    private TextView repeatValue;
    private ProgressBar progressBar;

    //DATA
    private TitleViewModel ayahViewModel;
    private ArrayList<Track> trackList;
    private ArrayAdapter<CharSequence> language_adapter;
    private MediaPlayer mediaPlayer;
    private ArrayList<JSONObject> jsonArrayResponse;

    private MemorizeActivityAdapter mAdapter;
    private LinearLayout coordinatorLayout;
    private Integer lastSurahIndex = 0;


    SharedPreferences sharedPreferences;
    private String startAyahNumber = "1";
    private String endAyahNumber = "2";
    private String preferredRepeatCount = "10";
    private int repeatCountInteger = 10;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Context context;
    private String suraNumber;
    private String suraNumber2Play;
    private String newpath;
    private boolean isPlaying;
    private Runnable runnable;
    private Handler handler;
    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
    private DownloadManager downloadManager;
    DownloadManager.Query query;
    private boolean download_attempted;
    Timer myTimer = new Timer();
    long downloadId;
    private boolean RANGEISSHOWN;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize);
        sharedPreferences = SharedPreferences.getInstance();
        sharedPreferences.init(getApplicationContext());
        //DONE restore the last state
        //There was a surah selected

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.memorizer);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
        query = new DownloadManager.Query();

        ayahViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        //INITIALIZE UI ELEMENTS
        suranames_spinner = findViewById(R.id.surah_spinner);
        Button dl_audio = findViewById(R.id.download_audio_button);
        playVerse = findViewById(R.id.play_verse);
        ImageButton decRepeat = findViewById(R.id.dec_repeat);
        ImageButton incRepeat = findViewById(R.id.inc_repeat);

        ImageButton incStart = findViewById(R.id.inc_start);
        ImageButton decStart = findViewById(R.id.dec_start);
        ImageButton incEnd = findViewById(R.id.inc_end);
        ImageButton decEnd = findViewById(R.id.dec_end);

        coordinatorLayout = findViewById(R.id.mem_main_lin_layout);

        commitBtn = findViewById(R.id.commit_btn);

        startValue = findViewById(R.id.start_tv);
        endValue = findViewById(R.id.end_tv);
        repeatValue = findViewById(R.id.repeat_count_tv);


        RecyclerView recyclerView = findViewById(R.id.memorize_range_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MemorizeActivityAdapter(this);
        recyclerView.setAdapter(mAdapter);
        handler = new Handler();

        progressBar = findViewById(R.id.progressBarMemorize);
        progressBar.setVisibility(View.GONE);

        //UI ACTION
        dl_audio.setOnClickListener(this);
        playVerse.setOnClickListener(this);
        decRepeat.setOnClickListener(this);
        incRepeat.setOnClickListener(this);
        decStart.setOnClickListener(this);
        incStart.setOnClickListener(this);
        decEnd.setOnClickListener(this);
        incEnd.setOnClickListener(this);
        commitBtn.setOnClickListener(this);

        suranames_spinner.setOnItemSelectedListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiverDownload, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            try {
                startService(new Intent(getBaseContext(), OnClearFromService.class));
            } catch (IllegalStateException | SecurityException x) {
                Log.e(TAG, x.getMessage());
            }

        }
        /*DONE end number never lower than the start
           if start number entered and it is higher than the end number, set the end number
            equal to the start number. But if the start number is changed to a lower value,
            reset the end number back to what it was before
         */
        //DONE
        populateSpinner();
    }
    private BroadcastReceiver broadcastReceiverDownload = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(columnReason);

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    //Retrieve the saved download id
                    /*
                    there are multiple files being downloaded.
                    */
                    String ayahNumber2Download = sharedPreferences.read("download_" + id, "0");
                    if (Integer.parseInt(ayahNumber2Download) > 0) {
                        sharedPreferences.write("download_" + id, "0");
                        PopulateTrackList();
                    }
                    Log.i(TAG, "DOWNLOAD COMPLETE, Download id " + id + " ayah number: " + ayahNumber2Download);
                        //PopulateTrackList();

                    MarkAyahAsDownloaded(ayahNumber2Download);



                } else if (status == DownloadManager.STATUS_FAILED) {
                    make(coordinatorLayout,
                            "error " + reason,
                            LENGTH_LONG).show();
                    //Crashlytics.log("download error - " + reason + "->" + language + "/" + recitation_style + "/" + reciter + "/" + suraNumber2Download);
                    mAdapter.notifyDataSetChanged();
                } else if (status == DownloadManager.STATUS_PAUSED) {
                    make(coordinatorLayout,
                            "PAUSED!\n" + "reason of " + reason,
                            LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_PENDING) {
                    make(coordinatorLayout,
                            "PENDING!",
                            LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_RUNNING) {
                    make(coordinatorLayout,
                            "RUNNING!",
                            LENGTH_LONG).show();
                }
            }
        }

    };
    private void populateSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.suranames, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        suranames_spinner.setAdapter(adapter);
        lastSurahIndex = sharedPreferences.read(SharedPreferences.SELECTED_MEMORIZING_SURAH, 0);
        suranames_spinner.setSelection(lastSurahIndex);
        setUIValues();

        if(sharedPreferences.contains(SharedPreferences.PREFERRED_REPEAT_COUNT))
            preferredRepeatCount = sharedPreferences.read(SharedPreferences.PREFERRED_REPEAT_COUNT, "10");
        repeatCountInteger = Integer.parseInt(preferredRepeatCount);
        repeatValue.setText(preferredRepeatCount);
    }

    private void setUIValues() {


        startAyahNumber = sharedPreferences.read(lastSurahIndex + "_start", "1");
        endAyahNumber = sharedPreferences.read(lastSurahIndex + "_end", "2");

        Log.d(TAG, lastSurahIndex + " last surah index, start:" + startAyahNumber + "-end:" + endAyahNumber);

        startValue.setText(startAyahNumber);
        endValue.setText(endAyahNumber);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dec_start:
                adjustHighLowStart(-1);
                break;
            case R.id.inc_start:
                adjustHighLowStart(1);
                break;
            case R.id.dec_end:
                adjustHighLowEnd(-1);
                break;
            case R.id.inc_end:
                adjustHighLowEnd(1);
                break;
            case R.id.dec_repeat:
                adjustRepeat(-1);
                break;
            case R.id.inc_repeat:
                adjustRepeat(1);
                break;
            case R.id.commit_btn:
                loadRange();
                //TODO
                //also load all the audio files in a row
                break;
            case R.id.play_verse:
                if(isPlaying)
                    stop();
                else
                    play();
                break;
            case R.id.download_audio_button:
                loadAudioFiles();
                break;
        }
    }

    private void loadAudioFiles() {
        playTheFileIfExists(fixZeroes(suraNumber)+fixZeroes(startAyahNumber));
    }

    private void loadRange() {
        suraNumber = String.valueOf(lastSurahIndex+1);//we adjust the value with only where it is necessary
        sharedPreferences.write(lastSurahIndex + "_start", startAyahNumber);
        sharedPreferences.write(lastSurahIndex + "_end", endAyahNumber);
        //TODO First load all the surah to check if it is fully available.

            ayahViewModel.getAyahRange(suraNumber, startAyahNumber, endAyahNumber).observe(this, new Observer<List<AyahRange>>() {
                @Override
                public void onChanged(List<AyahRange> ayahRanges) {
                    //TODO display the range
                    //send to the adapter
                    if (!RANGEISSHOWN) {
                        if (ayahRanges.size() == 0) {
                            //The list is empty. DOWNLOAD
                            Log.d(TAG, "surah not yet downloaded");
                            httpRequestSurah();
                        } else {
                            mAdapter.setText(ayahRanges);
                            PopulateTrackList();
                            Log.d(TAG, "ADAPTER " + ayahRanges.size());
                            Log.d(TAG, "surah exists in database");
                            RANGEISSHOWN = true;
                            commitBtn.setEnabled(false);

                        }
                    } else {
                        Log.d(TAG, "RANGEISSHOWN " + RANGEISSHOWN);
                    }
                }
            });
    }

    private void httpRequestSurah() {
        Log.i(TAG, "CLICK clicking");
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/ajax_quran.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        commitBtn.setEnabled(true);
                        // Convert String to json object
                        //httpresponse = true;
                        jsonArrayResponse = new ArrayList<JSONObject>();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }
                            populateAyahList(jsonArrayResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrashlytics.getInstance().recordException(e);
                            Log.i(TAG, "error json ttttttttttttttttt");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "ERROR RESPONSE enable reload button");
                progressBar.setVisibility(View.GONE);
                commitBtn.setEnabled(true);
                //tempbut.setVisibility(View.VISIBLE);
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("action", "izohsiz_text_obj"); //Add the data you'd like to send to the server.
                MyData.put("database_id", "1, 120, 59, 79");
                MyData.put("surah_id", suraNumber);
                //https://inventivesolutionste.ipage.com/ajax_quran.php
                //POST
                //action:names_as_objects
                //language_id:1
                return MyData;
            }
        };
        //Making request. Disable the commit button to avoid multiple requests.

        //tempbut.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        commitBtn.setEnabled(false);
        queue.add(stringRequest);
    }
    void populateAyahList(ArrayList<JSONObject> auclist){

        ChapterTextTable text;

        for (JSONObject i:auclist
        ) {

            try{
                //"ID":"31206","VerseID":"7","AyahText":"صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ","DatabaseID":"1","SuraID":"1","OrderNo":"5","SuraType":"Meccan","Note":null
                //Log.d("JSONOBJECT", i.toString());
                int verse_id = i.getInt("VerseID");
                int DatabaseID = i.getInt("DatabaseID");
                int chapter_id = i.getInt("SuraID");
                int OrderNo = i.getInt("OrderNo");
                String surah_type = i.getString("SuraType");
                String AyahText = i.getString("AyahText");
                //int sura_id, int verse_id, int favourite, int language_id, String ayah_text, String surah_type, int order_no, String comment, int read_count, int shared_count, int audio_position
                text = new ChapterTextTable(chapter_id, verse_id, 0, DatabaseID, OrderNo, AyahText, "", surah_type);
                ayahViewModel.insertText(text);
            }catch (Exception sx){
                Log.e(TAG, "EXCEPTION " + sx.getMessage());
            }
        }
    }
    private void adjustRepeat(int i) {
        int repeatCount = 0;
        try{
            repeatCount = Integer.parseInt(repeatValue.getText().toString());
        }catch (NumberFormatException nfx){
            //cant parse
            repeatCount = 1;
        }
        if (i > 0) {
            repeatCount += i;
        } else {
            if (repeatCount > 1) {
                repeatCount += i;
            }else{
                //stop playing the verses
                stopPlay();
            }
        }
        String repeatCount1 = String.format("%d", repeatCount);
        repeatValue.setText(repeatCount1);
        sharedPreferences.write(SharedPreferences.PREFERRED_REPEAT_COUNT, repeatCount1);
        repeatCountInteger = repeatCount;
        enableCommitButton();

    }

    private void enableCommitButton() {
        commitBtn.setEnabled(true);
        RANGEISSHOWN = false;
    }

    private void stopPlay() {
        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    void adjustHighLowStart(int i)
        {
        //todo don't allow end number to be higher than the start
            int sVal = 0;
            int eVal = 0;
            try {
                sVal = Integer.parseInt(startValue.getText().toString());
                eVal = Integer.parseInt(endValue.getText().toString());
            }catch (NumberFormatException nfx){
                //cant parse
            }
            if(i>0){
                if(sVal<eVal-1){
                    sVal+=i;
                    startValue.setText(""+sVal);
                }else if(sVal==0 && eVal==0){
                    sVal+=i;
                    startValue.setText(""+sVal);
                    eVal+=i+1;
                    endValue.setText(""+eVal);
                }
            }else{
                if(sVal>1){
                    sVal--;
                    startValue.setText(""+sVal);
                }
            }
            startAyahNumber = startValue.getText().toString();
            enableCommitButton();
        }
    void adjustHighLowEnd(int i)
    {
        int sVal = 0;
        int eVal = 0;
        try {
            sVal = Integer.parseInt(startValue.getText().toString());
            eVal = Integer.parseInt(endValue.getText().toString());
        }catch (NumberFormatException nfx){
            //cant parse
        }
        if(i>0){
            eVal+=i;
            endValue.setText(""+eVal);
        }else {
            if(sVal+1<eVal){
                eVal+=i;
                endValue.setText(""+eVal);
            }else if(sVal==0 && eVal==0){

            }
        }
        endAyahNumber = endValue.getText().toString();
        enableCommitButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();



    }

    @Override
    protected void onPause() {
        super.onPause();
        //todo save state on exit
        stopPlay();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        stop();
        super.onDestroy();

    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }
    private void playTheFileIfExists(String play_item_number) {

        if (play_item_number != null) {
            suraNumber2Play = play_item_number;
            play();
        }
    }
    void nextTrack(){
        PopulateTrackList();
        Log.d(TAG, "tracklist " + trackList.size());
            if(trackList.size() > 1){
                for(int i=0;i<trackList.size();i++){
                    Log.d(TAG, i + " index. " + trackList.get(i).getName() + " " + suraNumber2Play);
                    if (trackList.get(i).getName().equals(suraNumber2Play)) {
                        try{
                            suraNumber2Play =  trackList.get(i + 1).getName();
                            Log.e(TAG, suraNumber2Play + " - next suranumber. " + repeatCountInteger + " - repeatCountInteger");
                            break;
                        }catch (IndexOutOfBoundsException x){
                            if (repeatCountInteger>1) {
                                //go to the first file
                                repeatCountInteger--;//minus 1
                                suraNumber2Play = trackList.get(0).getName();
                            } else {
                                suraNumber2Play = null;
                            }

                        }
                    }
                }
            }
    }

    void play() {
        if (suraNumber2Play != null) {

                //suranomi = "(" + language + ") " + QuranMap.SURAHNAMES[Integer.parseInt(suraNumber2Play) - 1];
                String url;
                String filePath = "";

                //String path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
                //newpath = path + "/quran_audio/arabic/by_ayah/1/" + fixZeroes(suraNumber);//1 will change when there are more reciters
            newpath = getNewPath();
                File directory = new File(newpath);
                File[] files = directory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        String trackname = file.getName();
                        if (trackname.contains(".")) {
                            trackname = trackname.substring(0, trackname.lastIndexOf("."));
                            //suraNumber2Play =  fixZeroes(suraNumber)+fixZeroes(suraNumber2Play);
                            if (trackname.equals(suraNumber2Play)) {
                                //filePath = new StringBuilder().append(path).append("/quran_audio/"+language + "/by_surah/" + recitation_style + "/" + reciter+"/").append(prependZero(trackname)).append(".mp3").toString();
                                filePath = newpath + "/" + trackname + ".mp3";
                                Log.i(TAG, "Trackname " + trackname + " FP:" + filePath);
                            }
                        }
                    }
                }  //This surah is not available

                Log.i(TAG, "SURANAME 2 PLAY "+ suraNumber2Play);

                if (TrackDownloaded(suraNumber2Play)) {
                    url = filePath;
                } else {
                    url = newpath + "/" + fixAyahNameZeros(Integer.parseInt(suraNumber2Play)) + ".mp3";

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
                            resume();
                        }
                    });

                    try {
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
                    } catch (IOException x) {
                        Log.e(TAG, "ERROR " + x.getMessage());
                        //current_track_tv.setText("");
                        Toast.makeText(this, R.string.filenotfound, Toast.LENGTH_SHORT).show();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
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

    private boolean TrackDownloaded(String v) {

        boolean retval = false;
        if (trackList != null) {
            for (Track i : trackList
            ) {
                Log.i(TAG, "TRACK DOWNLOADED? " + v + " => " + i + " " + (i.getName().equals(v)));
                if (i.getName().equals(v)) {
                    //match found

                    retval = true;
                }
            }
        }else {
            Log.i(TAG, "TRACK DOWNLOADED CHECK FAIL " + v + " => " + trackList);
        }

        return retval;
    }
    public void resume() {

        if (mediaPlayer != null) {
            mediaPlayer.start();
            playVerse.setImageResource(R.drawable.ic_pause_circle_60dp);
            isPlaying = true;
            playCycle();
        }
    }

    public void playCycle() {
        if (mediaPlayer != null) {
            try {

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
                    Log.e(TAG, "PLAYING STOPPED");
                    isPlaying = false;
                    playVerse.setImageResource(R.drawable.ic_play_circle_48dp);
                    handler.removeCallbacks(runnable);
                    stop();
                    if (repeatCountInteger>0) {
                        nextTrack();
                        play();
                    }else {

                    }

                }
            } catch (IllegalStateException x) {
                isPlaying = false;
                mediaPlayer.release();
                handler.removeCallbacks(runnable);

            }

        }
    }
    public void stop() {
        Log.i("STOP", "stop");
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            playVerse.setImageResource(R.drawable.ic_play_circle_48dp);
        }
    }
    private void PopulateTrackList() {
        trackList = new ArrayList<>();

            //Log.d(TAG, "Files Path: " + path);
            //adding new folder structure
            newpath = getNewPath();
            Log.d(TAG, "Files Path: " + newpath);
            File directory = new File(newpath);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(".")) {
                        //String trackname = files[i].getName().substring(0, files[i].getName().lastIndexOf("."));
                        String trackname = file.getName();
                        trackname = trackname.substring(0, trackname.lastIndexOf("."));
                        Log.d(TAG, "track name: " + trackname);
                        try{
                            Integer.parseInt(trackname);
                        }catch (NumberFormatException nfx){
                            DeleteTheFile(file);
                        }

                        try {
                            //int tt = Integer.parseInt(trackname);
                            if (!TrackDownloaded(trackname)) {
                                String filePath = newpath + "/" + file.getName();
                                try {
                                    metadataRetriever.setDataSource(filePath);
                                    //Date date = new Date();
                                    Track track = new Track(AudioTimer.getTimeStringFromMs(Integer.parseInt(Objects.requireNonNull(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)))), trackname, filePath);
                                    trackList.add(track);
                                } catch (RuntimeException x) {
                                    Log.e(TAG, "METADATA ERROR " + trackname);
                                }
                            }
                        } catch (NumberFormatException nfx) {
                            Log.e(TAG, "TRACKNAME ERROR " + trackname);
                            DeleteTheFile(file);
                            //TODO delete the file with x-y.mp3 naming format (dual download)
                        }
                    }
                }
                if (trackList.size() > 1){
                    Collections.sort(trackList);
                    //current_track_tv.setText("");
                }else if(trackList.size()==1){
                    //current_track_tv.setText("");
                }
                else{
                    //current_track_tv.setText(R.string.tracklist_empty_warning);
                }

            } else {
                //current_track_tv.setText(R.string.tracklist_empty_warning);
                Log.d(TAG, "NULL ARRAY no files found");
            }
    }

    private String getNewPath() {


        String path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
        return path + "/quran_audio/arabic/by_ayah/1/"+fixZeroes(suraNumber);
    }

    private void DeleteTheFile(File file) {
        try {
            file.delete();
        } catch (SecurityException x) {
            Log.e(TAG, "FAILED to DELETE " + x.getMessage());
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        lastSurahIndex = position;
        //TODO HTTPrequest
        suraNumber = String.valueOf(position + 1);
        if (sharedPreferences != null) {
            sharedPreferences.write(SharedPreferences.SELECTED_MEMORIZING_SURAH, position);
        }
        setUIValues();
        mAdapter.setText(null);
        enableCommitButton();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void OnTrackPrevious() {

    }

    @Override
    public void OnTrackPlay() {

    }

    @Override
    public void OnTrackNext() {

    }

    @Override
    public void OnTrackPause() {

    }
    private boolean WritePermission() {
        Log.i("MY PERMISSION TO WRITE", this + " granted?");
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_WRITE_EXTERNAL_STORAGE);
            return false;
        } else {
            // Permission has already been granted
            Log.i("PERMISSION TO WRITE", MY_WRITE_EXTERNAL_STORAGE + " already granted");
            return true;
        }

    }
    private String fixZeroes(String s){
        String retVal = "";
        int tempVal;
        //try to parse the string into integer
        try{
            tempVal = Integer.parseInt(s);
            if(tempVal<10){
                retVal = "00"+tempVal;
            }else if(tempVal>9&&tempVal<100){
                retVal = "0"+tempVal;
            }else {
                //it is higher than 99
                retVal = s;
            }
        }catch (IllegalFormatException ignore){

        }

        return retVal;
    }
    @Override
    public void DownloadThis(String ayah2download) {

        if (WritePermission()) {
            String ayahReferenceNumber = fixZeroes(suraNumber) + fixZeroes(ayah2download);


                String url = mFirebaseRemoteConfig.getString("server_audio") + "/quran_audio/arabic/by_ayah/1/" + fixZeroes(suraNumber) + "/" + ayahReferenceNumber + ".mp3";

                newpath = getNewPath();
                //newpath = getExternalFilesDir(null) + "/quran_audio/arabic/by_ayah/1/" + fixZeroes(suraNumber);
                File file = new File(newpath, ayahReferenceNumber + ".mp3");
                DownloadManager.Request request;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    request = new DownloadManager.Request(Uri.parse(url))
                            .setTitle(ayahReferenceNumber)
                            .setDescription("Downloading " + ayahReferenceNumber)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))
                            .setRequiresCharging(false)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                } else {
                    request = new DownloadManager.Request(Uri.parse(url))
                            .setTitle(ayahReferenceNumber)
                            .setDescription("Downloading " + ayahReferenceNumber)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                }

                //if (sharedPreferences.read(SharedPreferences.SIGNATURE, "ERROR").equals("OK")) {
                if (isNetworkAvailable()) {


                    //query.setFilterById(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
                    Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING));
                    cursor.moveToFirst();
                    if (cursor != null && cursor.getCount() >= 1) {
                        Toast.makeText(getApplicationContext(), "Please, wait", Toast.LENGTH_SHORT).show();
                        mAdapter.notifyDataSetChanged();

                        if (!sharedPreferences.read(SharedPreferences.NOMOREADS, false)) {
                            //mInterstitialAd.show();
                        }
                    } else {
                        Log.i(TAG, cursor.getCount() + " downloads ");
                        //No downloads running. allow download
                        Log.i(TAG, "Download start " + ayahReferenceNumber);
                        downloadId = downloadManager.enqueue(request);
                        sharedPreferences.write("download_" + downloadId, ayahReferenceNumber); //storing the download id under the right sura reference. We can use the id later to check for download status
                        //sharedPreferences.write("downloading_surah_" + zznumber, (int) downloadId);
                        mAdapter.notifyDataSetChanged();
                        make(coordinatorLayout, "Downloading ayah: " + ayahReferenceNumber, LENGTH_SHORT).show();
                        //myTimer.schedule(new TimerTask() {
                        //Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING));

//                                @Override
//                                public void run() {
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            try{
//                                                mAdapter.notifyDataSetChanged();
//                                                cursor.moveToFirst();
//                                                if (cursor == null || cursor.getCount() == 0) {
//                                                    myTimer.cancel();
//                                                }
//                                            }catch (IllegalStateException x){
//                                                if (cursor == null || cursor.getCount() == 0 && myTimer!=null) {
//                                                    myTimer.cancel();
//                                                }
//                                            }
//                                        }
//                                    });
//                                }
//                            }, 500, 1000);
                    }
                } else {
                    //Log.i(TAG, "NO NETWORK");
                    make(coordinatorLayout, R.string.no_internet, LENGTH_SHORT).show();
                }
                //} else {
                //Log.i(TAG, "NO SIGNATURE");
                //must start the app normal
                //}

            } else {
                Log.i("PERMISSION NG", "Download fail");
                make(coordinatorLayout,
                        R.string.write_permission_denied,
                        LENGTH_LONG).show();
            }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void LoadTitlesFromServer() {

    }

    @Override
    public void insertTitle(ChapterTitleTable title) {

    }

    @Override
    public void MarkAsAwarded(int surah_id) {

    }

    String fixAyahNameZeros(int ayah_id){
        String retVal = "";
        int tempVal;
        //try to parse the string into integer
        try{
            tempVal = ayah_id;
            if(tempVal<9999){
                retVal = "00"+tempVal;
            }else if(tempVal>9999&&tempVal<99999){
                retVal = "0"+tempVal;
            }else {
                //it is higher than 99
                retVal = String.valueOf(ayah_id);
            }
        }catch (IllegalFormatException ignore){

        }
        return retVal;
    }


    String makeAyahRefName(int verse_id){
        String rv = "";
        rv = fixZeroes(suraNumber).concat(fixZeroes(String.valueOf(verse_id)));
        return rv;
    }

    @Override
    public void MarkAsDownloaded(int ayah_id) {
        //TODO set the downloaded ayah state

    }

    void MarkAyahAsDownloaded(String downloadedAyahId){
        if (mAdapter != null) {
            int actual_position = 0;
            if (mAdapter.getItemCount() > 0) {
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    String ayah_ref_name =  makeAyahRefName(mAdapter.getTitleAt(i).verse_id);
                    try {
                        if (mAdapter.getTitleAt(i) != null && ayah_ref_name.equals(downloadedAyahId)) {
                            Log.d(TAG, ayah_ref_name + " matches downloadedAyahId " + downloadedAyahId);
                            actual_position = i;
                            mAdapter.notifyDataSetChanged();
                        }
                    } catch (IndexOutOfBoundsException x) {
                        //Crashlytics.log(x.getMessage() + " - " + x.getStackTrace());
                    }
                }
                AyahRange ctitle = mAdapter.getTitleAt(actual_position);
                Log.d(TAG,  "CTITLE " + " index:" + actual_position + ". " + ctitle.verse_id + " - verse id, audio progress: " + ctitle.audio_progress);
                if (ctitle != null && ctitle.audio_progress < 100) {
                    ctitle.audio_progress = 100;
                    ayahViewModel.update(ctitle);
                }
            }
        }
    }

    @Override
    public void MarkAsDownloading(int ayah_id) {
        if (mAdapter != null) {
            int actual_position = 0;
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                try {
                    if (mAdapter.getTitleAt(i) != null && mAdapter.getTitleAt(i).verse_id == ayah_id) {
                        actual_position = i;
                    }
                } catch (IndexOutOfBoundsException iobx) {
                    Log.e("CANNOT GET POSITION", iobx.getMessage());
                }
            }
            AyahRange ctitle = mAdapter.getTitleAt(actual_position);
            if (ctitle != null) {
                ctitle.audio_progress = 50;
                ayahViewModel.update(ctitle);
            }
        }
    }
}
