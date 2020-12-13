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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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

public class MemorizeActivity extends AppCompatActivity implements View.OnClickListener, MyListener, AdapterView.OnItemSelectedListener, Playable {
    private static final int MY_WRITE_EXTERNAL_STORAGE = 101;
    public static final String TAG = MemorizeActivity.class.getSimpleName();

    //DEFINE UI ELEMENTS
    private Spinner suranames_spinner;
    private ImageButton playVerse;
    private ImageButton decRepeat;
    private ImageButton incRepeat;
    private ImageButton decStart;
    private ImageButton incStart;
    private ImageButton decEnd;
    private ImageButton incEnd;
    private Button commitBtn;
    private Button dl_audio;
    private TextView startValue;
    private TextView endValue;
    private TextView repeatValue;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    //DATA
    private TitleViewModel ayahViewModel;
    private ArrayList<Track> trackList;
    private ArrayAdapter<CharSequence> language_adapter;
    private MediaPlayer mediaPlayer;
    private ArrayList<JSONObject> jsonArrayResponse;

    private MemorizeActivityAdapter adapter;
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
    private MediaMetadataRetriever metadataRetriever;
    private DownloadManager downloadManager;
    DownloadManager.Query query;
    private boolean download_attempted;
    Timer myTimer = new Timer();
    long downloadId;

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
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
        ayahViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        //INITIALIZE UI ELEMENTS
        suranames_spinner = findViewById(R.id.surah_spinner);
        dl_audio = findViewById(R.id.download_audio_button);
        playVerse = findViewById(R.id.play_verse);
        decRepeat = findViewById(R.id.dec_repeat);
        incRepeat = findViewById(R.id.inc_repeat);

        incStart = findViewById(R.id.inc_start);
        decStart = findViewById(R.id.dec_start);
        incEnd = findViewById(R.id.inc_end);
        decEnd = findViewById(R.id.dec_end);

        commitBtn = findViewById(R.id.commit_btn);

        startValue = findViewById(R.id.start_tv);
        endValue = findViewById(R.id.end_tv);
        repeatValue = findViewById(R.id.repeat_count_tv);


        recyclerView = findViewById(R.id.memorize_range_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MemorizeActivityAdapter(this);
        recyclerView.setAdapter(adapter);

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

        /*DONE end number never lower than the start
           if start number entered and it is higher than the end number, set the end number
            equal to the start number. But if the start number is changed to a lower value,
            reset the end number back to what it was before
         */
        //DONE
        populateSpinner();
    }

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

