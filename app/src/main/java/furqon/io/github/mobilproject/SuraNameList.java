package furqon.io.github.mobilproject;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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

public class SuraNameList extends AppCompatActivity implements MyListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

    private TitleListAdapter mAdapter;
    private Context context;
    private ArrayList<JSONObject> jsonArrayResponse;
    long downloadId;

    private Button tempbut;
    private Button quranic_order_btn;
    private Button revelation_order_btn;

    private String suranomer = "";


    private RecyclerView recyclerView;
    private InterstitialAd mInterstitialAd;
    private sharedpref sharedPref;
    private ArrayList<String> trackList;

    private TitleViewModel titleViewModel;
    private RewardAd mRewardedVideoAd;



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

        titleViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);


        context = this;
        mRewardedVideoAd = new RewardAd(context);
        quranic_order_btn = findViewById(R.id.quranic_order);
        revelation_order_btn = findViewById(R.id.revelation_order);

        registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        trackList = new ArrayList<String>();
        PopulateTrackList();
        //requestHandler = new HTTPRequestHandler(context, titleViewModel);

        tempbut = findViewById(R.id.button);

        tempbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("CLICK", "clicking");
                //ChapterTitle chapterTitle = new ChapterTitle(1, 2, 5, "uxtext", "arabic", "Makkah");
                //titleViewModel.insert(chapterTitle);
                //requestHandler.httpRequest();

                RequestQueue queue = Volley.newRequestQueue(context);
                String url = "https://inventivesolutionste.ipage.com/ajax_quran.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //progressBar.setVisibility(View.INVISIBLE);
                                // Convert String to json object
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
                                    populateAuctionList(jsonArrayResponse);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.i("error json", "tttttttttttttttt");
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
            void populateAuctionList(ArrayList<JSONObject> auclist){

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(, android.R.layout.simple_spinner_item, auclist);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //spinner.setAdapter(adapter);
                ChapterTitleTable title;

                for (JSONObject i:auclist
                ) {

                    try{
                        Log.d("JSONOBJECT", i.getString("arabic"));
                        //int language_no = i.getInt("language_no");
                        int order_no = i.getInt("order_no");
                        int chapter_id = i.getInt("chapter_id");
                        String surah_type = i.getString("surah_type");
                        String uzbek = i.getString("uzbek");
                        String arabic = i.getString("arabic");

                        title = new ChapterTitleTable(1, order_no, chapter_id, uzbek, arabic, surah_type);
                        titleViewModel.insert(title);


                    }catch (Exception sx){
                        Log.e("EXCEPTION", sx.getMessage());
                    }
                }




            }
        });





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

