package furqon.io.github.mobilproject;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuraNameList extends AppCompatActivity {
    private static final String TAG = "SURANAMELIST";
    private static final int MY_PERMISSIONS_WRITE_TO_DISK = 0;
    private TitleListAdapter mAdapter;
    private Context context;
    private ArrayList<JSONObject> jsonArrayResponse;
    long downloadId;
//    CardView download_container;
//    ImageView downloadButton;

    ProgressBar progressBar;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    boolean download_attempted;
    private Button tempbut;
    private Button quranic_order_btn;
    private Button revelation_order_btn;
    private String suranomer = "";
    private RecyclerView recyclerView;
    private InterstitialAd mInterstitialAd;
    private SharedPreferences sharedPref;
    private ArrayList<String> trackList;
    private TitleViewModel titleViewModel;
    DownloadManager downloadManager;
    DownloadManager.Query query;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_i:
                open_settings();
                return true;
            case R.id.favourites_i:
                open_favourites();
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

    private void open_favourites() {
        Intent intent;
        intent = new Intent(this, furqon.io.github.mobilproject.Favourites.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sura_name_list);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        titleViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);


        context = this;

        quranic_order_btn = findViewById(R.id.quranic_order);
        revelation_order_btn = findViewById(R.id.revelation_order);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        //registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//        download_container = findViewById(R.id.download_cont);
//        progressBarDownload = findViewById(R.id.progressBar_download);
//        downloadButton = findViewById(R.id.button_download);

        downloadManager = (DownloadManager) this.getSystemService(this.DOWNLOAD_SERVICE);
        query = new DownloadManager.Query();

        //trackList = new ArrayList<String>();
        //PopulateTrackList();
        //requestHandler = new HTTPRequestHandler(context, titleViewModel);

        tempbut = findViewById(R.id.button);
        progressBar = findViewById(R.id.tl_progressBar);



        //https://inventivesolutionste.ipage.com/ajax_quran.php
        //POST
        //action:names_as_objects
        //language_id:1

        sharedPref = SharedPreferences.getInstance();
        sharedPref.init(getApplicationContext());
        //getSupportActionBar().setTitle("Suralar");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //suralist = mDatabase.getSuraTitles();


        //mAdapter = new SuraNameListAdapter(this, suralist, trackList, enabledList);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TitleListAdapter(this, trackList);
        recyclerView.setAdapter(mAdapter);


        LoadTheList();

        MobileAds.initialize(this, getString(R.string.addmob_app_id));
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE == "debug") {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        quranic_order_btn.setClickable(true);
        revelation_order_btn.setClickable(true);

        SetButtonStates();

        //LOAD in Mushaf order
        quranic_order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPref.write(sharedPref.displayOrder, 0);
                quranic_order_btn.setBackgroundColor(getResources().getColor(R.color.gold));
                revelation_order_btn.setBackgroundColor(0);
                mAdapter.notifyDataSetChanged();

                LoadTheList();
            }
        });

        //LOAD in revelation order
        revelation_order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPref.write(sharedPref.displayOrder, 1);
                revelation_order_btn.setBackgroundColor(getResources().getColor(R.color.gold));
                quranic_order_btn.setBackgroundColor(0);
                mAdapter.notifyDataSetChanged();

                LoadTheList();
            }
        });
        tempbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadTitles();
            }
        });

    }

    private void LoadTitles() {
        Log.i(TAG, "CLICK THE TEMP BUTTON");
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = mFirebaseRemoteConfig.getString("server_link") + "/ajax_quran.php";
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
                            progressBar.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Log.i("error json", "tttttttttttttttt");
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
        progressBar.setVisibility(View.VISIBLE);
    }

    void populateAuctionList(ArrayList<JSONObject> auclist) {

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(, android.R.layout.simple_spinner_item, auclist);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
        ChapterTitleTable title;

        for (JSONObject i : auclist
        ) {

            try {
                Log.d(TAG, "JSONOBJECT " + i.getString("language_no") + i.getString("uzbek"));
                int language_no = i.getInt("language_no");
                int order_no = i.getInt("order_no");
                int chapter_id = i.getInt("chapter_id");
                String surah_type = i.getString("surah_type");
                String uzbek = i.getString("uzbek");
                String arabic = i.getString("arabic");

                title = new ChapterTitleTable(language_no, order_no, chapter_id, uzbek, arabic, surah_type);
                titleViewModel.insert(title);


            } catch (Exception sx) {
                Log.e("EXCEPTION", sx.getMessage());
            }
        }
    }


    private void SetButtonStates() {
        if (sharedPref.read(sharedPref.displayOrder, 0) == 0) {
            quranic_order_btn.setBackgroundColor(getResources().getColor(R.color.gold));
            revelation_order_btn.setBackgroundColor(0);
        } else {
            quranic_order_btn.setBackgroundColor(0);
            revelation_order_btn.setBackgroundColor(getResources().getColor(R.color.gold));
        }
        //recyclerView.scheduleLayoutAnimation();
    }

    private void LoadTheList() {


        titleViewModel.getAllTitles().observe(this, new Observer<List<ChapterTitleTable>>() {
            @Override
            public void onChanged(@Nullable List<ChapterTitleTable> surahTitles) {
                //Toast.makeText(SuraNameList.this, "LOADING TITLES " + surahTitles.size(), Toast.LENGTH_LONG).show();
                if (surahTitles.size() != 114) {
                    //tempbut.setVisibility(View.VISIBLE);
                    if (!download_attempted) {
                        Log.e(TAG, "LOADING LIST");
                        download_attempted = true;
                        LoadTitles();
                        tempbut.setVisibility(View.GONE);
                    }

                    //titleViewModel.deleteAll();
                } else {
                    tempbut.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);

                }
                mAdapter.setTitles(surahTitles);
                SetButtonStates();
            }
        });
    }


    @Override
    protected void onResume() {
        //mRewardedVideoAd.resume(this);
        super.onResume();
        SetButtonStates();
    }

    @Override
    protected void onPause() {
        //mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //mRewardedVideoAd.destroy(this);

        //mInterstitialAd.show();
        super.onDestroy();
//        if (broadcastReceiver != null) {
//            unregisterReceiver(broadcastReceiver);
//        }
    }
