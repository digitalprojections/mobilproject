package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;

import furqon.io.github.mobilproject.Services.OnClearFromService;

import static com.google.android.material.snackbar.Snackbar.*;

public class MemorizeActivity extends AppCompatActivity implements View.OnClickListener, MyListener, AdapterView.OnItemSelectedListener, SetSuraNumber, Playable {
    private static final int MY_WRITE_EXTERNAL_STORAGE = 101;
    public static final String TAG = MemorizeActivity.class.getSimpleName();

    //DEFINE UI ELEMENTS
    private Spinner suranames_spinner;
    private ImageButton playVerse;
    private ImageButton playMode;
    ImageButton decRepeat;
    ImageButton incRepeat;
    ImageButton incStart;
    ImageButton decStart;
    ImageButton incEnd;
    ImageButton decEnd;
    private TextView startValue;
    private TextView endValue;
    private TextView repeatValue;
    private ProgressBar progressBar;
    TextView startendtext;
    Animation scaler;

    //DATA
    private TitleViewModel ayahViewModel;
    private ArrayList<Track> trackList;
    private ArrayAdapter<CharSequence> language_adapter;
    private MediaPlayer mediaPlayer;
    private ArrayList<JSONObject> jsonArrayResponse;

    private MemorizeActivityAdapter mAdapter;
    private LinearLayout coordinatorLayout;
    private Integer lastSurahIndex = 0;
    RecyclerView recyclerView;

