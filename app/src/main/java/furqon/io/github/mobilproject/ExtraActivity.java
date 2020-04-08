package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;
import java.util.Objects;

public class ExtraActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "EAXTRAACTIVITY";
    Button favourite_but;
    Button search_but;
    //Button youtube_but;
    Button rate_but;
    Button coins_but;
    Button message_but;
    Button chat_but;
    Button audio_but;
    InterstitialAd mInterstitialAd;

    TextView nbadge;
    private AdView mAdView;
    FirebaseRemoteConfig mFirebaseRemoteConfig;


    private Animation scaler;
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//
//        inflater.inflate(R.menu.options_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.invite_i:
//                //shareDeepLink();
//                return true;
//            case R.id.settings_i:
//                //open_settings();
//                return true;
//            case R.id.favourites_i:
//                open_favourites();
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);
        //=============================================
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.extra_title);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        favourite_but = findViewById(R.id.favouritebut);
        search_but = findViewById(R.id.searchbtn);
        //youtube_but = findViewById(R.id.youtubebut);
        rate_but = findViewById(R.id.ratebtn);
        coins_but = findViewById(R.id.earn_coins_button);
        message_but = findViewById(R.id.messageButton);
        nbadge = findViewById(R.id.numeric_badge_txt);
        nbadge.bringToFront();
        chat_but = findViewById(R.id.chat_button);
        audio_but = findViewById(R.id.mediabutton);

        favourite_but.setOnClickListener(this);
        search_but.setOnClickListener(this);
        //youtube_but.setOnClickListener(this);
        rate_but.setOnClickListener(this);
        coins_but.setOnClickListener(this);
        message_but.setOnClickListener(this);
        chat_but.setOnClickListener(this);
        audio_but.setOnClickListener(this);



        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(3) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();


        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
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

        mAdView = findViewById(R.id.adViewExtra);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE == "debug") {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInterstitialAd.show();
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, String.valueOf(v.getId()));
        switch (v.getId()) {
            case R.id.favouritebut:
                open_favourites();
                break;
            case R.id.searchbtn:
                open_search();
                break;
            case R.id.ratebtn:
                Rateus();
                break;
            case R.id.youtubebut:
                open_youtube();
                break;
            case R.id.earn_coins_button:
                open_earn_coins();
                break;
            case R.id.messageButton:
                open_messages();
                break;
            case R.id.chat_button:
                open_chatroom();
                break;
            case R.id.mediabutton:
                if (BuildConfig.BUILD_TYPE == "debug") {
                    open_media_page();
                } else {
                    open_chatroom();
                }

                break;

        }
    }

    private void open_media_page() {
        Intent intent = new Intent(this, MediaActivity.class);
        //intent.setData(Uri.parse(mFirebaseRemoteConfig.getString("youtube_video")));
        startActivity(intent);

    }


}
