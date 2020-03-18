package furqon.io.github.mobilproject;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.LayoutInflaterCompat;
import androidx.lifecycle.LiveData;
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


public class AyahList extends AppCompatActivity implements ManageSpecials, Playable, MyListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private ArrayList<JSONObject> jsonArrayResponse;
    private ArrayList<String> trackList;

    long downloadId;
    private TitleViewModel titleViewModel;
    private AyahListAdapter mAdapter;
    MediaPlayer mediaPlayer;

    boolean isPlaying;

    Integer audio_pos;
    Integer ayah_position;
    public String suranomi;
    public String xatchup = "xatchup";
    Boolean trackDownload;
    //String suranomer;
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

    LinearLayout cl;

    private MenuItem playButton;
    private MenuItem menu_bookmark_btn;

    private NotificationManager notificationManager;


    private String suraNumber;
    private int status = 0;

    CardView download_container;
    ImageView downloadButton;
    ProgressBar progressBarDownload;
    TextView downloadText;


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
        //mRewardedVideoAd = new RewardAd(context);
        trackList = new ArrayList<String>();
        PopulateTrackList();

        sharedPref = sharedpref.getInstance();
        sharedPref.init(getApplicationContext());

        cl = findViewById(R.id.linearLayout4);

        audiorestore = getString(R.string.audiopos_restored);
        audiostore = getString(R.string.audiopos_stored);
        loadfailed = getString(R.string.audio_load_fail);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        download_container = findViewById(R.id.download_cont);
        progressBarDownload = findViewById(R.id.progressBar_download);
        downloadButton = findViewById(R.id.button_download);
        downloadText = findViewById(R.id.download_text);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            registerReceiver(broadcastReceiverAudio, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromService.class));
        }

        Toolbar toolbar = findViewById(R.id.audiobar);
        setSupportActionBar(toolbar);

        registerReceiver(broadcastReceiverDownload, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        Bundle intent = getIntent().getExtras();
        String extratext;
        if(intent!=null){
            try{
                extratext = intent.getString("SURANAME");
                assert extratext != null;
                suranomi = extratext.substring(0, extratext.indexOf(":"));
                suraNumber = extratext.substring(extratext.indexOf(":") + 1);

                Log.i("LOADED SURA", suraNumber + " " + suranomi);

                Objects.requireNonNull(getSupportActionBar()).setTitle(suranomi);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                recyclerView = findViewById(R.id.chapter_scroll);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));

                mAdapter = new AyahListAdapter(this, suranomi, suraNumber);
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
                        MyData.put("surah_id", suraNumber);
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
        titleViewModel.getTitle(suraNumber).observe(this, new Observer<ChapterTitleTable>() {
            @Override
            public void onChanged(ChapterTitleTable chapterTitleTable) {

                int currentStatus = Integer.parseInt(chapterTitleTable.status);
                if(status==0){
                    //just created
                    //Toast.makeText(getApplicationContext(), status + " FIRST SHOW " + chapterTitleTable.status, Toast.LENGTH_SHORT).show();
                    status = currentStatus;
                }else if(status==currentStatus){
                    //second or more tries
                    //Toast.makeText(getApplicationContext(), status + " FAILED TO UPDATE " + chapterTitleTable.status, Toast.LENGTH_SHORT).show();
                }else if(status<currentStatus){
                    //Toast.makeText(getApplicationContext(), status + " TITLE UPDATED " + chapterTitleTable.status, Toast.LENGTH_SHORT).show();
                    int xcoins = sharedPref.read(sharedPref.COINS, 0);
                    int newtotal;
                    if(xcoins>0){
                        newtotal = xcoins-1;
                    }else {
                        newtotal = 0;
                    }
                    sharedPref.write(sharedPref.COINS, newtotal);
                }
                SetDownloadButtonState(chapterTitleTable);
            }
        });

    }
    private void PopulateTrackList() {
        String path = getExternalFilesDir(null).getAbsolutePath();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files!=null){
            Log.d("FILES", "Size: "+ files.length);

            for (int i = 0; i < files.length; i++)
            {
                Log.d("Files",  suraNumber + " FileName:" + files[i].getName());
                String trackname = files[i].getName().toString();
                if(trackname.contains(".")){
                    trackname = trackname.substring(0, trackname.lastIndexOf("."));
                    if(!TrackDownloaded(trackname)){
                        trackList.add(trackname);
                    }
                    Log.i("TRACKNAME",  "number " + trackname + " track is available");
                }
            }
        }else{
            Log.d("NULL ARRAY", "no files found");
        }
    }
    private BroadcastReceiver broadcastReceiverDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id==downloadId){
                Log.i("DOWNLOAD COMPLETE", "Download id " + downloadId);
                //MoveFiles();

                    PopulateTrackList();
                    MarkAsDownloaded(Integer.parseInt(suraNumber));


            }else {
                Log.i("DOWNLOAD OTHER FILE", "Download id " + downloadId);
            }
        }
    };


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




    BroadcastReceiver broadcastReceiverAudio = new BroadcastReceiver() {
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
        titleViewModel.getChapterText(suraNumber).observe(this, new Observer<List<AllTranslations>>() {
            @Override
            public void onChanged(@Nullable List<AllTranslations> surahText) {
                //Toast.makeText(SuraNameList.this, "LOADING TITLES " + surahTitles.size(), Toast.LENGTH_LONG).show();
                int sc = getSurahLength(suraNumber);
                Log.e("SURAH AYAH COUNT", sc + " " +surahText.size());
                if(surahText.size()!=sc){
                    if(!httpresponse) {
                        if(surahText.size()!=0){
                            titleViewModel.deleteSurah(Integer.parseInt(suraNumber));
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
        int sl = 0;
            sl = QuranMap.GetSurahLength(Integer.parseInt(suranomer)-1);
        return sl;
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

        try{
            menu_bookmark_btn = menu.getItem(0);
            playButton = menu.getItem(1);
            ayah_position = sharedPref.read(xatchup + suranomi, 0);


            if (ayah_position > 0) {
                //BOOKMARK FOUND
                Toast.makeText(context, getString(R.string.bookmark_found), Toast.LENGTH_SHORT).show();
                menu.getItem(0).setVisible(true);
            }else{
                menu.getItem(0).setVisible(false);
            }
        }catch (IndexOutOfBoundsException iobx){

        }



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play:
                if(isPlaying){
                    OnTrackPause();
                    playButton.setIcon(R.drawable.ic_play_circle);
                }else{
                    OnTrackPlay();
                }
                return true;
//            case R.id.stop:
//                if (mediaPlayer != null) {
//                    pause();
//                     playButton.setIcon(R.drawable.ic_play_arrow_black_24dp);
//                }
//                return true;
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
                    if(trackname.equals(suraNumber)){
                        filePath = new StringBuilder().append(path).append("/").append(trackname).append(".mp3").toString();
                    }
                }
            }
        }else{
            //This surah is not available
        }
        String url; // your URL here
        if(TrackDownloaded(suraNumber)){
            url = filePath;
        }else{

            url = new StringBuilder().append("https://mobilproject.github.io/furqon_web_express/by_sura/").append(suraNumber).append(".mp3").toString();
        }

        if(!url.isEmpty()){
            Log.i("PLAY", url);
            if (mediaPlayer == null) {

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        Furqon.ShowNotification(AyahList.this, R.drawable.ic_pause_circle, suranomi, audio_pos);
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

                playButton.setIcon(R.drawable.ic_pause_circle);

            } else {
                if(isPlaying){
                    pause();
                }else{
                    mediaPlayer.release();
                    mediaPlayer = null;
                    play();
                }


            }
            trackDownload = true;
        }else{
            final PopupWindow popupWindow = new PopupWindow(this);
            View view = getLayoutInflater().inflate(R.layout.popup_hint, null);
            popupWindow.setContentView(view);
            popupWindow.showAtLocation(cl, 0, 0,0);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();
                }
            });
            trackDownload = false;
        }
    }