    SharedPreferences sharedPreferences;
    private String startAyahNumber = "1";
    private String endAyahNumber = "2";
    private String preferredRepeatCount = "10";
    private int repeatCountInteger = 10;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Context context;
    //private String suraNumber;
    private String suraNumber2Play;
    private String newpath;
    private boolean isPlaying;
    private boolean repeatOne;
    private Runnable runnable;
    private Handler handler;
    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
    private DownloadManager downloadManager;
    DownloadManager.Query query;
    private boolean download_attempted;
    Timer myTimer = new Timer();
    long downloadId;
    private boolean RANGEISSHOWN;
    private FirebaseAnalytics mFirebaseAnalytics;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(false);
        menu.getItem(2).setVisible(false);
        menu.getItem(3).setVisible(false);
        menu.getItem(4).setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_i:
                open_settings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void open_settings() {
        Intent intent;
        intent = new Intent(this, furqon.io.github.mobilproject.Settings.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize);
        sharedPreferences = SharedPreferences.getInstance();
        sharedPreferences.init(getApplicationContext());
        //DONE restore the last state
        //There was a surah selected
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        scaler = AnimationUtils.loadAnimation(this, R.anim.bounce);

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
        suranames_spinner.setOnItemSelectedListener(this);
        playVerse = findViewById(R.id.play_verse);
        playMode = findViewById(R.id.play_mode);
        ImageButton refreshBtn = findViewById(R.id.refresh_btn);
        decRepeat = findViewById(R.id.dec_repeat);
        incRepeat = findViewById(R.id.inc_repeat);
        incStart = findViewById(R.id.inc_start);
        decStart = findViewById(R.id.dec_start);
        incEnd = findViewById(R.id.inc_end);
        decEnd = findViewById(R.id.dec_end);

        coordinatorLayout = findViewById(R.id.mem_main_lin_layout);

        startendtext = findViewById(R.id.starting_and_ending_ayats);

        startValue = findViewById(R.id.start_tv);
        endValue = findViewById(R.id.end_tv);
        repeatValue = findViewById(R.id.repeat_count_tv);



        recyclerView = findViewById(R.id.memorize_range_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MemorizeActivityAdapter(this, ARG.suraNumber);
        recyclerView.setAdapter(mAdapter);
        handler = new Handler();

        progressBar = findViewById(R.id.progressBarMemorize);
        progressBar.setVisibility(View.GONE);

        //UI ACTION
        playVerse.setOnClickListener(this);
        playMode.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
        decRepeat.setOnClickListener(this);
        incRepeat.setOnClickListener(this);
        decStart.setOnClickListener(this);
        incStart.setOnClickListener(this);
        decEnd.setOnClickListener(this);
        incEnd.setOnClickListener(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiverDownload, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            try {
                startService(new Intent(getBaseContext(), OnClearFromService.class));
            } catch (IllegalStateException | SecurityException x) {
                Log.e(TAG, Objects.requireNonNull(x.getMessage()));
            }
        }
        /*DONE end number never lower than the start
           if start number entered and it is higher than the end number, set the end number
            equal to the start number. But if the start number is changed to a lower value,
            reset the end number back to what it was before
         */
        //DONE
        populateSpinner();
        setSwipeControls();
    }

    //DOWNLOAD COMPLETE OR FAILED
    private final BroadcastReceiver broadcastReceiverDownload = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            query.setFilterById(id);
            String ayahNumber2Download = sharedPreferences.read("download_" + id, "0");
            int ayahInt;
            ayahInt = Integer.parseInt(ayahNumber2Download);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(columnReason);

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    if (ayahInt > 0) {
                        sharedPreferences.write("download_" + id, "0");
                        PopulateTrackList();
                    }
                    Log.i(TAG, "DOWNLOAD COMPLETE, Download id " + id + " ayah number: " + ayahNumber2Download);
                        //PopulateTrackList();
                    if(!ayahNumber2Download.equals("0"))
                        MarkAyahAsDownloaded(ayahNumber2Download);
                } else if (status == DownloadManager.STATUS_FAILED) {
                    make(coordinatorLayout,
                            "error " + reason,
                            LENGTH_LONG).show();
                    //Crashlytics.log("download error - " + reason + "->" + language + "/" + recitation_style + "/" + reciter + "/" + suraNumber2Download);
                    MarkAsDownloadFailed(ayahInt);
                    mAdapter.notifyDataSetChanged();
                } else if (status == DownloadManager.STATUS_PAUSED) {
                    make(coordinatorLayout,
                            "PAUSED!\n" + "reason of " +  reason,
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

    private void MarkAsDownloadFailed(int ayahInt) {
        String failedAudio = String.valueOf(ayahInt);
        if (mAdapter != null) {
            int actual_position = 0;
            if (mAdapter.getItemCount() > 0) {
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    String ayah_ref_name = ARG.makeAyahRefName(mAdapter.getTitleAt(i).verse_id);
                    //Log.d(TAG, ayah_ref_name + " FAILED AUDIO downloadedAyahId " + failedAudio);
                    try {
                        if (mAdapter.getTitleAt(i) != null && ARG.makeAyahRefName(failedAudio).equals(ayah_ref_name)) {
                            //Log.d(TAG, ayah_ref_name + " matches downloadedAyahId " + downloadedAyahId);
                            actual_position = i;
                            AyahRange ctitle = mAdapter.getTitleAt(actual_position);
                            //Log.d(TAG,  "CTITLE " + " index:" + actual_position + ". " + ctitle.verse_id + " - verse id, audio progress: " + ctitle.audio_progress);
                            ctitle.audio_progress = 0;
                            ayahViewModel.update(ctitle);
                            mAdapter.notifyDataSetChanged();
                        }
                    } catch (IndexOutOfBoundsException x) {
                        //Crashlytics.log(x.getMessage() + " - " + x.getStackTrace());
                    }
                }
            }
        }
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
    }

    private void setUIValues() {
        RANGEISSHOWN = false;
        startAyahNumber = sharedPreferences.read(lastSurahIndex + "_start", "1");
        endAyahNumber = sharedPreferences.read(lastSurahIndex + "_end", "2");
        repeatOne = sharedPreferences.read("AYAH_PLAYMODE", false);
        Log.d(TAG, lastSurahIndex + " last surah index, start:" + startAyahNumber + "-end:" + endAyahNumber);
        startValue.setText(startAyahNumber);
        endValue.setText(endAyahNumber);
        if(sharedPreferences.contains(SharedPreferences.PREFERRED_REPEAT_COUNT)){
            preferredRepeatCount = sharedPreferences.read(SharedPreferences.PREFERRED_REPEAT_COUNT, "10");
            startendtext.setVisibility(View.GONE);
        }else{
            startendtext.setVisibility(View.VISIBLE);
            startendtext.startAnimation(scaler);
            decStart.startAnimation(scaler);
            incStart.startAnimation(scaler);
            decEnd.startAnimation(scaler);
            incEnd.startAnimation(scaler);
        }

        repeatCountInteger = Integer.parseInt(preferredRepeatCount);
        repeatValue.setText(preferredRepeatCount);
        setPlayMode();
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
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
            case R.id.play_verse:

                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "play_verse");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "play");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "audio");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if(isPlaying){
                    stop();
                }
                else {
                    loadRange();
                    loadAudioFiles();
                }
                break;
            case R.id.play_mode:
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "play_mode");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "play mode, repeat one " + repeatOne);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "audio");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                repeatOne=!repeatOne;
                setPlayMode();
                break;
                case R.id.refresh_btn:
                reloadLists();
                    setUIValues();
                break;
        }
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


                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, R.string.want_to_delete, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(trackList.size()>0) {
                                DeleteTheTrack(trackList.get(position).getName());
                                AyahRange ctitle = mAdapter.getTitleAt(position);
                                if (ctitle != null) {
                                    ctitle.audio_progress = 0;
                                    ayahViewModel.update(ctitle);
                                }
                                PopulateTrackList();
                                mAdapter.notifyDataSetChanged();
                                //recyclerView.scrollToPosition(position);
                            }
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
                if (mediaPlayer!=null && mediaPlayer.isPlaying()) {

                } else {
                    snackbar.show();
                }
                }

        });
            itemTouchHelper.attachToRecyclerView(recyclerView);

    }
    private void DeleteTheTrack(String tracktodelete) {

            String path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
            newpath = getNewPath();
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

                mAdapter.setTrackList(trackList);
                recyclerView.setAdapter(mAdapter);
            } else {
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.d(TAG, "NULL ARRAY no files found");
                mAdapter.setTrackList(trackList);
                recyclerView.setAdapter(mAdapter);
            }
    }
    private void setPlayMode() {
        if(!repeatOne){
            playMode.setImageResource(R.drawable.ic_repeat_black_24dp);
        }
        else{
            playMode.setImageResource(R.drawable.ic_repeat_one_black_24dp);
        }
        sharedPreferences.write("AYAH_PLAYMODE", repeatOne);
    }

    private void loadAudioFiles() {
        PopulateTrackList();
        ARG.makeAyahRefName(startAyahNumber);
        suraNumber2Play = ARG.makeAyahRefName(startAyahNumber);
        play();

    }

    private void loadRange() {

        ARG.setSuraName(String.valueOf(lastSurahIndex+1));
        sharedPreferences.write(lastSurahIndex + "_start", startAyahNumber);
        sharedPreferences.write(lastSurahIndex + "_end", endAyahNumber);
        //TODO First load all the surah to check if it is fully available.

            ayahViewModel.getAyahRange(ARG.suraNumber, startAyahNumber, endAyahNumber).observe(this, new Observer<List<AyahRange>>() {
                @Override
                public void onChanged(List<AyahRange> ayahRanges) {
                    Log.d(TAG, ARG.suraNumber + " - " + startAyahNumber + " - " + endAyahNumber + " surah being called from DB " + ARG.suraNumber + " of total " + QuranMap.GetSurahLength(Integer.parseInt(ARG.suraNumber)-1));
                    //TODO display the range
                    //send to the adapter
                    if (!RANGEISSHOWN) {
                        if (ayahRanges.size() == 0) {
                            //The list is empty. DOWNLOAD
                            Log.d(TAG, "surah not yet downloaded");
                            mAdapter.setText(null);
                            httpRequestSurah();

                        } else if(ayahRanges.size()==(Integer.parseInt(endAyahNumber)-Integer.parseInt(startAyahNumber))+1) {
                            mAdapter.setText(ayahRanges);
                            PopulateTrackList();
                            Log.d(TAG, "surah exists in database ");
                            Log.d(TAG, "RESULTS SHOWN");

                            RANGEISSHOWN = true;

                        }
                    } else {
                        Log.d(TAG, "RANGEISSHOWN " + RANGEISSHOWN);
                    }
                }
            });
    }

    private void httpRequestSurah() {
        Log.i(TAG, "HTTPREQUEST sura text");
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/ajax_quran.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        // Convert String to json object
                        //httpresponse = true;
                        jsonArrayResponse = new ArrayList<JSONObject>();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }
                            Log.d(TAG, "HTTP RESPONSE ARRIVED");
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
                //tempbut.setVisibility(View.VISIBLE);

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("action", "izohsiz_text_obj"); //Add the data you'd like to send to the server.
                MyData.put("database_id", "1, 120, 59, 79");
                MyData.put("surah_id", ARG.suraNumber);
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
        queue.add(stringRequest);
    }
    void populateAyahList(ArrayList<JSONObject> ayah_list){

        ChapterTextTable text;

        for (JSONObject i:ayah_list
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
        setUIValues();
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
        @SuppressLint("DefaultLocale") String repeatCount1 = String.format("%d", repeatCount);
        repeatValue.setText(repeatCount1);
        sharedPreferences.write(SharedPreferences.PREFERRED_REPEAT_COUNT, repeatCount1);
        repeatCountInteger = repeatCount;
        reloadLists();

    }

    private void reloadLists() {
        loadRange();
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
                if(sVal<eVal){
                    sVal+=i;
                    startValue.setText(String.valueOf(sVal));
                }else if(sVal==0 && eVal==0){
                    sVal+=i;
                    startValue.setText(String.valueOf(sVal));
                    eVal+=i;
                    endValue.setText(String.valueOf(sVal));
                }
            }else{
                if(sVal>1){
                    sVal--;
                    startValue.setText(String.valueOf(sVal));
                }else{
                    //disable minus button

                }
            }
            startAyahNumber = startValue.getText().toString();
            reloadLists();
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
        if(i>0 && eVal<QuranMap.GetSurahLength(lastSurahIndex)){
            eVal+=i;
            endValue.setText(String.valueOf(eVal));
        }else {
            //decrease the value
            if(sVal<eVal && i<0){
                eVal+=i;
                endValue.setText(String.valueOf(eVal));
            }
        }
        endAyahNumber = endValue.getText().toString();
        reloadLists();
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
    void nextTrack(){
        Log.d(TAG, "tracklist " + trackList.size());
        if (TrackDownloaded(suraNumber2Play) && repeatCountInteger>0) {
            suraNumber2Play =  getTheNextVerseNumber(suraNumber2Play);
            Log.e(TAG, suraNumber2Play + " - next suranumber. " + repeatCountInteger + " - repeatCountInteger");
        }else
            {
                suraNumber2Play = null;
            //STOP PLAYING?
            // set values null?
            }
        play();
    }

    private String getTheNextVerseNumber(String suraNumber2Play) {
        int sn2p = 0;
        int vn=0;
        String rv=null;
        try{
            sn2p=Integer.parseInt(suraNumber2Play);
            vn=sn2p%1000;
            if(vn<Integer.parseInt(endAyahNumber)){
                if(repeatOne){
                    repeatCountInteger--;//minus 1
                    repeatValue.setText(String.valueOf(repeatCountInteger));
                    repeatValue.startAnimation(scaler);
                    if(repeatCountInteger>0){
                        rv=ARG.makeAyahRefName(startAyahNumber);
                    }
                }else{
                    vn=vn+1;
                    rv=String.valueOf(vn);
                    rv=ARG.makeAyahRefName(rv);
                }
            }else{
                repeatCountInteger--;//minus 1
                repeatValue.setText(String.valueOf(repeatCountInteger));
                repeatValue.startAnimation(scaler);

                if(repeatOne){
                    if(repeatCountInteger>0){
                        rv=ARG.makeAyahRefName(vn);
                    }
                }else{
                    if(repeatCountInteger>0){
                        rv=ARG.makeAyahRefName(startAyahNumber);
                    }
                }
            }

        }
        catch (IllegalFormatException ignored){

        }
        return rv;
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

                Log.i(TAG, "SURANAME to PLAY "+ suraNumber2Play);

                if (TrackDownloaded(suraNumber2Play)) {
                    url = filePath;
                } else {
                    url = newpath + "/" + ARG.makeAyahRefName(suraNumber2Play) + ".mp3";
                }

                if (!url.isEmpty()) {
                    Log.i(TAG, "PLAY " + url);
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
                        MarkAsPlaying(suraNumber2Play);
                    } catch (IOException x) {
                        Log.e(TAG, "ERROR " + x.getMessage());
                        //current_track_tv.setText("");
                        make(coordinatorLayout, R.string.filenotfound, LENGTH_SHORT).show();
                        MarkAsPlaying(null);
                        mediaPlayer.release();
                        mediaPlayer = null;
                        int actual_position = ARG.getAyahNameFromReferenceName(suraNumber2Play);
                        AyahRange ctitle = mAdapter.getTitleAt(actual_position);
                        if(ctitle!=null){
                            ctitle.audio_progress = 0;
                            ayahViewModel.update(ctitle);
                            mAdapter.notifyDataSetChanged();
                        }else{
                            loadRange();
                        }

                    }
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
        else {
            setUIValues();
            stop();
        }
    }

    private void MarkAsPlaying(String suraNumber2Play) {
        mAdapter.markAsPlaying(suraNumber2Play);
        //mAdapter.notifyDataSetChanged();
    }

    private boolean TrackDownloaded(String v) {

        boolean retval = false;
        if (trackList != null) {
            for (Track i : trackList
            ) {
                //Log.i(TAG, "TRACK DOWNLOADED? " + v + " => " + i + " " + (i.getName().equals(v)));
                if (i.getName().equals(v)) {
                    //match found
                    retval = true;
                    break;
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
            isPlaying=false;
            mediaPlayer = null;
            playVerse.setImageResource(R.drawable.ic_play_circle_48dp);
            MarkAsPlaying(null);
        }
    }
    private void PopulateTrackList() {

        trackList = new ArrayList<>();
        //Log.d(TAG, "Files Path: " + path);
        // adding new folder structure
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
                                String filePath = newpath + "/" + file.getName();
                                Log.d(TAG, "COMPARE AYAHS " + trackname + " vs " + ARG.makeAyahRefName(startAyahNumber));
                                if(Integer.parseInt(trackname)>=Integer.parseInt(ARG.makeAyahRefName(startAyahNumber)) && Integer.parseInt(trackname)<=Integer.parseInt(ARG.makeAyahRefName(endAyahNumber))){
                                    try{
                                    metadataRetriever.setDataSource(filePath);
                                    //Date date = new Date();
                                    Track track = new Track(AudioTimer.getTimeStringFromMs(Integer.parseInt(Objects.requireNonNull(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)))), trackname, filePath);
                                    trackList.add(track);

                                    }catch(RuntimeException rtx){
                                        
                                    }
                                }
                                else
                                    {
                                    Log.d(TAG, "filename outside the range");
                                }

                        } catch (NumberFormatException nfx) {
                            Log.e(TAG, "TRACKNAME ERROR " + trackname);
                            DeleteTheFile(file);
                            //TODO delete the file with x-y.mp3 naming format (dual download)
                        }
                    }
                }
                if(trackList.size()>=1) {
                    //current_track_tv.setText("");
                    mAdapter.setTrackList(trackList);
                }
            } else {
                Log.d(TAG, "EMPTY ARRAY no files found");
                trackList.clear();
                make(coordinatorLayout, R.string.click_ayah, LENGTH_SHORT).show();
            }
    }

    private String getNewPath() {
        String path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath();
        return path + "/quran_audio/arabic/by_ayah/1/"+ARG.getSurahNameOnly();
    }

    private void DeleteTheFile(File file) {
        try {
            if(file.delete())
                PopulateTrackList();
        } catch (SecurityException x) {
            Log.e(TAG, "FAILED to DELETE " + x.getMessage());
        }
        loadRange();
    }


    private boolean WritePermission() {
        Log.i(TAG, "MY PERMISSION TO WRITE granted?");
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_WRITE_EXTERNAL_STORAGE);
            return false;
        } else {
            // Permission has already been granted
            Log.i(TAG, "PERMISSION TO WRITE " + MY_WRITE_EXTERNAL_STORAGE + " already granted");
            return true;
        }

    }

    @Override
    public void DownloadThis(String ayah2download) {

        if (WritePermission())
        {
            String ayahReferenceNumber = ARG.makeAyahRefName(ayah2download);
                String url = mFirebaseRemoteConfig.getString("server_audio") + "/quran_audio/arabic/by_ayah/1/" + ARG.getSurahNameOnly() + "/" + ayahReferenceNumber + ".mp3";
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
                    if (cursor.getCount() >= 3) {
                        make(coordinatorLayout, R.string.dl_queue_full, LENGTH_SHORT).show();
                        //mAdapter.notifyDataSetChanged();

                        if (!sharedPreferences.read(SharedPreferences.NOMOREADS, false)) {
                            //mInterstitialAd.show();
                        }
                        Log.i(TAG, cursor.getCount() + " downloads ");
                    } else {
                        //No downloads running. allow download
                        Log.i(TAG, "Download start " + url);
                        downloadId = downloadManager.enqueue(request);
                        sharedPreferences.write("download_" + downloadId, ayahReferenceNumber); //storing the download id under the right sura reference. We can use the id later to check for download status
                        //sharedPreferences.write("downloading_surah_" + zznumber, (int) downloadId);
                        //mAdapter.notifyDataSetChanged();
                        make(coordinatorLayout, getString(R.string.downloading_audio) + ARG.getAyahNameFromReferenceName(ayahReferenceNumber), LENGTH_SHORT).show();
                    }
                } else {
                    Log.i(TAG, "NO NETWORK");
                    make(coordinatorLayout, R.string.no_internet, LENGTH_SHORT).show();
                }
                //} else {
                //Log.i(TAG, "NO SIGNATURE");
                //must start the app normal
                //}

            } else {
                Log.i(TAG, "PERMISSION NG Download fail");
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



    @Override
    public void MarkAsDownloaded(int ayah_id) {
        //TODO set the downloaded ayah state
        MarkAyahAsDownloaded(String.valueOf(ayah_id));
    }

    void MarkAyahAsDownloaded(String downloadedAyahId){
        Log.d(TAG, "line 1067 MARK AS DOWNLOADED " + downloadedAyahId);
        if (mAdapter != null) {
            int actual_position = 0;
            if (mAdapter.getItemCount() > 0) {
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    String ayah_ref_name = ARG.makeAyahRefName(mAdapter.getTitleAt(i).verse_id);

                    Log.d(TAG, ayah_ref_name + " matches??? downloadedAyahId " + ARG.makeAyahRefName(downloadedAyahId));

                    try {
                        if (mAdapter.getTitleAt(i) != null && ARG.makeAyahRefName(downloadedAyahId).equals(ayah_ref_name)) {
                            //Log.d(TAG, ayah_ref_name + " matches downloadedAyahId " + downloadedAyahId);
                            actual_position = i;
                            AyahRange ctitle = mAdapter.getTitleAt(actual_position);
                            Log.d(TAG,  "CTITLE " + " index:" + actual_position + ". " + ctitle.verse_id + " - verse id, audio progress: " + ctitle.audio_progress);
                            ctitle.audio_progress = 100;
                            ayahViewModel.update(ctitle);
                            //mAdapter.notifyDataSetChanged();
                        }
                    } catch (IndexOutOfBoundsException x) {
                        //Crashlytics.log(x.getMessage() + " - " + x.getStackTrace());
                    }
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
                    Log.e("CANNOT GET POSITION", Objects.requireNonNull(iobx.getMessage()));
                }
            }
            AyahRange ctitle = mAdapter.getTitleAt(actual_position);
            if (ctitle != null) {
                ctitle.audio_progress = 50;
                ayahViewModel.update(ctitle);
            }
        }
    }

    @Override
    public void SetSurahNumber(String s) {
        suraNumber2Play = ARG.makeAyahRefName(s);
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

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        stop();
        lastSurahIndex = position;
        ARG.setSuraName(String.valueOf(position + 1));
        if (sharedPreferences != null) {
            sharedPreferences.write(SharedPreferences.SELECTED_MEMORIZING_SURAH, position);
        }
        mAdapter.setText(null);
        mAdapter.setTrackList(null);
        setUIValues();
        reloadLists();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
