package furqon.io.github.mobilproject;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.File;
import java.util.List;
import java.util.Locale;

import hotchemi.android.rate.AppRate;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Uri deepLink;

    Button suralar_but;
    Button davomi_but;
    Button youtube_but;
    //Button favourite_but;
    Button search_but;
    Button rate_but;
    //Button coins_but;
    Button message_but;
    //Button chat_but;
    Button audio_but;
    Button about_but;
    private Animation scaler;
    TextView nbadge;
    ImageView imageView;
    private Handler handler;
    // Try to use more data here. ANDROID_ID is a single point of attack.
    //InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "MAIN ACTIVITY";
    private static final String DEEP_LINK_URL = "https://furqon.page.link/ThB2";
    private SharedPreferences mSharedPref;
    private boolean randomayahshown;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_i:
                shareDeepLink();
                return true;
            case R.id.settings_i:
                open_settings();
                return true;
            case R.id.favourites_i:
                open_favourites();
                return true;
//            case R.id.messages_i:
//                open_messages();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mSharedPref = SharedPreferences.getInstance();
        mSharedPref.init(getApplicationContext());

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Fabric.with(this, new Crashlytics());
        Crashlytics.log("Activity created");


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, getString(R.string.addmob_app_id));

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
        about_but = findViewById(R.id.about_button);

        suralar_but = findViewById(R.id.suralar);
        davomi_but = findViewById(R.id.davomi);
        youtube_but = findViewById(R.id.youtubebut);
        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(this);
        youtube_but.setOnClickListener(this);
        suralar_but.setOnClickListener(this);
        davomi_but.setOnClickListener(this);
        //favourite_but.setOnClickListener(this);
        search_but.setOnClickListener(this);
        rate_but.setOnClickListener(this);
        message_but.setOnClickListener(this);
        audio_but.setOnClickListener(this);
        about_but.setOnClickListener(this);
        //coins_but.setOnClickListener(this);
        //chat_but.setOnClickListener(this);

        if (mSharedPref.contains(mSharedPref.XATCHUP)) {
            davomi_but.setVisibility(View.VISIBLE);
        } else {
            davomi_but.setVisibility(View.GONE);
        }

        //String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        try {
            File dir = this.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.log(Log.ERROR, TAG, "NPE caught");
            Crashlytics.logException(e);
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

    private void open_settings() {
        Intent intent;
        intent = new Intent(this, Settings.class);
        startActivity(intent);
    }
    private void shareDeepLink() {
        createShortLink();
    }

    public Uri buildDeepLink(Uri dl, int version) {
        String uriPrefix = "furqon.page.link";

        if(currentUser!=null){
            Log.i(TAG, "CURRENT USER ID " + currentUser.getUid());
            //userId=currentUser.getUid()
            if (mSharedPref != null && !mSharedPref.read(mSharedPref.CREDS_ALREADY_SENT, false)) {
                mSharedPref.write(mSharedPref.USERID, currentUser.getUid());
                //checkAppSignature(this);
            }

        }

        DynamicLink.Builder builder = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setDomainUriPrefix(uriPrefix)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(version)
                        .build())
                .setLink(dl);

        // Build the dynamic link
        DynamicLink link = builder.buildDynamicLink();
        // [END build_dynamic_link]


        // Return the dynamic link as a URI
        return link.getUri();
    }
    public void createShortLink() {
        // [START create_short_link]
        if(currentUser!=null){
            String val = "https://quran-kareem.web.app/?user_id="+currentUser.getUid();
            Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    //.setLink(Uri.parse("https://furqon.page.link/ThB2"))
                    .setLink(Uri.parse(val))
                    .setDomainUriPrefix("https://furqon.page.link")
                    .setAndroidParameters(
                            new DynamicLink.AndroidParameters.Builder("furqon.io.github.mobilproject")
                                    .setMinimumVersion(0)
                                    .build())

                    // Set parameters
                    // ...
                    .buildShortDynamicLink()
                    .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                        @Override
                        public void onComplete(@NonNull Task<ShortDynamicLink> task) {

                                if (task.isSuccessful()) {
                                    // Short link created

                                        Uri shortLink = task.getResult().getShortLink();
                                    Uri flowchartLink = task.getResult().getPreviewLink();
                                    Log.i(TAG, "SHORT LINK " + shortLink.getPath());
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_SUBJECT, R.string.quran_kareem_title);
                                    intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                    startActivity(intent);
                                    //Log.i("SHARE", deepLink.getPath());

                                } else {
                                    // Error
                                    // ...
                                    Crashlytics.log(Log.ERROR, TAG, task.getResult().toString());
                                }


                        }
                    });
        }
        // [END create_short_link]
    }



    @Override
    protected void onStart() {
        super.onStart();
        //checkForDynamicLink();
        // Check if user is signed in (non-null) and update UI accordingly.
        //updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSharedPref.contains(mSharedPref.XATCHUP)) {
            davomi_but.setVisibility(View.VISIBLE);
        }else{
            davomi_but.setVisibility(View.GONE);
        }
    }

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
        if (mSharedPref.contains(mSharedPref.XATCHUP)) {
            String xatchup = mSharedPref.read(mSharedPref.XATCHUP, "");
            if (xatchup.length() > 0) {
                Log.i("XATCHUP", xatchup);
                Intent intent;
                Context context = this;
                intent = new Intent(context, AyahList.class);
                intent.putExtra("SURANAME", xatchup);
                context.startActivity(intent);
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.no_bookmarks), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.no_bookmarks), Toast.LENGTH_LONG).show();
        }

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

    public void ayahOfTheDay() {
            Intent intent = new Intent(this, AyahOfTheDay.class);
            startActivity(intent);

            //mSharedPref.write(mSharedPref.RANDOM_AYAH_SEEN, true);

    }

    private void open_youtube() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(mFirebaseRemoteConfig.getString("youtube_video")));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void open_chatroom() {
        Toast.makeText(getApplicationContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
    }

    private void open_earn_coins() {
        //Toast.makeText(getApplicationContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
        Intent intent;
        intent = new Intent(this, EarnCoinsActivity.class);
        startActivity(intent);
    }

    private void Rateus() {
        AppRate.with(this).showRateDialog(this);
    }

    private void open_favourites() {
        Intent intent;
        intent = new Intent(this, Favourites.class);
        startActivity(intent);
    }

    private void open_search() {
        Intent intent;
        intent = new Intent(this, Search.class);
        startActivity(intent);
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
    private void open_about() {
        Intent intent = new Intent(this, AboutActivity.class);
        //intent.setData(Uri.parse(mFirebaseRemoteConfig.getString("youtube_video")));
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
            case R.id.davomi:
                continueReading();
                break;
            case R.id.youtubebut:
                open_youtube();
                break;
            case R.id.favouritebut:
                open_favourites();
                break;
            case R.id.searchbtn:
                open_search();
                break;
            case R.id.ratebtn:
                Rateus();
                break;
            case R.id.earn_coins_button:
                open_earn_coins();
                break;
            case R.id.messageButton:
                open_messages();
                break;
            case R.id.chat_button:
                //open_chatroom();
                break;
            case R.id.mediabutton:
                open_media_page();

//                if (BuildConfig.BUILD_TYPE == "debug") {
//                    open_media_page();
//                } else {
//                    open_chatroom();
//                }

                break;
            case R.id.about_button:
                open_about();
                break;
        }
    }


}
