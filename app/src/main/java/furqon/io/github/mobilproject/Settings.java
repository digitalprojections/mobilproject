package furqon.io.github.mobilproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class Settings extends AppCompatActivity {

    private AdView mAdView;
    private SharedPreferences sharedPref;
    InterstitialAd mInterstitialAd;


    private Switch sw_ar;
    private Switch sw_uz;
    private Switch sw_ru;
    private Switch sw_en;

    private Switch sw_random_ayah;
    private Button ok_but;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPref = SharedPreferences.getInstance();
        sharedPref.init(getApplicationContext());
        sw_ar = findViewById(R.id.arabic_sw);
        sw_uz = findViewById(R.id.uzbek_sw);
        sw_ru = findViewById(R.id.ru_sw);
        sw_en = findViewById(R.id.en_sw);
        sw_random_ayah = findViewById(R.id.random_ayah_switch);
        ok_but = findViewById(R.id.ok_button);

        sw_ar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPref.write(sharedPref.ARSW, sw_ar.isChecked());
                ok_but.setEnabled(true);
            }
        });
        sw_en.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPref.write(sharedPref.ENSW, sw_en.isChecked());
                ok_but.setEnabled(true);
            }
        });
        sw_ru.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPref.write(sharedPref.RUSW, sw_ru.isChecked());
                ok_but.setEnabled(true);
            }
        });
        sw_uz.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPref.write(sharedPref.UZSW, sw_uz.isChecked());
                ok_but.setEnabled(true);
            }
        });

        sw_random_ayah.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPref.write(sharedPref.RANDOMAYAHSW, sw_random_ayah.isChecked());
                ok_but.setEnabled(true);
            }
        });

        ok_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.super.onBackPressed();
                Toast.makeText(view.getContext(), getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
                sharedPref.init(getApplicationContext());
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



    public void updateView() {
        sw_ar.setChecked(sharedPref.read(sharedPref.ARSW, false));
        sw_uz.setChecked(sharedPref.read(sharedPref.UZSW, false));
        sw_ru.setChecked(sharedPref.read(sharedPref.RUSW, false));
        sw_en.setChecked(sharedPref.read(sharedPref.ENSW, false));
        sw_random_ayah.setChecked(sharedPref.read(sharedPref.RANDOMAYAHSW, true));
    }

    @Override
    protected void onResume(){
        super.onResume();
        ok_but.setEnabled(false);
    }
    @Override
    protected void  onPause(){
        super.onPause();
        ok_but.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInterstitialAd.show();
    }
}