//        mDatabase = DatabaseAccess.getInstance(getApplicationContext());
//        if(!mDatabase.isOpen()) {
//            mDatabase.open();
//        }

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        //suralist = mDatabase.getSuraTitles();




        //mAdapter = new SuraNameListAdapter(this, suralist, trackList, enabledList);
        mAdapter = new TitleListAdapter(this, trackList);
        recyclerView.setAdapter(mAdapter);



        LoadTheList();

        MobileAds.initialize(this, getString(R.string.addmob_app_id));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_fullpage));
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
    }

    private void SetButtonStates() {
        if(sharedPref.read(sharedPref.displayOrder, 0)==0){
            quranic_order_btn.setBackgroundColor(getResources().getColor(R.color.gold));
            revelation_order_btn.setBackgroundColor(0);
        }else{
            revelation_order_btn.setBackgroundColor(getResources().getColor(R.color.gold));
            quranic_order_btn.setBackgroundColor(0);
        }
    }

    private void LoadTheList() {


        titleViewModel.getAllTitles().observe(this, new Observer<List<ChapterTitleTable>>() {
            @Override
            public void onChanged(@Nullable List<ChapterTitleTable> surahTitles) {
                //Toast.makeText(SuraNameList.this, "LOADING TITLES " + surahTitles.size(), Toast.LENGTH_LONG).show();
                if(surahTitles.size()!=114){
                    tempbut.setVisibility(View.VISIBLE);
                    //titleViewModel.deleteAll();
                }else{
                    tempbut.setVisibility(View.GONE);
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
//        if(mDatabase!=null) {
//            mDatabase.close();
//        }

        if(broadcastReceiver!=null){
            unregisterReceiver(broadcastReceiver);
        }
    }
//
//    @Override
//    public void EnableThis(String suraNumber) {
////        if(NotInTheList(enabledList, suraNumber)){
////            enabledList.add(suraNumber);
////        }
//    }
//
    @Override
    public void LoadTitlesFromServer() {
        Log.d("LOADED FROM SERVER", " ");
//        if(requestHandler!=null){
//            requestHandler.httpRequest();
//        }else{
//            //tempbut.setVisibility(View.VISIBLE);
//
//        }
    }
    @Override
    public void insertTitle(ChapterTitleTable title){
        Log.d("TITLE insert", title.uzbek);
        titleViewModel.insert(title);
    }

    @Override
    public void MarkAsAwarded(int surah_id) {
        int actual_position = surah_id;
        for(int i=0;i<mAdapter.getItemCount();i++){
            if(mAdapter.getTitleAt(i).chapter_id==surah_id){
                actual_position = i;
            }
        }
        Log.e("ACTUAL SURAH ID?", surah_id + " " + actual_position);
        ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
        ctitle.status = "2";
        titleViewModel.update(ctitle);
    }

    @Override
    public void MarkAsDownloaded(int surah_id) {
        if(mAdapter!=null){
            int actual_position = surah_id;
            for(int i=0;i<mAdapter.getItemCount();i++){
                try{
                    if(mAdapter.getTitleAt(i).chapter_id==surah_id){
                        actual_position = i;
                    }
                }catch (IndexOutOfBoundsException iobx){
                    Log.e("CANNOT GET POSITION", iobx.getMessage());
                }
            }
            ChapterTitleTable ctitle = mAdapter.getTitleAt(actual_position);
            if(!ctitle.status.equals("3"))
            {
                ctitle.status = "3";
                titleViewModel.update(ctitle);
            }
        }
    }

    @Override
    public void MarkAsDownloading(int surah_id) {
        //TODO if quit while downloading, the progressbar is left permanently on
        //ChapterTitleTable ctitle = mAdapter.getTitleAt(surah_id);
        //ctitle.status = "4";
        //titleViewModel.update(ctitle);
    }


    @Override
    public void DownloadThis(String suraNumber) {
        suranomer = suraNumber;
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

            Log.i("PERMISSION OK", "Download start " + url);
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

    private void PopulateTrackList() {
        //TODO clean up wrong files
        String path = getExternalFilesDir(null).getAbsolutePath();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files!=null){
            //Log.d("FILES", "Size: "+ files.length);

            for (int i = 0; i < files.length; i++)
            {
                //Log.d("Files", "FileName:" + files[i].getName());
                String trackname = files[i].getName().toString();
                if(trackname.contains(".")){
                    trackname = trackname.substring(0, trackname.lastIndexOf("."));
                    try{
                        int tt = Integer.parseInt(trackname);
                        trackList.add(trackname);
                        MarkAsDownloaded(tt);
                    }catch (NumberFormatException nfx){
                        Log.e("TRACKNAME ERROR", trackname);
                        DeleteTheFile(files[i]);
                        //TODO delete the file with x-y.mp3 naming format (dual download)
                    }
                    //Log.i("TRACKNAME", trackname);
                }
            }
        }else{
            Log.d("NULL ARRAY", "no files found");
        }
    }

    private void DeleteTheFile(File file) {
        try{
            file.delete();
        }catch (SecurityException sx){
            Log.e("FAILED to DELETE", sx.getMessage());
        }
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
                    DownloadThis(suranomer);

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
}