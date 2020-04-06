package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Semaphore;

import furqon.io.github.mobilproject.Services.OnClearFromService;

public class MediaActivity extends AppCompatActivity implements MyListener, ManageCoins, Playable {
    private static final int MY_WRITE_EXTERNAL_STORAGE = 101;
    private static final String TAG = "MediaActivity";
    private ArrayList<Track> trackList;
    private TitleViewModel titleViewModel;
    private MediaActivityAdapter mAdapter;
    private RecyclerView recyclerView;
    private Spinner spinner;
    private SpinnerAdapter spinnerAdapter;



    private Context context;

    long downloadId;
    CardView download_container;
    ImageView downloadButton;
    ProgressBar progressBarDownload;
    TextView downloadText;
    String suraNumber;
    public String suranomi;
    TextView cost_txt;
    TextView coins_txt;

    Integer audio_pos;
    Integer ayah_position;

    String audiorestore;
    String audiostore;
    String loadfailed;
    private MenuItem playButton;
    private int ayah_unlock_cost;
    private int available_coins;
    private int currentStatus;
    private int status = 0;
    InterstitialAd mInterstitialAd;
    Handler handler;
    private NotificationManager notificationManager;
    private MediaPlayer mediaPlayer;
    boolean isPlaying;

    private sharedpref sharedPref;
    ProgressBar progressBar;
    private Runnable runnable;
    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        //recyclerView.setAdapter(mAdapter);

//
//        download_container = findViewById(R.id.download_cont);
//        progressBarDownload = findViewById(R.id.progressBar_download);
//        downloadButton = findViewById(R.id.button_download);
//        downloadText = findViewById(R.id.download_text);
        registerReceiver(broadcastReceiverAudio, new IntentFilter("TRACKS_TRACKS"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadcastReceiverDownload, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            startService(new Intent(getBaseContext(), OnClearFromService.class));
        }

        trackList = new ArrayList<Track>();

        recyclerView = findViewById(R.id.mp_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MediaActivityAdapter(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.available_languages, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.mp_spinner_item);
        // Apply the adapter to the spinner
        spinner = findViewById(R.id.mp_language_spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimary));

                Log.d(TAG, adapter.getItem(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        PopulateTrackList();
    }

    private BroadcastReceiver broadcastReceiverDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == downloadId) {
                Log.i(TAG, "DOWNLOAD COMPLETE Download id " + downloadId);
                //MoveFiles();

                PopulateTrackList();
                MarkAsDownloaded(Integer.parseInt(suraNumber));


            } else {
                Log.i(TAG, "DOWNLOAD OTHER FILE Download id " + downloadId);
            }
        }

    };

    private void PopulateTrackList() {
        String path = getExternalFilesDir(null).getAbsolutePath();
        Log.d(TAG, "Files Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            Log.d("FILES", "Size: " + files.length);

            for (int i = 0; i < files.length; i++) {
                //Log.d(TAG, "Files " + suraNumber + " FileName:" + files[i].getName());

                if (files[i].getName().contains(".")) {
                    String trackname = files[i].getName().substring(0, files[i].getName().lastIndexOf("."));
                    if (!TrackDownloaded(files[i].getName())) {
                        String filePath = new StringBuilder().append(path).append("/").append(files[i].getName()).toString();
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
        }


    }

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

    private void StartDownload() {
        DownloadThis(suraNumber);

    }

    private boolean TrackDownloaded(String v) {
        boolean retval = false;
        for (Track i : trackList
        ) {
            if (i.getName().equals(v)) {
                //match found
                //Log.i("TRACK DOWNLOADED?", String.valueOf(v) + " " + i + " " + (i.equals(v)));
                retval = true;
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
        available_coins = sharedPref.read(sharedPref.COINS, 0);
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
            MarkAsDownloading(0);
        } else {
            Log.i("PERMISSION NG", "Download fail");
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

        int coins = sharedPref.read(sharedPref.COINS, 0);
        if (coins > ayah_unlock_cost) {
            titleViewModel.updateTitleAsRewarded(suraNumber);
        } else {
            Toast.makeText(this, R.string.not_enough_coins, Toast.LENGTH_LONG).show();
        }
        downloadText.setText(R.string.down_or_play);
        playButton.setVisible(true);

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

    private void storeAudioPosition() {


        //sharedPref.write(suranomi, mediaPlayer.getCurrentPosition());

        Toast.makeText(getBaseContext(), audiostore, Toast.LENGTH_SHORT).show();

    }

    public void resume() {
        audio_pos = sharedPref.read(suranomi, 0);

        //if (mediaPlayer != null) {

        //seekBar.setProgress(pos);
        //   mediaPlayer.seekTo(audio_pos);
        //   mediaPlayer.start();
        Toast.makeText(getBaseContext(), audiorestore, Toast.LENGTH_SHORT).show();
        //playCycle();
        // }
    }

    @Override
    public void SetCoinValues() {
        Log.d("AYAHLIST:", "setcoinsvalue");


        available_coins = sharedPref.read(sharedPref.COINS, 0);


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
        available_coins = sharedPref.read(sharedPref.COINS, 0);
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

    private void SetDownloadButtonState(ChapterTitleTable titleTable) {


        PopulateTrackList();

        if (TrackDownloaded(suraNumber)) {
            //set by the actually available audio files
            //playButton.setIcon(R.drawable.ic_play_circle);
            //Log.i("TITLES", " TRUE ");
            //downloadButton.setFocusable(false);
            download_container.setVisibility(View.INVISIBLE);
            //downloadText.setText(R.string.play_local);
            downloadText.setVisibility(View.VISIBLE);
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

                downloadButton.setImageResource(R.drawable.ic_unlock);
                downloadButton.setFocusable(true);
                downloadButton.setTag(1);
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
    }

    public void pause() {
        if (isPlaying) {
            sharedpref.getInstance().write(suranomi, mediaPlayer.getCurrentPosition());
            isPlaying = false;
            Intent intent = new Intent(this, AudioService.class);
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
            if (broadcastReceiverAudio != null) {
                unregisterReceiver(broadcastReceiverAudio);
            }
        } catch (IllegalArgumentException iax) {

        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (broadcastReceiverDownload != null) {
                    unregisterReceiver(broadcastReceiverDownload);
                }

            }
        } catch (IllegalArgumentException iax) {
            Crashlytics.log("AYAHLIST " + iax.getMessage() + iax.getStackTrace());
        }
        mInterstitialAd.show();
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
}
