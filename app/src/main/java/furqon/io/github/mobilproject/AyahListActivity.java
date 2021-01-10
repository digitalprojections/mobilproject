package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AyahListActivity extends AppCompatActivity implements ManageSpecials {

    private static final String TAG = AyahListActivity.class.getSimpleName();
    private ArrayList<JSONObject> jsonArrayResponse;
    String url; // your URL here
    private TitleViewModel titleViewModel;
    private AyahListActivityAdapter mAdapter;
    boolean download_attempted;
    final Handler delayHandler = new Handler();
    TextView contentloading;

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
    Handler handler;
    Runnable runnable;
    private SharedPreferences sharedPref;
    private Button tempbut;
    private Context context;
    private boolean httpresponse;
    RecyclerView recyclerView;
    InterstitialAd mInterstitialAd;
    LinearLayout cl;

    private MenuItem playButton;
    private MenuItem menu_bookmark_btn;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private String suraNumber;




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

        sharedPref = SharedPreferences.getInstance();
        sharedPref.init(getApplicationContext());

        cl = findViewById(R.id.linearLayout4);

        audiorestore = getString(R.string.audiopos_restored);
        audiostore = getString(R.string.audiopos_stored);
        loadfailed = getString(R.string.audio_load_fail);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        contentloading = findViewById(R.id.al_textView);
        contentloading.setVisibility(View.GONE);

        Toolbar toolbar = findViewById(R.id.audiobar);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE == "debug") {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdClicked() {
                Bundle params = new Bundle();
                params.putString("activity_name", "AyahList");
                params.putString("full_text", "ad clicked");
                mFirebaseAnalytics.logEvent("share_image", params);
            }
        });



        Bundle intent = getIntent().getExtras();
        String extratext;
        if(intent!=null){
            try{
                extratext = intent.getString("SURANAME");
                assert extratext != null;
                suranomi = extratext.substring(0, extratext.indexOf(":"));
                suraNumber = extratext.substring(extratext.indexOf(":") + 1);
            }
            catch (NullPointerException npx){
                Toast.makeText( getBaseContext(), R.string.error_message, Toast.LENGTH_SHORT).show();
            }
        }else{
            suranomi = QuranMap.SURAHNAMES[0];
            suraNumber = "1";
        }

        audio_pos = sharedPref.read(suranomi, 0);
        handler = new Handler();

        recyclerView = findViewById(R.id.chapter_scroll);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new AyahListActivityAdapter(this, suranomi, suraNumber);
        recyclerView.setAdapter(mAdapter);
        LoadTheList();
        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.i(TAG, "LOADED SURA " + suraNumber + " " + suranomi);
        getSupportActionBar().setTitle(suranomi);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tempbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadSurah();
            }
        });


    }

    private void LoadSurah() {

        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.i(TAG, "CLICK clicking");
                RequestQueue queue = Volley.newRequestQueue(context);
                String url = mFirebaseRemoteConfig.getString("server_php") + "/ajax_quran.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                contentloading.setVisibility(View.GONE);
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
                                    if (BuildConfig.BUILD_TYPE.equals("debug"))
                                        Log.i(TAG, "error json ttttttttttttttttt");
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (BuildConfig.BUILD_TYPE.equals("debug"))
                            Log.i(TAG, "ERROR RESPONSE enable reload button");
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
        tempbut.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
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
                        text = new ChapterTextTable(chapter_id, verse_id,0, DatabaseID, OrderNo, AyahText, "", surah_type);
                        titleViewModel.insertText(text);
                    }catch (Exception sx){
                        if (BuildConfig.BUILD_TYPE.equals("debug"))
                            Log.e(TAG, "EXCEPTION " + sx.getMessage());
                    }
                }
    }






    private void LoadTheList() {
        titleViewModel.getChapterText(suraNumber).observe(this, new Observer<List<AllTranslations>>() {
            @Override
            public void onChanged(@Nullable List<AllTranslations> surahText) {
                //Toast.makeText(SuraNameList.this, "LOADING TITLES " + surahTitles.size(), Toast.LENGTH_LONG).show();
                int sc = getSurahLength(suraNumber);
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.e("SURAH AYAH COUNT", sc + " " +surahText.size());
                if(surahText.size()!=sc){
                    if(!httpresponse) {
                        if(surahText.size()!=0){
                            titleViewModel.deleteSurah(Integer.parseInt(suraNumber));
                            Toast.makeText(getApplicationContext(), R.string.surah_size_issue, Toast.LENGTH_LONG).show();
                        }
                        if (!download_attempted) {
                            tempbut.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);

                            contentloading.setVisibility(View.VISIBLE);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Do something after 5s = 5000ms
                                    contentloading.setVisibility(View.GONE);
                                    if (mInterstitialAd.isLoaded()&& !sharedPref.read(SharedPreferences.NOMOREADS, false))
                                        mInterstitialAd.show();
                                }
                            }, 3000);

                            LoadSurah();
                            download_attempted = true;
                        }
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
                Intent intent = new Intent(context, MediaActivity.class);
                intent.putExtra("suranumber", suraNumber);
                startActivity(intent);
//                if(isPlaying){
//                    OnTrackPause();
//                    playButton.setIcon(R.drawable.ic_play_circle);
//                }else{
//                    OnTrackPlay();
//                    playButton.setIcon(R.drawable.ic_pause_circle);
//                }
                return true;
            case R.id.menu_bookmark_button:
                recyclerView.scrollToPosition(ayah_position-1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void UpdateSpecialItem(ChapterTextTable text) {

        titleViewModel.updateText(text);

    }

}