//    private void ShowRewardAdForThisItem() {
//            //String suranomi = suraName.getText().toString();
//            mRewardedVideoAd.SHOW();
//        }
    private void StartDownload() {
        DownloadThis(suraNumber);
        MarkAsDownloading(0);
    }
    private boolean TrackDownloaded(String v) {
        boolean retval = false;
        for (String i:trackList
        ) {
            if(i.equals(v)){
                //match found
                Log.i("TRACK DOWNLOADED?", String.valueOf(v) + " " + i + " " + (i.equals(v)));
                retval = true;
            }

        }
        return retval;
    }
    private void SetDownloadButtonState(ChapterTitleTable titleTable){


        PopulateTrackList();

        if(TrackDownloaded(suraNumber)){

            //set by the actually available audio files
            //playButton.setIcon(R.drawable.ic_play_circle);
            //Log.i("TITLES", " TRUE ");
            //downloadButton.setFocusable(false);
            download_container.setVisibility(View.INVISIBLE);
            downloadText.setText(R.string.play_local);
            downloadText.setVisibility(View.VISIBLE);
            downloadButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }else{
            if(titleTable.status.equals("2")){
                //download allowed. Active within the session only. Forgotten on restart
                downloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                //playButton.setIcon(R.drawable.ic_play_circle);
                downloadButton.setFocusable(true);
                downloadButton.setTag(2);
                progressBarDownload.setVisibility(View.INVISIBLE);
                downloadText.setText(R.string.down_or_play);
            }
            else{
                downloadButton.setImageResource(R.drawable.ic_unlock);
                downloadButton.setFocusable(true);
                downloadButton.setTag(1);
                progressBarDownload.setVisibility(View.INVISIBLE);
                downloadText.setText(R.string.unlock_or_play);
            }
        }



        Log.i("DOWNLOAD BUTTON", " " + downloadButton.getTag());

            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(mContext,"Download surah number " + suraNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                    //String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suranomer + ".mp3"; // your URL here
                    switch (downloadButton.getTag().toString()){
                        case "1"://red arrow
                            ShowCoinAlert();

                            //ShowRewardAdForThisItem();
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
    }


    private void ShowCoinAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Use coins to unlock");
        builder.setItems(R.array.unlock_actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(getApplicationContext(), i + " selected", Toast.LENGTH_SHORT).show();
                switch (i){
                    case 0:
                        int coins = sharedPref.read(sharedPref.COINS, 0);
                        if(coins>0){
                            MarkAsAwarded(Integer.parseInt(suraNumber));
                        }else{
                            Toast.makeText(getApplicationContext(),  R.string.not_enough_coins, Toast.LENGTH_LONG).show();
                        }
                        //Toast.makeText(getApplicationContext(),  R.string.use_coins, Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        //Toast.makeText(getApplicationContext(),  R.string.earn_coins, Toast.LENGTH_SHORT).show();
                        Intent intent;
                        intent = new Intent(context, EarnCoinsActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        //Toast.makeText(getApplicationContext(),  R.string.cancel, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
// Get the layout inflater

        //builder.setView(R.layout.multiple_choice_for_use_coin_dialog);


// Create the AlertDialog
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private boolean WritePermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            Log.i(  "MY PERMISSION TO WRITE", MY_PERMISSIONS_REQUEST_READ_CONTACTS + " granted?");

            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    DownloadThis(suraNumber);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    LoadTheList();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
    @Override
    public void DownloadThis(String suraNumber) {

        if (WritePermission()) {
            String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suraNumber + ".mp3"; // your URL here
            File file = new File(getExternalFilesDir(null), suraNumber + ".mp3");
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
        }


    }

    @Override
    public void LoadTitlesFromServer() {


    }

    @Override
    public void insertTitle(ChapterTitleTable title) {

    }
    @Override
    public void MarkAsDownloading(int surah_id) {
        download_container.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        playButton.setVisible(false);
        downloadText.setText(R.string.downloading);
    }
    @Override
    public void MarkAsAwarded(int surah_id) {
        //Log.e("ACTUAL SURAH ID?", surah_id + " " + suraNumber);
        downloadText.setText(R.string.down_or_play);
        playButton.setVisible(true);
        titleViewModel.updateTitleAsRewarded(suraNumber);
    }

    @Override
    public void MarkAsDownloaded(int surah_id) {
        downloadText.setText(R.string.play_local);
        playButton.setVisible(true);
        titleViewModel.updateTitleAsDownloaded(suraNumber);
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
        if(broadcastReceiverAudio!=null){
            unregisterReceiver(broadcastReceiverAudio);
        }
        if(broadcastReceiverDownload!=null){
            unregisterReceiver(broadcastReceiverDownload);
        }

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
