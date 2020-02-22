package furqon.io.github.mobilproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class Settings extends AppCompatActivity {

    private AdView mAdView;
    private sharedpref sharedPref;
    InterstitialAd mInterstitialAd;


    private Switch sw_ar;
    private Switch sw_uz;
    private Switch sw_ru;
    private Switch sw_en;
    private Button ok_but;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPref = sharedpref.getInstance();
        sharedPref.init(getApplicationContext());
        sw_ar = findViewById(R.id.arabic_sw);
        sw_uz = findViewById(R.id.uzbek_sw);
        sw_ru = findViewById(R.id.ru_sw);
        sw_en = findViewById(R.id.en_sw);
        ok_but = findViewById(R.id.done_setting);

        ok_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_settings();
            }
        });


        MobileAds.initialize(this, "ca-app-pub-3838820812386239~2342916878");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        updateView();

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    public void save_settings() {

        sharedPref.write(sharedPref.ARSW, sw_ar.isChecked());
        sharedPref.write(sharedPref.UZSW, sw_uz.isChecked());
        sharedPref.write(sharedPref.RUSW, sw_ru.isChecked());
        sharedPref.write(sharedPref.ENSW, sw_en.isChecked());
        Toast.makeText(this, "Setting are saved", Toast.LENGTH_SHORT).show();
        sharedPref.init(getApplicationContext());
    }


    public void updateView() {
        sw_ar.setChecked(sharedPref.read(sharedPref.ARSW, false));
        sw_uz.setChecked(sharedPref.read(sharedPref.UZSW, false));
        sw_ru.setChecked(sharedPref.read(sharedPref.RUSW, false));
        sw_en.setChecked(sharedPref.read(sharedPref.ENSW, false));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInterstitialAd.show();
    }
}
