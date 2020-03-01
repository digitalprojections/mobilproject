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
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Objects;

public class SuraNameList extends AppCompatActivity implements MyListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private SuraNameListAdapter mAdapter;
    private DatabaseAccess mDatabase;
    private Cursor suralist;
    private ArrayList<String> enabledList = new ArrayList<String>();
    long downloadId;


    RecyclerView recyclerView;
    InterstitialAd mInterstitialAd;
    private sharedpref sharedPref;
    private ArrayList<String> trackList;


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
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

    private void open_settings(){
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

        registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        trackList = new ArrayList<String>();
        PopulateTrackList();

        //https://inventivesolutionste.ipage.com/ajax_quran.php
        //POST
        //action:names_as_objects
        //language_id:1

        sharedPref = sharedpref.getInstance();
        sharedPref.init(getApplicationContext());
        //getSupportActionBar().setTitle("Suralar");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mDatabase = DatabaseAccess.getInstance(getApplicationContext());
        if(!mDatabase.isOpen()) {
            mDatabase.open();
        }

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        suralist = mDatabase.getSuraTitles();




        mAdapter = new SuraNameListAdapter(this, suralist, trackList, enabledList);
        recyclerView.setAdapter(mAdapter);



        MobileAds.initialize(this, "ca-app-pub-3838820812386239~2342916878");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }



    @Override
    protected void onResume() {
        //mRewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //mRewardedVideoAd.destroy(this);
        super.onDestroy();
        mInterstitialAd.show();
        if(mDatabase!=null) {
            mDatabase.close();
        }

        if(broadcastReceiver!=null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void EnableThis(String suraNumber) {
        if(NotInTheList(enabledList, suraNumber)){
            enabledList.add(suraNumber);
        }
    }

    private boolean NotInTheList(ArrayList<String> enabledList, String suraNumber) {
        boolean retval = false;
        for (String i:enabledList
             ) {
            if(i.equals(suraNumber)){
                retval = true;
            }
        }
        return retval;
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
                        .setDescription("Downloading")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setDestinationUri(Uri.fromFile(file))
                        .setRequiresCharging(false)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true);
            } else {
                request = new DownloadManager.Request(Uri.parse(url))
                        .setTitle(suraNumber)
                        .setDescription("Downloading")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setDestinationUri(Uri.fromFile(file))
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true);
            }

            Log.i("PERMISSION GRANTED", "Download start " + url);
            DownloadManager downloadManager = (DownloadManager) this.getSystemService(this.DOWNLOAD_SERVICE);
            downloadId = downloadManager.enqueue(request);
        }


    }



    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id==downloadId){
                Log.i("DOWNLOAD COMPLETE", "Download id " + downloadId);
                //MoveFiles();
                PopulateTrackList();
            }else {
                Log.i("DOWNLOAD FAILED", "Download id " + downloadId);
            }
        }
    };

    /*private void MoveFiles() {
        File src = new File(Environment.getExternalStorageDirectory(), "SurahAudio");
        File dst = new File(getFilesDir().getAbsolutePath(), "Surah Audio");
        dst.mkdirs();
        try{
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();

            try
            {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
            finally
            {
                if (inChannel != null)
                    inChannel.close();
                if (outChannel != null)
                    outChannel.close();
            }
        }catch (FileNotFoundException fnfx){
            Toast.makeText(this, fnfx.getMessage(), Toast.LENGTH_LONG);

        }catch (IOException iox){
            Toast.makeText(this, iox.getMessage(), Toast.LENGTH_LONG);
        }

    }*/
    private void PopulateTrackList() {
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
                    trackList.add(trackname);
                    Log.i("TRACKNAME", trackname);
                }
            }
        }else{
            Log.d("NULL ARRAY", "no files found");
        }
    }
    private boolean WritePermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                Log.i(  "MY PERMISSION TO WRITE", MY_PERMISSIONS_REQUEST_READ_CONTACTS + " granted?");
            }
            return false;
        } else {
            // Permission has already been granted
            return true;
        }
    }
}