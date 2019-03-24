package furqon.io.github.mobilproject;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public EditText name;
    public Button suralar_but;
    public Button davomi_but;

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
            }
        });


        try {
            File dir = this.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}



    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
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

    private void continueReading() {
        if (sharedPreferences.contains("xatchup")) {
            String cp = sharedPreferences.getString("xatchup", "");
            Log.i("XATCHUP", cp);
            Intent intent;
            Context context = this;
            intent = new Intent(context, AyahList.class);
            intent.putExtra("SURANAME",cp);
            context.startActivity(intent);
        } else {
            Toast.makeText(getBaseContext(), "No bookmarks found", Toast.LENGTH_LONG).show();
        }


    }

    public void openSuraNames(View view) {

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

    public void openFavourites() {
        Intent intent = new Intent(this, Favourites.class);
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

            if (data != null) {

                // Fragment fragment = new NotificationActivity();
                //getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();

            }


        }
    }
}