//
//    @Override
//    public void EnableThis(String suraNumber) {
////        if(NotInTheList(enabledList, suraNumber)){
////            enabledList.add(suraNumber);
////        }
//    }
//

//
//    @Override
//    public void LoadTitlesFromServer() {
//
//    }
//
//    @Override
//    public void insertTitle(ChapterTitleTable title) {
//        //Log.d(TAG, "TITLE insert " + title.uzbek);
//        titleViewModel.insert(title);
//    }

//    @Override
//    public void MarkAsAwarded(int surah_id) {
//        int actual_position = surah_id;
//        for (int i = 0; i < mAdapter.getItemCount(); i++) {
//            if (mAdapter.getTitleAt(i).chapter_id == surah_id) {
//                actual_position = i;
//            }
//        }
//        Log.e(TAG, "ACTUAL SURAH ID?" + surah_id + " " + actual_position);
//        ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
//        ctitle.status = "2";
//        titleViewModel.update(ctitle);
//    }
//
//    @Override
//    public void MarkAsDownloaded(int surah_id) {
//        if (mAdapter != null) {
//            int actual_position = surah_id;
//            for (int i = 0; i < mAdapter.getItemCount(); i++) {
//                try {
//                    if (mAdapter.getTitleAt(i).chapter_id == surah_id) {
//                        actual_position = i;
//                    }
//                } catch (IndexOutOfBoundsException iobx) {
//                    Log.e("CANNOT GET POSITION", iobx.getMessage());
//                }
//            }
//            ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
//            if (!ctitle.status.equals("3")) {
//                ctitle.status = "3";
//                titleViewModel.update(ctitle);
//            }
//        }
//    }
//
//    @Override
//    public void MarkAsDownloading(int surah_id) {
//
//        //mInterstitialAd.show();
//        //TODO if quit while downloading, the progressbar is left permanently on
//        if (mAdapter != null) {
//            int actual_position = surah_id;
//            for (int i = 0; i < mAdapter.getItemCount(); i++) {
//                try {
//                    if (mAdapter.getTitleAt(i).chapter_id == surah_id) {
//                        actual_position = i;
//                    }
//                } catch (IndexOutOfBoundsException iobx) {
//                    Log.e("CANNOT GET POSITION", iobx.getMessage());
//                }
//            }
//            ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
//            if (!ctitle.status.equals("4")) {
//                ctitle.status = "4";
//                titleViewModel.update(ctitle);
//            }
//        }
//    }
//
//    @Override
//    public void DownloadThis(String suraNumber) {
//        suranomer = suraNumber;
//        if (WritePermission()) {
//            String url = "https://mobilproject.github.io/furqon_web_express/by_sura/" + suraNumber + ".mp3"; // your URL here
//            File file = new File(getExternalFilesDir(null), suraNumber + ".mp3");
//            DownloadManager.Request request;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                request = new DownloadManager.Request(Uri.parse(url))
//                        .setTitle(suraNumber)
//                        .setDescription("Downloading")
//                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//                        .setDestinationUri(Uri.fromFile(file))
//                        .setRequiresCharging(false)
//                        .setAllowedOverMetered(true)
//                        .setAllowedOverRoaming(true);
//            } else {
//                request = new DownloadManager.Request(Uri.parse(url))
//                        .setTitle(suraNumber)
//                        .setDescription("Downloading")
//                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//                        .setDestinationUri(Uri.fromFile(file))
//                        .setAllowedOverMetered(true)
//                        .setAllowedOverRoaming(true);
//            }
//
//            Log.i(TAG, "PERMISSION OK, Download start " + url);
//
//
//            downloadId = downloadManager.enqueue(request);
//            sharedPref.write("download_" + downloadId, suranomer); //storing the download id under the right sura reference. We can use the id later to check for download status
//            sharedPref.write("downloading_surah_" + suranomer, (int) downloadId);
//            //MarkAsDownloading(Integer.parseInt(suranomer));
///*
//            query.setFilterById(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PENDING|DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_SUCCESSFUL);
//            Cursor cursor = downloadManager.query(query);
//            if(cursor!=null){
//                for(int i=0;i<cursor.getCount();i++){
//                    Log.i(TAG, cursor.getInt(i) + " download " + cursor.);
//                    cursor.moveToNext();
//                }
//            }
//*/
//        }
//    }


