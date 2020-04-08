package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class EarnCoinsActivity extends AppCompatActivity implements ManageCoins {

    private ImageButton share_btn;
    private ImageButton watchAds_btn;
    private TextView coins_txt;

    private TextView rewardad_txt;
    private TextView share_txt;

    private static final String DEEP_LINK_URL = "https://furqon.page.link/ThB2";
    private SharedPreferences mSharedPref;
    Uri deepLink;
    String userid;
    private RewardAd mRewardedVideoAd;
    private AdView mAdView;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earn_coins);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.earn_coins);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedPref = SharedPreferences.getInstance();
        mSharedPref.init(getApplicationContext());
        mRewardedVideoAd = new RewardAd(this);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);


        // Create a deep link and display it in the UI
        deepLink = buildDeepLink(Uri.parse(DEEP_LINK_URL), 0);


        coins_txt = findViewById(R.id.coins_count_txt);
        rewardad_txt = findViewById(R.id.rewardad_description_tv);
        share_txt = findViewById(R.id.share_description_tv);
        share_btn = findViewById(R.id.ShareImageButton);
        watchAds_btn = findViewById(R.id.WatchAdsImageButton);
        //watchAds_btn.setEnabled(false);

        rewardad_txt.setText(mFirebaseRemoteConfig.getString("ad_reward") + " " + getText(R.string.coins));
        share_txt.setText(mFirebaseRemoteConfig.getString("share_reward") + " " + getText(R.string.coins));

        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDeepLink();
            }
        });

        //Snackbar.make(share_btn, "Earn Coins", BaseTransientBottomBar.LENGTH_INDEFINITE).show();

        watchAds_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO watch reward ads to earn coins
                mRewardedVideoAd.SHOW();
            }
        });
        mAdView = findViewById(R.id.adViewEarnCoins);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        setCoinValue();
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE == "debug") {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setCoinValue();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //setCoinValue();
    }

    private void setCoinValue() {
        if(watchAds_btn!=null){
            watchAds_btn.setEnabled(true);
        }
        if(coins_txt!=null){
//            if (!mSharedPref.read(mSharedPref.INITIAL_COINS_USED, false)) {
//                mSharedPref.write(mSharedPref.COINS, mSharedPref.read(mSharedPref.INITIAL_COINS, 0));
//                mSharedPref.write(mSharedPref.INITIAL_COINS_USED, true);
//            }
            String mycoins = String.valueOf(mSharedPref.read(mSharedPref.COINS, 0));

            coins_txt.setText(mycoins);
        }else{
//            if (!mSharedPref.read(mSharedPref.INITIAL_COINS_USED, false)) {
//                mSharedPref.write(mSharedPref.COINS, mSharedPref.read(mSharedPref.INITIAL_COINS, 0));
//                mSharedPref.write(mSharedPref.INITIAL_COINS_USED, true);
//            }
            TextView coins_txt = findViewById(R.id.coins_count_txt);
            String mycoins = String.valueOf(mSharedPref.read(mSharedPref.COINS, 0));
            coins_txt.setText(mycoins);
        }

        //Log.d("COINS", mSharedPref.read(mSharedPref.INITIAL_COINS_USED, false) + " " + mSharedPref.read(mSharedPref.INITIAL_COINS, 0));

    }

    private void ShowRewardAdForThisItem() {
        //String suranomi = suraName.getText().toString();
        mRewardedVideoAd.SHOW();
    }
    private void shareDeepLink() {
        if(userid!=null){
            Log.i("SHARING", userid);
            //userId=currentUser.getUid()
            createShortLink();
        }
        else{
            //user failed to login anonymously
            //sharing impossible, meaningless?
        }

    }

    public Uri buildDeepLink(Uri dl, int version) {
        String uriPrefix = "furqon.page.link";

        userid = mSharedPref.read(mSharedPref.USERID, null);

        // Set dynamic link parameters:
        //  * URI prefix (required)
        //  * Android Parameters (required)
        //  * Deep link
        // [START build_dynamic_link]


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
        String val = "https://quran-kareem.web.app/?user_id="+userid;
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
                            Log.i("SHORT LINK", shortLink.getPath());
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_SUBJECT, R.string.quran_kareem_title);
                            intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                            startActivity(intent);
                            //Log.i("SHARE", deepLink.getPath());
                        } else {
                            // Error
                            // ...
                            Log.i("LINK ERROR", task.getResult().toString());
                        }
                    }
                });
        // [END create_short_link]
    }

    @Override
    public void SetCoinValues() {
        Log.d("EarnCoins:", "setcoinsvalue");
        setCoinValue();
    }

    @Override
    public void UseCoins(int val) {
        Log.d("EarnCoins:", "use coins");
    }

    @Override
    public void EarnCoins() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInterstitialAd.show();
    }
}
