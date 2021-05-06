package furqon.io.github.mobilproject;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
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

        downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
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
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
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
                sharedPref.write(SharedPreferences.displayOrder, 0);
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
                sharedPref.write(SharedPreferences.displayOrder, 1);
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
        String url = mFirebaseRemoteConfig.getString("server_php") + "/ajax_quran.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        // Convert String to json object
                        jsonArrayResponse = new ArrayList<JSONObject>();

                        try {
                            Log.i(TAG, response);
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }

                            //PASS to SPINNER
                            //load auction names and available lot/bid count
                            populateSurahTitleList(jsonArrayResponse);
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

    void populateSurahTitleList(ArrayList<JSONObject> surahTitleList) {

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(, android.R.layout.simple_spinner_item, auclist);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
        ChapterTitleTable title;

        for (JSONObject i : surahTitleList
        ) {

            try {
                //Log.d(TAG, "JSONOBJECT "+ i.getString("language_no") + i.getString("uzbek"));
                //int language_no = i.getInt("language_no");
                int order_no = i.getInt("order_no");
                int chapter_id = i.getInt("chapter_id");
                String surah_type = i.getString("surah_type");
                String uzbek = i.getString("uzbek");
                String arabic = i.getString("arabic");

                title = new ChapterTitleTable(1, order_no, chapter_id, uzbek, arabic, surah_type);
                titleViewModel.insert(title);


            } catch (Exception sx) {
                Log.e("EXCEPTION", sx.getMessage());
            }
        }
    }


    private void SetButtonStates() {
        if (sharedPref.read(SharedPreferences.displayOrder, 0) == 0) {
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
                //Toast.makeText(SuraNameList.this, "TITLES " + surahTitles.size(), Toast.LENGTH_LONG).show();
                Log.d(TAG, surahTitles.size() + " sura titles");
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
    }
}