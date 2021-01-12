package furqon.io.github.mobilproject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.MediaRouteButton;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AyahOfTheDayActivity extends AppCompatActivity {
    private static final String TAG = AyahOfTheDayActivity.class.getSimpleName();
    private int random_surah;
    private int random_ayah;
    private int cursor_retry = 0;

    private String suraname;
    private int language_id = 4;
    private boolean FavouriteSelected;

    //public DatabaseAccess mDatabase;
    //Cursor ayahcursor;
    TitleViewModel viewModel;
    List<RandomSurah> randomSurahs;
    List<AllTranslations> allTranslationsList;
    TextView ayah_reference;
    TextView ayah_text;


    ImageButton prev_btn;
    ImageButton next_btn;
    ImageButton share_btn;
    ImageButton home_btn;
    ImageButton find_btn;
    ImageButton fav_btn;
    ProgressBar pbar;
    FloatingActionButton fab;
    TextView uztxt;
    TextView rutxt;
    TextView entxt;
    boolean isOpen = false;
    Animation fabopen, fabclose, scaler;
    private SharedPreferences sharedPref;
    private Context mContext;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private ArrayList<JSONObject> jsonArrayResponse;
    private boolean httpresponse;
    private Button openChaptersBtn;


    public AyahOfTheDayActivity() {
        sharedPref = SharedPreferences.getInstance();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayah_of_the_day);
        mContext = this;
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        //mDatabase = DatabaseAccess.getInstance(getApplicationContext());
//        if (!mDatabase.isOpen()) {
//            mDatabase.open();
//        }


        viewModel = ViewModelProviders.of(this).get(TitleViewModel.class);

        pbar = findViewById(R.id.progBar);
        uztxt = findViewById(R.id.uztxt);
        rutxt = findViewById(R.id.rutxt);
        entxt = findViewById(R.id.entxt);
        entxt = findViewById(R.id.entxt);
        prev_btn = findViewById(R.id.previous_verse);
        next_btn = findViewById(R.id.next_verse);
        share_btn = findViewById(R.id.random_share);
        home_btn = findViewById(R.id.random_home);
        find_btn = findViewById(R.id.random_find);
        fav_btn = findViewById(R.id.random_favourite);

        fabopen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabclose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        scaler = AnimationUtils.loadAnimation(this, R.anim.bounce);

        ayah_reference = findViewById(R.id.random_verse_title);
        ayah_text = findViewById(R.id.random_verse_text);
        fab = findViewById(R.id.fab);
        openChaptersBtn = findViewById(R.id.openChaptersButton);
        pbar.setVisibility(View.INVISIBLE);
        //DONE upo create choose a random sura and ayah


        //get a random verse within the range available in that sura
        openChaptersBtn.setVisibility(View.GONE);
        openChaptersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SuraNameList.class);
                startActivity(intent);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.startAnimation(scaler);

                animateFabs();
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.i(TAG, " random number");
            }
        });

        uztxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.i(TAG, " UZBEK LANGUAGE");
                snackbarMessage(view, getString(R.string.language_selected));
                animateFabs();
                language_id = 4;
                sharedPref.write("r_language_id", language_id);
                ShowTheAyahBeside();
            }

        });
        rutxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbarMessage(view, getString(R.string.language_selected));
                animateFabs();
                language_id = 5;
                sharedPref.write("r_language_id", language_id);
                ShowTheAyahBeside();
            }
        });
        entxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbarMessage(view, getString(R.string.language_selected));
                animateFabs();
                language_id = 6;
                sharedPref.write("r_language_id", language_id);
                ShowTheAyahBeside();
            }
        });
        sharedPref.init(this);

        if (sharedPref.contains("r_language_id")) {
            language_id = sharedPref.read("r_language_id", 1);
        } else {
            sharedPref.write("r_language_id", language_id);
        }





        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (random_ayah > 1) {
                    random_ayah--;
                    prev_btn.startAnimation(scaler);
                    ShowTheAyahBeside();
                }
            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (random_ayah < QuranMap.GetSurahLength(random_surah-1)) {
                        random_ayah++;
                        next_btn.startAnimation(scaler);
                        ShowTheAyahBeside();
                    }
            }
        });
        //
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share_btn.startAnimation(scaler);
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, ayah_text.getText() + "\n(" + suraname + ", " + (random_ayah-1) + ")\nhttps://goo.gl/sXBkNt\nQuran Kareem, Android\n(" + getApplicationContext().getResources().getText(R.string.shareayah) + ")");
//                sendIntent.putExtra(Intent.EXTRA_TEXT, ayah_text.getText() + "\n(" + suraname + ", " + random_ayah + ")\n"+shortLink+"\n"+ mContext.getResources().getText(R.string.seeTranslations));
//                sendIntent.setType("text/plain");
//                startActivity(Intent.createChooser(sendIntent, getApplicationContext().getResources().getText(R.string.shareayah)));
                createDynamicLink_Basic();
            }
        });
        //
        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                home_btn.startAnimation(scaler);
                //AyahOfTheDay.super.onBackPressed();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        //
        find_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                find_btn.startAnimation(scaler);
                sharedPref.write("xatchup" + suraname, random_ayah);
                sharedPref.write("xatchup", suraname + ":" + random_surah);
                continueReading();
            }
        });
        //
        fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fav_btn.startAnimation(scaler);
                addToFavourites(view);
            }
        });
        //makeCall();
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        if(appLinkData!=null) {

            //if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.d(TAG, appLinkAction + " - " + appLinkData.getQueryParameter("sn") + appLinkData.getQueryParameter("an"));
            String sntext = appLinkData.getQueryParameter("sn");
            if(sntext!=null)
                random_surah = Integer.parseInt(sntext);
            String antext = appLinkData.getQueryParameter("an");
            if(antext!=null)
                random_ayah = Integer.parseInt(antext);
            makeCall();

        }else{
            Log.w(TAG, "NO LINK FOUND");
            makeCall();
        }


    }
    private void makeCall(){
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {


                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...
                        try{
                            random_surah = Integer.parseInt(deepLink.getQueryParameter("chapter"));
                            random_ayah = Integer.parseInt(deepLink.getQueryParameter("verse"));
                        }catch (NullPointerException | NumberFormatException npx){
                            viewModel.getRandomSurah().observe((LifecycleOwner) mContext, new Observer<List<RandomSurah>>() {
                                @Override
                                public void onChanged(List<RandomSurah> randomSurah) {
                                    openChaptersBtn.setVisibility(View.GONE);
                                    randomSurahs = randomSurah;
                                    if(FavouriteSelected)
                                    {
                                        ShowTheAyahBeside();
                                        FavouriteSelected = false;
                                    }else{
                                        displayRandomAyah();
                                    }

                                }
                            });
                        }

                        if(random_surah!=0 && random_ayah!=0){
                            //we have the surah reference
                            displaySelectedAyah();
                        }
                        //Log.d(TAG, "sura: " +  + ", verse: "+);
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    public void createDynamicLink_Basic() {
        // [START create_link_basic]
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://mobilproject.github.io/furqon_web_express/?chapter=" + random_surah+"&verse="+random_ayah))
                .setDomainUriPrefix("https://furqon.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
                .setIosParameters(new DynamicLink.IosParameters.Builder("https://mobilproject.github.io/furqon_web_express").build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();
        // [END create_link_basic]

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(dynamicLinkUri.toString()))
                .buildShortDynamicLink()
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if(random_surah>=0 && random_ayah>=0){
                            if (task.isSuccessful()) {
                                // Short link created
                                Uri shortLink = task.getResult().getShortLink();
                                Uri flowchartLink = task.getResult().getPreviewLink();
                                //Log.d(TAG, shortLink + " short dynamic link");
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, ayah_text.getText() + "\n(" + QuranMap.SURAHNAMES[random_surah-1] + ", " + random_ayah + ")\n"+shortLink+"\n"+ mContext.getResources().getText(R.string.seeTranslations));
                                sendIntent.setType("text/plain");
                                mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.shareayah)));
                                Log.d(TAG, "manual link: " + ayah_text.getText() + "\n(" + QuranMap.SURAHNAMES[random_surah-1] + ", " + random_ayah + ")\n"+ shortLink +"\n"+ mContext.getResources().getText(R.string.seeTranslations));
                            } else {
                                // Error
                                // ...
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, ayah_text.getText() + "\n(" + QuranMap.SURAHNAMES[random_surah-1] + ", " + random_ayah + ")\nhttps://goo.gl/sXBkNt\n"+ mContext.getResources().getText(R.string.seeTranslations));
                                sendIntent.setType("text/plain");
                                mContext.startActivity(Intent.createChooser(sendIntent, mContext.getResources().getText(R.string.shareayah)));
                                Log.d(TAG, "manual link: " + ayah_text.getText() + "\n(" + QuranMap.SURAHNAMES[random_surah-1] + ", " + random_ayah + ")\n"+ mContext.getResources().getText(R.string.seeTranslations));
                            }
                        }
                        
                    }
                });
    }
    private int AnAvailableSurahID() {
        int randomSurahNumber;
        if(randomSurahs!=null && randomSurahs.size()>0){
            //get the size of the list.
            int rslen = randomSurahs.size();//value between 1 and 114 inclusive
            randomSurahNumber = (int) Math.floor(Math.random()*rslen);//get a random value of between 0-113
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.i("RANDOM SURAH LIST", randomSurahNumber + ", " + randomSurahs.get(randomSurahNumber).sura_id);
            return randomSurahs.get(randomSurahNumber).sura_id;//the actual surah id based on the random number generated above
        }
        else{
            //if the database is empty, return 0
            return 0;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        //mDatabase.close();
    }

    private void continueReading() {
        if (sharedPref.contains(sharedPref.XATCHUP)) {
            String xatchup = sharedPref.read(sharedPref.XATCHUP, "");
            if (xatchup.length() > 0) {
                Intent intent;
                Context context = this;
                intent = new Intent(context, AyahListActivity.class);
                intent.putExtra("SURANAME", xatchup);
                context.startActivity(intent);
            }
        } else {
            Toast.makeText(this, getString(R.string.no_bookmarks), Toast.LENGTH_LONG).show();
        }

    }

    private void addToFavourites(View view) {
        // manage sqlite creation and data addition
        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.i("AYAT FAVOURITED", String.valueOf(view));
        if(allTranslationsList!=null){
                if (fav_btn.getTag() == "1") {
                    //mDatabase.removeFromFavs(random_surah, random_ayah, "0");

                        allTranslationsList.get(random_ayah-1).favourite = 0;
                } else {
                    //mDatabase.saveToFavs(random_surah, random_ayah, "1");

                        allTranslationsList.get(random_ayah-1).favourite = 1;
                }

            ChapterTextTable text = MapTextObjects(allTranslationsList.get(random_ayah-1));
            FavouriteSelected = true;
            viewModel.updateText(text);
        }
        //SetFavouriteIconState();
    }
    private ChapterTextTable MapTextObjects(AllTranslations allTranslations) {
        ChapterTextTable ctext = new ChapterTextTable(allTranslations.sura_id, allTranslations.verse_id, allTranslations.favourite, 1, allTranslations.order_no, allTranslations.ar_text, allTranslations.comments_text, allTranslations.surah_type);
        ctext.setId(allTranslations.id);
        return ctext;
    }

    private void snackbarMessage(View view, String s) {
        Snackbar.make(view, s, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void ShowTheAyahBeside() {
        String randomayahreference = getString(R.string.surah) + " " + random_surah + " " + suraname + getString(R.string.ayah) + random_ayah;
        ayah_reference.setText(randomayahreference);
        ayah_text.setText(getTextByLanguage());
        SetFavouriteIconState();
    }

    private String getTextByLanguage() {
        String ltext ="";
        if(allTranslationsList!=null && allTranslationsList.size()>0){
            switch (language_id){
                case 5:
                    ltext = allTranslationsList.get(random_ayah-1).ru_text;
                    break;
                case 6:
                    ltext = allTranslationsList.get(random_ayah-1).en_text;
                    break;
                default:
                    ltext = allTranslationsList.get(random_ayah-1).uz_text;
                    break;
            }
        }

        return ltext;
    }

    private void displayRandomAyah() {
        random_surah = AnAvailableSurahID();
        if(random_surah==0){
            //database is empty. quit
            ayah_text.setText(R.string.chapters_not_available);
            //Intent intent = new Intent(getBaseContext(), MainActivity.class);
            //startActivity(intent);
            String randomayahreference = getString(R.string.surah) + " " + getString(R.string.unavailable) + " " + getString(R.string.ayah) + getString(R.string.unavailable);
            openChaptersBtn.setVisibility(View.VISIBLE);
            ayah_reference.setText(randomayahreference);
            return;
        }else{
            random_ayah = (int) Math.floor(Math.random() * QuranMap.GetSurahLength(random_surah-1))+1;
            //Log.d("RANDOM SURAH MAX AYAH", random_surah + " is surah " + QuranMap.GetSurahLength(random_surah-1));
            viewModel.getChapterText(String.valueOf(random_surah)).observe(this, new Observer<List<AllTranslations>>() {
                @Override
                public void onChanged(List<AllTranslations> allTranslations) {
                    allTranslationsList = allTranslations;
                    ShowRandomAyah(allTranslations);
                }
            });
        }
    }
    private void displaySelectedAyah() {
            viewModel.getChapterText(String.valueOf(random_surah)).observe(this, new Observer<List<AllTranslations>>() {
                @Override
                public void onChanged(List<AllTranslations> allTranslations) {
                    allTranslationsList = allTranslations;
                    ShowRandomAyah(allTranslations);
                }
            });
        }

    private void ShowRandomAyah(List<AllTranslations> allTranslations) {

        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.d(TAG, random_surah + " is surah " + random_ayah);
        try{
            suraname = QuranMap.SURAHNAMES[random_surah-1];//DONE fix it to the actual suraname
            String randomayahreference = getString(R.string.surah) + " " + random_surah + " " + suraname + getString(R.string.ayah) + random_ayah;
            ayah_reference.setText(randomayahreference);
            ayah_text.setText(getTextByLanguage());

            SetFavouriteIconState();

            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.d(TAG, suraname + "-" + random_surah + " " + random_ayah);

        }catch (IndexOutOfBoundsException iobx) {
            ayah_text.setText(R.string.failed_to_load_ayah);
            if(QuranMap.GetSurahLength(random_surah)>=random_ayah){
                //the chapter may not exist
                LoadSurah();
            }
        }catch (Exception x){
            Toast.makeText(this, R.string.failure_generic, Toast.LENGTH_SHORT).show();
        }
    }

    private void SetFavouriteIconState() {
        if(allTranslationsList!=null){
            int is_fav = allTranslationsList.get(random_ayah-1).favourite;
            if (BuildConfig.BUILD_TYPE.equals("debug"))
                Log.d(TAG, "fav state is " + is_fav);
            if(is_fav!=0){
                fav_btn.setImageResource(R.drawable.ic_favorite_black_24dp);
                fav_btn.setTag("1");
            } else {
                fav_btn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                fav_btn.setTag("0");
            }
        }
    }
    private void LoadSurah() {

        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.i(TAG, "CLICK clicking");
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/ajax_quran.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Convert String to json object
                        pbar.setVisibility(View.GONE);
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
                pbar.setVisibility(View.GONE);
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("action", "izohsiz_text_obj"); //Add the data you'd like to send to the server.
                MyData.put("database_id", "1, 120, 59, 79");
                MyData.put("surah_id", String.valueOf(random_surah));
                //https://inventivesolutionste.ipage.com/ajax_quran.php
                //POST
                //action:names_as_objects
                //language_id:1
                return MyData;
            }
        };
        pbar.setVisibility(View.VISIBLE);
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
                viewModel.insertText(text);
            }catch (Exception sx){
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.e(TAG, "EXCEPTION " + sx.getMessage());
            }
        }
    }

    private void animateFabs() {
        if (isOpen) {

            uztxt.startAnimation(fabclose);
            rutxt.startAnimation(fabclose);
            entxt.startAnimation(fabclose);

            uztxt.setClickable(false);
            rutxt.setClickable(false);
            entxt.setClickable(false);
            isOpen = false;
        } else {
            uztxt.startAnimation(fabopen);
            rutxt.startAnimation(fabopen);
            entxt.startAnimation(fabopen);

            uztxt.setClickable(true);
            rutxt.setClickable(true);
            entxt.setClickable(true);

            isOpen = true;

        }
    }
}
