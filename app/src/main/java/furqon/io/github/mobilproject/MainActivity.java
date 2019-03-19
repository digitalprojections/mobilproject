package furqon.io.github.mobilproject;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {
    public EditText name;
    public Button suralar_but;
    public Button davomi_but;
    public Button fav_but;
    public Button set_but;
    private Handler handler;
    private TextView statusText;

    // Try to use more data here. ANDROID_ID is a single point of attack.
    private InterstitialAd mInterstitialAd;
    private FirebaseAnalytics mFirebaseAnalytics;

    SharedPreferences sharedPreferences;

    @Override
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

                default:
                    return super.onOptionsItemSelected(item);
        }

    }

    private void open_settings(){
        Intent intent;
        intent = new Intent(this, furqon.io.github.mobilproject.Settings.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        if (sharedPreferences.contains("switch1")) {

        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            ;
            editor.putBoolean("switch1", true);
            editor.apply();
        }
        if (sharedPreferences.contains("switch2")) {

        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            ;
            editor.putBoolean("switch2", true);
            editor.apply();
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(this, "ca-app-pub-3838820812386239~2342916878");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        handler = new Handler();

        suralar_but = findViewById(R.id.suralar);
        davomi_but = findViewById(R.id.davomi);
        fav_but = findViewById(R.id.favorites);
        set_but = findViewById(R.id.setbut);
        statusText = findViewById(R.id.result);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        davomi_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueReading();
            }
        });

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
        set_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_settings();
            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInterstitialAd.show();
    }

    private void continueReading(){
        Toast.makeText(getBaseContext(), "Coming soon", Toast.LENGTH_LONG);
    }

    public void openSuraNames(View view){

        Intent intent = new Intent(this, SuraNameList.class);
        startActivity(intent);

        Bundle bundle = new Bundle();
        String id = "1";
        String name = "Surlar button";
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    public void openFavourites(){
        Intent intent = new Intent(this, Favourites.class);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Coming soon", Toast.LENGTH_LONG);
    }
    private void displayResult(final String result) {
        handler.post(new Runnable() {
            public void run() {
                //statusText.setText(result);
                setProgressBarIndeterminateVisibility(false);


            }
        });
    }

}