        Log.d(TAG, lastSurahIndex + " last surah index, start:" + startAyahNumber + " - end:" + endAyahNumber);

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
            case R.id.download_audio_button:
                loadAudioFiles();
                break;
        }
    }

    private void loadAudioFiles() {
        //Open download page
        Intent intent = new Intent(this, MemorizeDownloadActivity.class);
        startActivity(intent);
    }

    private void loadRange() {
        suraNumber = Integer.toString(lastSurahIndex + 1);//we adjust the value with only where it is necessary
        sharedPreferences.write(lastSurahIndex + "_start", startAyahNumber);
        sharedPreferences.write(lastSurahIndex + "_end", endAyahNumber);
        ayahViewModel.getAyahRange(suraNumber, startAyahNumber, endAyahNumber).observe(this, new Observer<List<AyahRange>>() {
            @Override
            public void onChanged(List<AyahRange> ayahRanges) {
                //TODO display the range
                //send to the adapter
                adapter.setText(ayahRanges);
                Log.d(TAG, "ADAPTER " + ayahRanges.size());

                if (ayahRanges.size() == 0) {
                    //The list is empty. DOWNLOAD
                    httpRequestSurah();
                } else {
                    Log.d(TAG, "surah exists in database");
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

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(, android.R.layout.simple_spinner_item, auclist);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
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
        sharedPreferences.write(SharedPreferences.PREFERRED_REPEAT_COUNT, "10");

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
        }
    void adjustHighLowEnd(int i)
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
        if (suraNumber2Play != null && trackList.size() > 1) {
            for(int i=0;i<trackList.size();i++){

                if (trackList.get(i).getName().equals(suraNumber2Play)) {
                    try{
                        suraNumber2Play = String.valueOf(Integer.parseInt(trackList.get(i + 1).getName()));
                        Log.e(TAG, suraNumber2Play + " - next suranumber");
                        break;
                    }catch (IndexOutOfBoundsException x){
                        if (repeatCountInteger>1) {
                            //go to the first file
                            repeatCountInteger--;//minus 1
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

                //suranomi = "(" + language + ") " + QuranMap.SURAHNAMES[Integer.parseInt(suraNumber2Play) - 1];
                String url;
                String filePath = "";

                String path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
                newpath = path + "/quran_audio/arabic/by_ayah/1";//1 will change when there are more reciters
                File directory = new File(newpath);
                File[] files = directory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        String trackname = file.getName();
                        if (trackname.contains(".")) {
                            trackname = trackname.substring(0, trackname.lastIndexOf("."));
                            suraNumber2Play = fixZeroes(suraNumber2Play);
                            if (trackname.equals(suraNumber2Play)) {
                                //filePath = new StringBuilder().append(path).append("/quran_audio/"+language + "/by_surah/" + recitation_style + "/" + reciter+"/").append(prependZero(trackname)).append(".mp3").toString();
                                filePath = newpath + "/" + trackname + ".mp3";
                                Log.i(TAG, "Trackname " + trackname + " FP:" + filePath);
                            }
                        }
                    }
                }  //This surah is not available

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
                            //mp_seekBar.setMax(mediaPlayer.getDuration());
                            //progressBar.setVisibility(View.INVISIBLE);
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

    private boolean TrackDownloaded(String v) {

        boolean retval = false;
        if (trackList != null) {
            for (Track i : trackList
            ) {
                if (i.getName().equals(v)) {
                    //match found
                    Log.i("TRACK DOWNLOADED?", v + " " + i + " " + (i.getName().equals(v)));
                    retval = true;
                }
                //Log.i("TRACK DOWNLOADED????", String.valueOf(v) + " " + i.getName());
            }
        }

        return retval;
    }
    public void resume() {

        if (mediaPlayer != null) {

            int audio_pos;
//            if(!language.equals("arabic"))
            //audio_pos = SharedPreferences.getInstance().read(suranomi, 0);
            /*if (audio_pos > 0 && audio_pos != mediaPlayer.getDuration()) {
                mediaPlayer.seekTo(audio_pos);
            } else {
                SharedPreferences.getInstance().write(suranomi, 1);
                mediaPlayer.seekTo(1);
                Toast.makeText(getBaseContext(), audiorestore, Toast.LENGTH_SHORT).show();
                //Snackbar.make(coordinatorLayout, audiorestore, Snackbar.LENGTH_SHORT).show();
            }*/
            mediaPlayer.start();
            playVerse.setImageResource(R.drawable.ic_pause_circle_60dp);
            isPlaying = true;
            playCycle();
        }
    }

    public void playCycle() {
        if (mediaPlayer != null) {
            try {
                //mp_seekBar.setProgress(mediaPlayer.getCurrentPosition());
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
                    //current_track_tv.setText("");
                    isPlaying = false;
                    //mp_seekBar.setProgress(0);
                    handler.removeCallbacks(runnable);
                    //SharedPreferences.getInstance().write(suranomi, 0);
                    stop();
                    if (repeatCountInteger>1) {
                        nextTrack();
                        play();
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
            //mediaPlayer.reset();
            //progressBar.setVisibility(View.INVISIBLE);
            playVerse.setImageResource(R.drawable.ic_play_circle_48dp);
            //current_track_tv.setText("");
        }
    }
    private void PopulateTrackList() {
        trackList = new ArrayList<>();
        String path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
            //Log.d(TAG, "Files Path: " + path);
            //TODO adding new folder structure
            newpath = path + "/quran_audio/arabic/by_ayah/1/";
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
                                } catch (RuntimeException x) {

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

                //adapter.setText(trackList);
                //recyclerView.setAdapter(mAdapter);
            } else {
                //current_track_tv.setText(R.string.tracklist_empty_warning);
                Log.d(TAG, "NULL ARRAY no files found");
                //adapter.setTitles(trackList);
                //recyclerView.setAdapter(mAdapter);
            }
            //mAdapter.notifyDataSetChanged();
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
        //Log.d(TAG, "p " + position);
        //DONE save the selected item for the resume
        lastSurahIndex = position;
        //TODO HTTPrequest
        suraNumber = String.valueOf(position + 1);

        if (sharedPreferences != null) {
            sharedPreferences.write(SharedPreferences.SELECTED_MEMORIZING_SURAH, position);
        }
        setUIValues();
        adapter.setText(null);
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
            Log.i("MY PERMISSION TO WRITE", MY_WRITE_EXTERNAL_STORAGE + " already granted");
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
    public void DownloadThis(String suraNumber) {
        if (WritePermission()) {

            
                String middle_path = "arabic/by_ayah/1";
                //TODO new path:
            String zznumber = fixZeroes(suraNumber);
                String url = mFirebaseRemoteConfig.getString("server_audio") + "/quran_audio/" + middle_path + "/" + zznumber + ".mp3";
                
                Log.e(TAG, " DOWNLOAD url " + url);
                //String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suraNumber + ".mp3"; // your URL here
            
                newpath = getExternalFilesDir(null) + "/quran_audio/" + middle_path;
                File file = new File(newpath, zznumber + ".mp3");
                DownloadManager.Request request;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    request = new DownloadManager.Request(Uri.parse(url))
                            .setTitle(zznumber)
                            .setDescription("Downloading " + zznumber)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))
                            .setRequiresCharging(false)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                } else {
                    request = new DownloadManager.Request(Uri.parse(url))
                            .setTitle(zznumber)
                            .setDescription("Downloading " + zznumber)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setDestinationUri(Uri.fromFile(file))
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                }

                if (sharedPreferences.read(SharedPreferences.SIGNATURE, "ERROR").equals("OK")) {
                    if (isNetworkAvailable()) {


                        //query.setFilterById(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
                        Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING));
                        cursor.moveToFirst();
                        if (cursor != null && cursor.getCount() >= 1) {
//                            for (int i = 0; i < cursor.getCount(); i++) {
//                                Log.i(TAG, cursor.getInt(i) + " download ");
//                                cursor.moveToNext();
//                            }
                            Toast.makeText(getApplicationContext(), "Please, wait", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();

                            if(!sharedPreferences.read(SharedPreferences.NOMOREADS, false))
                            {
                                //mInterstitialAd.show();
                            }
                        } else {
                            Log.i(TAG, cursor.getCount() + " downloads ");
                            //No downloads running. allow download
                            Log.i(TAG, "Download start " + zznumber);
                            downloadId = downloadManager.enqueue(request);
                            sharedPreferences.write("download_" + downloadId, zznumber); //storing the download id under the right sura reference. We can use the id later to check for download status
                            sharedPreferences.write("downloading_surah_" + zznumber, (int) downloadId);
                            adapter.notifyDataSetChanged();

                            myTimer.schedule(new TimerTask() {
                                Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING));

                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                adapter.notifyDataSetChanged();
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
                        Log.i(TAG, "NO NETWORK");
                    }
                } else {
                    Log.i(TAG, "NO SIGNATURE");
                }

        } else {
            Log.i("PERMISSION NG", "Download fail");
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

    @Override
    public void MarkAsDownloaded(int surah_id) {

    }

    @Override
    public void MarkAsDownloading(int surah_id) {

    }
}
