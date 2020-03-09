package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class AyahOfTheDay extends AppCompatActivity {
    private static final String TAG = "RANDOM VERSE";
    private int random_surah;
    private int random_ayah;
    private int cursor_retry = 0;

    private String suraname;
    private int language_id = 4;
    //public DatabaseAccess mDatabase;
    //Cursor ayahcursor;
    TitleViewModel viewModel;
    List<RandomSurah> randomSurahs;
    List<AllTranslations> allTranslationsList;
    TextView ayah_reference;
    TextView ayah_text;


    ImageButton prev_btn;
    ImageButton next_btn;
    ImageButton share_btn;
    ImageButton home_btn;
    ImageButton find_btn;
    ImageButton fav_btn;
    ProgressBar pbar;
    FloatingActionButton fab;
    TextView uztxt;
    TextView rutxt;
    TextView entxt;
    boolean isOpen = false;
    Animation fabopen, fabclose, scaler;
    private sharedpref sharedPref;

    public AyahOfTheDay() {
        sharedPref = sharedpref.getInstance();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayah_of_the_day);


        //mDatabase = DatabaseAccess.getInstance(getApplicationContext());
//        if (!mDatabase.isOpen()) {
//            mDatabase.open();
//        }
        viewModel = ViewModelProviders.of(this).get(TitleViewModel.class);
        viewModel.getRandomSurah().observe(this, new Observer<List<RandomSurah>>() {
            @Override
            public void onChanged(List<RandomSurah> randomSurah) {
                randomSurahs = randomSurah;
                displayAyah();
            }
        });


        pbar = findViewById(R.id.progBar);
        uztxt = findViewById(R.id.uztxt);
        rutxt = findViewById(R.id.rutxt);
        entxt = findViewById(R.id.entxt);
        entxt = findViewById(R.id.entxt);
        prev_btn = findViewById(R.id.previous_verse);
        next_btn = findViewById(R.id.next_verse);
        share_btn = findViewById(R.id.random_share);
        home_btn = findViewById(R.id.random_home);
        find_btn = findViewById(R.id.random_find);
        fav_btn = findViewById(R.id.random_favourite);

        fabopen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabclose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        scaler = AnimationUtils.loadAnimation(this, R.anim.bounce);

        ayah_reference = findViewById(R.id.random_verse_title);
        ayah_text = findViewById(R.id.random_verse_text);
        fab = findViewById(R.id.fab);

        pbar.setVisibility(View.INVISIBLE);
        //DONE upo create choose a random sura and ayah


        //get a random verse within the range available in that sura


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.startAnimation(scaler);

                animateFabs();
                Log.i(TAG, " random number");
            }
        });

        uztxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, " UZBEK LANGUAGE");
                snackbarMessage(view, getString(R.string.language_selected));
                animateFabs();
                language_id = 4;
                sharedPref.write("r_language_id", language_id);
                ShowTheAyahBeside();
            }

        });
        rutxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbarMessage(view, getString(R.string.language_selected));
                animateFabs();
                language_id = 5;
                sharedPref.write("r_language_id", language_id);
                ShowTheAyahBeside();
            }
        });
        entxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbarMessage(view, getString(R.string.language_selected));
                animateFabs();
                language_id = 6;
                sharedPref.write("r_language_id", language_id);
                ShowTheAyahBeside();
            }
        });
        sharedPref.init(this);

        if (sharedPref.contains("r_language_id")) {
            language_id = sharedPref.read("r_language_id", 1);
        } else {
            sharedPref.write("r_language_id", language_id);
        }





        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (random_ayah > 1) {
                    random_ayah--;
                    prev_btn.startAnimation(scaler);
                    ShowTheAyahBeside();
                }
            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (random_ayah < QuranMap.AYAHCOUNT[random_surah-1]) {
                    random_ayah++;
                    next_btn.startAnimation(scaler);
                    ShowTheAyahBeside();
                }
            }
        });
        //TODO SHARE
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share_btn.startAnimation(scaler);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, ayah_text.getText() + "\n(" + suraname + ", " + random_ayah + ")\nhttps://goo.gl/sXBkNt\nFurqon, Android\n(" + getApplicationContext().getResources().getText(R.string.shareayah) + ")");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getApplicationContext().getResources().getText(R.string.shareayah)));
            }
        });
        //TODO Home
        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                home_btn.startAnimation(scaler);
                AyahOfTheDay.super.onBackPressed();
            }
        });
        //TODO Bookmark
        find_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                find_btn.startAnimation(scaler);
                sharedPref.write("xatchup" + suraname, random_ayah);
                sharedPref.write("xatchup", suraname + ":" + random_surah);
                continueReading();
            }
        });
        //TODO Fav
        fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fav_btn.startAnimation(scaler);
                addToFavourites(view);
            }
        });
        //makeCall();
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        if(appLinkData!=null) {
            Log.d(TAG, appLinkAction + " - " + appLinkData.getQueryParameter("sn") + appLinkData.getQueryParameter("an"));
            String sntext = appLinkData.getQueryParameter("sn");
            if(sntext!=null)
                random_surah = Integer.parseInt(sntext);
            String antext = appLinkData.getQueryParameter("an");
            if(antext!=null)
                random_ayah = Integer.parseInt(antext);
            //makeCall();
        }
    }

    private int AnAvailableSurahID() {
        int randomSurahNumber;
        if(randomSurahs!=null){
            //get the size of the list.
            int rslen = randomSurahs.size();//value between 1 and 114 inclusive
            randomSurahNumber = (int) Math.floor(Math.random()*rslen);//get a random value of between 0-113
            Log.i("RANDOM SURAH LIST", randomSurahNumber + ", " + randomSurahs.get(randomSurahNumber).sura_id);
            return randomSurahs.get(randomSurahNumber).sura_id;//the actual surah id based on the random number generated above
        }
        else{
            //if the database is empty, return 0
            return 0;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        //mDatabase.close();
    }

    private void continueReading() {
        if (sharedPref.contains(sharedPref.XATCHUP)) {
            String xatchup = sharedPref.read(sharedPref.XATCHUP, "");
            if (xatchup.length() > 0) {
                Intent intent;
                Context context = this;
                intent = new Intent(context, AyahList.class);
                intent.putExtra("SURANAME", xatchup);
                context.startActivity(intent);
            }
        } else {
            Toast.makeText(this, getString(R.string.no_bookmarks), Toast.LENGTH_LONG).show();
        }

    }

    private void addToFavourites(View view) {
        // manage sqlite creation and data addition
        Log.i("AYAT FAVOURITED", String.valueOf(view));


                if (fav_btn.getTag() == "1") {
                    //mDatabase.removeFromFavs(random_surah, random_ayah, "0");
                    fav_btn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    fav_btn.setTag("0");
                } else {
                    //mDatabase.saveToFavs(random_surah, random_ayah, "1");
                    fav_btn.setImageResource(R.drawable.ic_favorite_black_24dp);
                    fav_btn.setTag("1");
                }




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (ayahcursor != null) {
//            ayahcursor.close();
//        }
    }

    private void snackbarMessage(View view, String s) {
        Snackbar.make(view, s, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void ShowTheAyahBeside() {
        String randomayahreference = getString(R.string.surah) + " " + random_surah + " " + suraname + getString(R.string.ayah) + random_ayah;
        ayah_reference.setText(randomayahreference);
        ayah_text.setText(getTextByLanguage());
    }

    private String getTextByLanguage() {
        String ltext;
        switch (language_id){
            case 5:
                ltext = allTranslationsList.get(random_ayah-1).ru_text;
                break;
            case 6:
                ltext = allTranslationsList.get(random_ayah-1).en_text;
                break;
            default:
                ltext = allTranslationsList.get(random_ayah-1).uz_text;
                break;
        }
        return ltext;
    }

    private void displayAyah() {
        random_surah = AnAvailableSurahID();
        if(random_surah==0){
            //database is empty. quit
            ayah_text.setText(R.string.chapters_not_available);
            return;
        }

        random_ayah = (int) Math.round(Math.random() * QuranMap.AYAHCOUNT[random_surah-1]);
        Log.d("RANDOM SURAH MAX AYAH", random_surah + " is surah " + QuranMap.AYAHCOUNT[random_surah-1]);
        viewModel.getChapterText(String.valueOf(random_surah)).observe(this, new Observer<List<AllTranslations>>() {
            @Override
            public void onChanged(List<AllTranslations> allTranslations) {
                allTranslationsList = allTranslations;
                ShowRandomAyah(allTranslations);
            }
        });

    }

    private void ShowRandomAyah(List<AllTranslations> allTranslations) {


        Log.d("RANDOM SURAH AND AYAH", random_surah + " is surah " + random_ayah);
        try{
            suraname = QuranMap.SURAHNAMES[random_surah-1];//DONE fix it to the actual suraname
            String randomayahreference = getString(R.string.surah) + " " + random_surah + " " + suraname + getString(R.string.ayah) + random_ayah;
            ayah_reference.setText(randomayahreference);
            ayah_text.setText(getTextByLanguage());

            int is_fav = allTranslations.get(random_ayah-1).favourite;
            Log.d(TAG, is_fav + " is fav");
            if(is_fav!=0){
                fav_btn.setImageResource(R.drawable.ic_favorite_black_24dp);
                fav_btn.setTag("1");
            } else {
                fav_btn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                fav_btn.setTag("0");
            }

            Log.d(TAG, suraname + "-" + random_surah + " " + random_ayah);

        }catch (IndexOutOfBoundsException iobx) {
            ayah_text.setText(R.string.failed_to_load_ayah);
        }catch (Exception x){
            Toast.makeText(this, R.string.failure_generic, Toast.LENGTH_SHORT).show();
        }






    }

    private void animateFabs() {
        if (isOpen) {

            uztxt.startAnimation(fabclose);
            rutxt.startAnimation(fabclose);
            entxt.startAnimation(fabclose);

            uztxt.setClickable(false);
            rutxt.setClickable(false);
            entxt.setClickable(false);
            isOpen = false;
        } else {
            uztxt.startAnimation(fabopen);
            rutxt.startAnimation(fabopen);
            entxt.startAnimation(fabopen);

            uztxt.setClickable(true);
            rutxt.setClickable(true);
            entxt.setClickable(true);

            isOpen = true;

        }
    }
}
