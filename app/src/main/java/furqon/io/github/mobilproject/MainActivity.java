package furqon.io.github.mobilproject;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.functions.FirebaseFunctions;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hotchemi.android.rate.AppRate;

import static furqon.io.github.mobilproject.BuildConfig.BUILD_TYPE;

public class MainActivity extends OptionsMenuActivity implements View.OnClickListener {
    Uri deepLink;

    CardView suralar_but;
    CardView memorize_but;
    //CardView davomi_but;
    //CardView youtube_but;
    //Button favourite_but;
    CardView search_but;
    CardView rate_but;
    //Button coins_but;
    CardView message_but;
    //Button chat_but;
    CardView audio_but;
    //CardView about_but;
    private Animation scaler;
    TextView nbadge;
    ImageView imageView;
    private Handler handler;
    // Try to use more data here. ANDROID_ID is a single point of attack.
    InterstitialAd mInterstitialAd = new InterstitialAd(this);
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "MAIN ACTIVITY";
    private static final String DEEP_LINK_URL = "https://furqon.page.link/ThB2";

    private boolean randomayahshown;

    private FirebaseFunctions mFunctions;




    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        mFunctions = FirebaseFunctions.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        if (BUILD_TYPE.equals("debug")) {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }


        handler = new Handler();
        //favourite_but = findViewById(R.id.favouritebut);
        search_but = findViewById(R.id.searchbtn);
        rate_but = findViewById(R.id.ratebtn);
        //coins_but = findViewById(R.id.earn_coins_button);
        message_but = findViewById(R.id.messageButton);
        nbadge = findViewById(R.id.numeric_badge_txt);
        nbadge.bringToFront();
        TitleViewModel titleViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);

        scaler = AnimationUtils.loadAnimation(this, R.anim.bounce);
        titleViewModel.getUnreadCount().observe(this, new Observer<List<NewMessages>>() {
            @Override
            public void onChanged(List<NewMessages> newMessages) {
                if (newMessages.size() > 0) {
                    nbadge.setVisibility(View.VISIBLE);
                    nbadge.setText(String.valueOf(newMessages.size()));
                    nbadge.startAnimation(scaler);
                } else {
                    nbadge.setVisibility(View.INVISIBLE);
                    nbadge.setText("0");
                }
            }
        });
        //chat_but = findViewById(R.id.chat_button);
        audio_but = findViewById(R.id.mediabutton);
        //about_but = findViewById(R.id.about_button);
        suralar_but = findViewById(R.id.suralar);
        memorize_but = findViewById(R.id.memorizebutton);

        //davomi_but = findViewById(R.id.davomi);
        //youtube_but = findViewById(R.id.youtubebut);
        imageView = findViewById(R.id.imageView);

        Picasso.get().load(R.mipmap.read).into((ImageView) findViewById(R.id.imageViewSuralar));
        Picasso.get().load(R.mipmap.messages).into((ImageView) findViewById(R.id.imageViewMessages));
        Picasso.get().load(R.mipmap.bookmarkjpg).into((ImageView) findViewById(R.id.imageViewMemorize));
        Picasso.get().load(R.mipmap.star).into((ImageView) findViewById(R.id.imageViewRate));
        Picasso.get().load(R.mipmap.audio).into((ImageView) findViewById(R.id.imageViewMedia));
        //Picasso.get().load(R.mipmap.youtube).into((ImageView) findViewById(R.id.imageViewYoutube));
        //Picasso.get().load(R.mipmap.search).into((ImageView) findViewById(R.id.imageViewSearch));

        imageView.setOnClickListener(this);
        //youtube_but.setOnClickListener(this);
        suralar_but.setOnClickListener(this);
        memorize_but.setOnClickListener(this);
        //davomi_but.setOnClickListener(this);
        //favourite_but.setOnClickListener(this);
        search_but.setOnClickListener(this);
        rate_but.setOnClickListener(this);
        message_but.setOnClickListener(this);
        audio_but.setOnClickListener(this);
        //about_but.setOnClickListener(this);
        //coins_but.setOnClickListener(this);
        //chat_but.setOnClickListener(this);

//        if (mSharedPref.contains(mSharedPref.XATCHUP)) {
//            davomi_but.setVisibility(View.VISIBLE);
//        } else {
//            davomi_but.setVisibility(View.GONE);
//        }

        //String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        try {
            File dir = this.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
