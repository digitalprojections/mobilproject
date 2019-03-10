package furqon.io.github.mobilproject;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.vending.licensing.*;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {
    public EditText name;
    public Button suralar_but;
    public Button davomi_but;
    public Button fav_but;
    private Handler handler;
    private TextView statusText;
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAggasiiEUvzcuJMFQ27n/9NVVFYbKIlOpcWtVAH0TyuxqigxMdgEcy1XE0rZ0w8gnmRNr33T2pBZRk2ApwppDsrH7iC9xhW/J2QW/mPZy/OMHCVvrEHdTRfmdkc4ONuCTj7sjwJCsW9YtP2Mu/oK4I98bHaDzO7g6yZsN4c+Ia9RRlCcR4bg1410iHoKNONoGYMzOgStrFwM/uNkXUk60B74A9+EptXAFcOJyLX3wlEdDxkPTTuhEtv5Y1fVoPsK2FweyiSDk5XghXqCsysV0zKYVbAQv2uiTXQg2aIMWT4dL1w4i9fGWttnvaVfqMPE9pRo4C4TZk3eBt3QulKDJBwIDAQAB";

    private LicenseCheckerCallback licenseCheckerCallback;
    private LicenseChecker checker;
    private static final byte[] SALT = new byte[] {
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95,
            -45, 77, -117, -36, -113, -11, 32, -64, 89
    };
    // Try to use more data here. ANDROID_ID is a single point of attack.
    private InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, "ca-app-pub-3838820812386239~2342916878");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        handler = new Handler();
        licenseCheckerCallback = new MyLicenseCheckerCallback();
        suralar_but = findViewById(R.id.suralar);
        davomi_but = findViewById(R.id.davomi);
        fav_but = findViewById(R.id.favorites);
        statusText = findViewById(R.id.result);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        checker = new LicenseChecker(
                this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), deviceId)),
                BASE64_PUBLIC_KEY  // Your public licensing key.
        );
        doCheck();
        suralar_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openSuraNames(view);


                //create query

            }
        });
        fav_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFavourites();
            }
        });



    }
    private void doCheck() {

        setProgressBarIndeterminateVisibility(true);
        //statusText.setText("checking...");
        checker.checkAccess(licenseCheckerCallback);

        /*Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);*/
    }
    public void openSuraNames(View view){
        Intent intent = new Intent(this, SuraNameList.class);
        startActivity(intent);
    }
    public void openFavourites(){
        Intent intent = new Intent(this, Favourites.class);
        startActivity(intent);

    }
    private void displayResult(final String result) {
        handler.post(new Runnable() {
            public void run() {
                //statusText.setText(result);
                setProgressBarIndeterminateVisibility(false);
                mInterstitialAd.show();

            }
        });
    }
    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // Should allow user access.
            displayResult(getString(R.string.allow));
        }

        public void dontAllow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            displayResult(getString(R.string.dont_allow));

            if (reason == Policy.RETRY) {
                // If the reason received from the policy is RETRY, it was probably
                // due to a loss of connection with the service, so we should give the
                // user a chance to retry. So show a dialog to retry.
                statusText.setText("RETRY");
            } else {
                // Otherwise, the user is not licensed to use this app.
                // Your response should always inform the user that the application
                // is not licensed, but your behavior at that point can vary. You might
                // provide the user a limited access version of your app or you can
                // take them to Google Play to purchase the app.

            }
        }

        @Override
        public void applicationError(int errorCode) {
            statusText.setText("ERROR");

        }
    }
}