//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//            /*
//            if (id == downloadId) {
//                Log.i(TAG, "DOWNLOAD COMPLETE, Download id " + downloadId);
//                sharedPref.write("download_"+suranomer, 0);
//                //MoveFiles();
//                PopulateTrackList();
//            } else {
//                Log.i(TAG, "DOWNLOAD FAILED, Download id " + downloadId);
//            }*/
//            query.setFilterById(id);
//            Cursor cursor = downloadManager.query(query);
//            if (cursor.moveToFirst()) {
//                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
//                int status = cursor.getInt(columnIndex);
//                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
//                int reason = cursor.getInt(columnReason);
//
//                if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                    Log.i(TAG, "DOWNLOAD COMPLETE, Download id " + id);
//                    //Retrieve the saved download id
//                    String suraid = sharedPref.read("download_" + id, "0");
//                    if (Integer.parseInt(suraid) > 0) {
//                        sharedPref.write("download_" + id, "0");
//                        //PopulateTrackList();
//                    }
//                } else if (status == DownloadManager.STATUS_FAILED) {
//                    Toast.makeText(SuraNameList.this,
//                            "FAILED!\n" + "reason of " + reason,
//                            Toast.LENGTH_LONG).show();
//                } else if (status == DownloadManager.STATUS_PAUSED) {
//                    Toast.makeText(SuraNameList.this,
//                            "PAUSED!\n" + "reason of " + reason,
//                            Toast.LENGTH_LONG).show();
//                } else if (status == DownloadManager.STATUS_PENDING) {
//                    Toast.makeText(SuraNameList.this,
//                            "PENDING!",
//                            Toast.LENGTH_LONG).show();
//                } else if (status == DownloadManager.STATUS_RUNNING) {
//                    Toast.makeText(SuraNameList.this,
//                            "RUNNING!",
//                            Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    };


//    private void PopulateTrackList() {
//
//        //TODO Any file that is added here is from the old version
//        //new version supports multiple languages and uses structured folders to sort out
//
//        //TODO clean up wrong files
//        String path = getExternalFilesDir(null).getAbsolutePath();
//        Log.d(TAG, "Files: Path: " + path);
//        File directory = new File(path);
//        File[] files = directory.listFiles();
//        if (files != null) {
//            //Log.d("FILES", "Size: "+ files.length);
//
//            for (int i = 0; i < files.length; i++) {
//                //Log.d("Files", "FileName:" + files[i].getName());
//                String trackname = files[i].getName().toString();
//                if (trackname.contains(".")) {
//                    trackname = trackname.substring(0, trackname.lastIndexOf("."));
//                    try {
//                        int tt = Integer.parseInt(trackname);
//                        trackList.add(trackname);
//                        MarkAsDownloaded(tt);
//                    } catch (NumberFormatException nfx) {
//                        Log.e(TAG, "TRACKNAME ERROR " + trackname);
//                        DeleteTheFile(files[i]);
//                        //TODO delete the file with x-y.mp3 naming format (dual download)
//                    }
//                    //Log.i("TRACKNAME", trackname);
//                }
//            }
//        } else {
//            Log.d(TAG, "NULL ARRAY no files found");
//        }
//    }
//
//    private boolean WritePermission() {
//
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//
//            // No explanation needed; request the permission
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_WRITE_TO_DISK);
//
//            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//            // app-defined int constant. The callback method gets the
//            // result of the request.
//            Log.i(TAG, "MY PERMISSION TO WRITE granted?");
//
//            return false;
//        } else {
//            // Permission has already been granted
//            return true;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_WRITE_TO_DISK: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                    DownloadThis(suranomer);
//
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    LoadTheList();
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request.
//        }
//    }
//
//    private void DeleteTheFile(File file) {
//        try {
//            file.delete();
//        } catch (SecurityException sx) {
//            Log.e(TAG, "FAILED to DELETE " + sx.getMessage());
//        }
//    }
}