// Create a deep link and display it in the UI
        if(deepLink==null){
            deepLink = buildDeepLink(Uri.parse(DEEP_LINK_URL), 0);
        }

//        Intent intent = new Intent(getApplicationContext(), AudioPlayerService.class);
//        intent.setAction(AudioPlayerService.ACTION_PLAY);
//        startService(intent);
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        Log.i(TAG, Locale.getDefault().getDisplayLanguage());
        Log.i(TAG, BuildConfig.BUILD_TYPE);

    }


    private void open_extraActivities() {
        Intent intent;
        intent = new Intent(this, ExtraActivity.class);
        startActivity(intent);
    }








    @Override
    protected void onStart() {
        super.onStart();
        //checkForDynamicLink();
        // Check if user is signed in (non-null) and update UI accordingly.
        //updateUI();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mSharedPref.contains(mSharedPref.XATCHUP)) {
//            davomi_but.setVisibility(View.VISIBLE);
//        }else{
//            davomi_but.setVisibility(View.GONE);
//        }
//    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mInterstitialAd.show();
    }

    //TODO ClassCastException fixed???
    private void continueReading() {
//        if (mSharedPref.contains(mSharedPref.XATCHUP)) {
//            String xatchup = mSharedPref.read(mSharedPref.XATCHUP, "");
//            if (xatchup.length() > 0) {
//                Log.i("XATCHUP", xatchup);
//                Intent intent;
//                Context context = this;
//                intent = new Intent(context, AyahList.class);
//                intent.putExtra("SURANAME", xatchup);
//                context.startActivity(intent);
//            } else {
//                Toast.makeText(getBaseContext(), getString(R.string.no_bookmarks), Toast.LENGTH_LONG).show();
//            }
//        } else {
//            Toast.makeText(getBaseContext(), getString(R.string.no_bookmarks), Toast.LENGTH_LONG).show();
//        }

    }

    public void open_suraNames() {

        Intent intent = new Intent(this, SuraNameList.class);
        startActivity(intent);

        Bundle bundle = new Bundle();
        String id = "1";
        String name = "Surlar button";
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }

    private void open_search() {
        Intent intent;
        intent = new Intent(this, Search.class);
        startActivity(intent);
    }

    public void ayahOfTheDay() {
            Intent intent = new Intent(this, AyahOfTheDay.class);
            startActivity(intent);

            //mSharedPref.write(mSharedPref.RANDOM_AYAH_SEEN, true);

    }



    private void Rateus() {
        AppRate.with(this).showRateDialog(this);
    }




    private void open_messages() {
        Intent intent;
        intent = new Intent(this, MessageList.class);
        startActivity(intent);
    }

    private void open_media_page() {
        Intent intent = new Intent(this, MediaActivity.class);
        //intent.setData(Uri.parse(mFirebaseRemoteConfig.getString("youtube_video")));
        startActivity(intent);

    }

    private void open_memory_activity() {
        Intent intent = new Intent(this, MemorizeActivity.class);
        startActivity(intent);
    }

    private void displayResult(final String result) {
        handler.post(new Runnable() {
            public void run() {
                //statusText.setText(result);
                setProgressBarIndeterminateVisibility(false);


            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {

            String data = intent.getStringExtra("data");


        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("Main", "https://furqon.page.link/deeplink");
    }

    @Override
    public void onStop() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, String.valueOf(v.getId()));
        switch (v.getId()) {
            case R.id.imageView:
                ayahOfTheDay();
                break;
            case R.id.suralar:
                open_suraNames();
                break;
            case R.id.memorizebutton:
                open_memory_activity();
                break;
//            case R.id.youtubebut:
//                open_youtube();
//                break;
            case R.id.favouritebut:
                //open_favourites();
                break;
            case R.id.searchbtn:
                open_search();
                break;
            case R.id.ratebtn:
                Rateus();
                break;
//            case R.id.earn_coins_button:
//                open_earn_coins();
//                break;
            case R.id.messageButton:
                open_messages();
                break;
//            case R.id.chat_button:
//                //open_chatroom();
//                break;
            case R.id.mediabutton:
                open_media_page();

//                if (BuildConfig.BUILD_TYPE == "debug") {
//                    open_media_page();
//                } else {
//                    open_chatroom();
//                }

                break;
//            case R.id.about_button:
//                open_about();
//                break;
        }
    }


}
