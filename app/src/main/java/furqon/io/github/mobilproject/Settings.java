package furqon.io.github.mobilproject;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;

public class Settings extends AppCompatActivity {

    private AdView mAdView;
    private SharedPreferences sharedPref;
    InterstitialAd mInterstitialAd;


    private Switch sw_ar;
    private Switch sw_uz;
    private Switch sw_ru;
    private Switch sw_en;

    private RadioButton madina_font;
    private RadioButton usmani_font;
    private RadioButton qalam_font;

    private ImageButton font_up;
    private ImageButton font_down;

    private TextView sampletext;
    private Typeface font;

    private Switch sw_random_ayah;
    private Button ok_but;
    private String TAG = "SETTINGS";

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

        font_up = findViewById(R.id.fontsize_up);
        font_down = findViewById(R.id.fontsize_down);

        font_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                float fs = sampletext.getTextSize();
                fs = fs + 1.0f;
                sampletext.setTextSize(TypedValue.COMPLEX_UNIT_PX, fs);
                //sharedPref.write(SharedPreferences.FONTSIZE, fsout);
                ok_but.setEnabled(true);
            }
        });
        font_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float fs = sampletext.getTextSize();
                Log.e(TAG, fs + " ");
                fs = fs - 1.0f;
                Log.e(TAG, fs + " ");
                sampletext.setTextSize(TypedValue.COMPLEX_UNIT_PX, fs);
                Log.e(TAG, sampletext.getTextSize() + "");
                //sharedPref.write(SharedPreferences.FONTSIZE, fsout);
                ok_but.setEnabled(true);
            }
        });

        sampletext = findViewById(R.id.st_sample_text);
        madina_font = findViewById(R.id.madina_rb);
        usmani_font = findViewById(R.id.usmani_rb);
        qalam_font = findViewById(R.id.qalam_rb);

        madina_font.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPref.write(sharedPref.FONT, "madina");
                    font = ResourcesCompat.getFont(Settings.this, R.font.maddina);
                    sampletext.setTypeface(font);
                    ok_but.setEnabled(true);
                }
            }
        });
        usmani_font.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPref.write(SharedPreferences.FONT, "usmani");
                    font = ResourcesCompat.getFont(Settings.this, R.font.al_uthmani);
                    sampletext.setTypeface(font);
                    ok_but.setEnabled(true);
                }

            }
        });
        qalam_font.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPref.write(SharedPreferences.FONT, "qalam");
                    font = ResourcesCompat.getFont(Settings.this, R.font.al_qalam);
                    sampletext.setTypeface(font);
                    ok_but.setEnabled(true);
                }
            }
        });


        sampletext.setText(R.string.basmala);

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
                Snackbar.make(findViewById(R.id.linearLayout2), getString(R.string.settings_saved), Snackbar.LENGTH_SHORT).show();
                sharedPref.init(getApplicationContext());
                //set font size
                float fs = sampletext.getTextSize();
                int fsout = Math.round(fs);
                sharedPref.write(SharedPreferences.FONTSIZE, fsout);
            }
        });

        MobileAds.initialize(this, "ca-app-pub-3838820812386239~2342916878");
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE == "debug") {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
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
        if (sharedPref.contains(sharedPref.FONT)) {
            switch (sharedPref.read(sharedPref.FONT, "")) {
                case "madina":
                    madina_font.setChecked(true);
                    break;
                case "usmani":
                    usmani_font.setChecked(true);
                    break;
                case "qalam":
                    qalam_font.setChecked(true);
                    break;
                default:
                    qalam_font.setChecked(true);
                    break;
            }
        } else {
            qalam_font.setChecked(true);
        }

        if (sharedPref.contains(sharedPref.FONTSIZE)) {
            float fs = (float) sharedPref.read(sharedPref.FONTSIZE, 0);
            sampletext.setTextSize(TypedValue.COMPLEX_UNIT_PX, fs);
        }
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
