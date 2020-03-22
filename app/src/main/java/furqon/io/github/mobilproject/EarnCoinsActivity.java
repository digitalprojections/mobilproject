package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.usage.NetworkStats;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import org.w3c.dom.Text;

import java.util.Objects;

public class EarnCoinsActivity extends AppCompatActivity implements ManageCoins {

    private ImageButton share_btn;
    private ImageButton watchAds_btn;
    private TextView coins_txt;
    private static final String DEEP_LINK_URL = "https://furqon.page.link/ThB2";
    private sharedpref sharedPref;
    Uri deepLink;
    String userid;
    private RewardAd mRewardedVideoAd;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earn_coins);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.earn_coins);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        sharedPref = sharedpref.getInstance();
        sharedPref.init(getApplicationContext());
        mRewardedVideoAd = new RewardAd(this);

        coins_txt = findViewById(R.id.coins_count_txt);

        setCoinValue();
        // Create a deep link and display it in the UI
        deepLink = buildDeepLink(Uri.parse(DEEP_LINK_URL), 0);



        share_btn = findViewById(R.id.ShareImageButton);
        watchAds_btn = findViewById(R.id.WatchAdsImageButton);
        //watchAds_btn.setEnabled(false);

        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDeepLink();
            }
        });

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
            String mycoins = String.valueOf(sharedPref.read(sharedPref.COINS, 0));
            coins_txt.setText(mycoins);
        }else{
            TextView coins_txt = findViewById(R.id.coins_count_txt);
            String mycoins = String.valueOf(sharedPref.read(sharedPref.COINS, 0));
            coins_txt.setText(mycoins);
        }

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

        userid = sharedPref.read(sharedPref.USERID, null);

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
}
