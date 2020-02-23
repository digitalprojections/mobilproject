package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class AyahOfTheDay extends AppCompatActivity {
    private static final String TAG = "RANDOM VERSE";
    private int random_surah;
    private int random_ayah;
    private int cursor_retry = 0;

    private String suraname;
    private int language_id = 4;
    public DatabaseAccess mDatabase;
    Cursor ayahcursor;
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


        mDatabase = DatabaseAccess.getInstance(getApplicationContext());
        if (!mDatabase.isOpen()) {
            mDatabase.open();
        }
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
        random_surah = (int) Math.round(Math.random() * 113)+1;
        random_ayah = (int) Math.round(Math.random() * QuranMap.AYAHCOUNT[random_surah-1]);
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
                displayAyah();
            }

        });
        rutxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbarMessage(view, getString(R.string.language_selected));
                animateFabs();
                language_id = 5;
                sharedPref.write("r_language_id", 5);
                displayAyah();
            }
        });
        entxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbarMessage(view, getString(R.string.language_selected));
                animateFabs();
                language_id = 6;
                sharedPref.write("r_language_id", 6);
                displayAyah();
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
                    makeCall();
                }
            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (random_ayah < QuranMap.AYAHCOUNT[random_surah-1]) {
                    random_ayah++;
                    next_btn.startAnimation(scaler);
                    makeCall();
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
        makeCall();
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
            makeCall();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabase.close();
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

            if (!mDatabase.isOpen()) {
                assert mDatabase!=null;
                mDatabase.open();
                if(mDatabase.isOpen()) {
                    addToFavourites(view);
                }
            } else{
                if (fav_btn.getTag() == "1") {
                    mDatabase.removeFromFavs(random_surah, random_ayah, "0");
                    fav_btn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    fav_btn.setTag("0");
                } else {
                    mDatabase.saveToFavs(random_surah, random_ayah, "1");
                    fav_btn.setImageResource(R.drawable.ic_favorite_black_24dp);
                    fav_btn.setTag("1");
                }

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ayahcursor != null) {
            ayahcursor.close();
        }
    }

    private void snackbarMessage(View view, String s) {
        Snackbar.make(view, s, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void makeCall() {
        if(mDatabase.isOpen()) {

            ayahcursor = mDatabase.getRandomAyah(random_surah, random_ayah);
            if (ayahcursor != null) {
                ayahcursor.moveToPosition(0);
                displayAyah();
            }
        }
    }

    private void displayAyah() {
            ShowRandomAyah();
    }

    private void ShowRandomAyah() {
        if (ayahcursor != null) {
            try{
                suraname = ayahcursor.getString(2);
                String randomayahreference = getString(R.string.surah) + suraname + getString(R.string.ayah) + random_ayah;
                ayah_reference.setText(randomayahreference);
                ayah_text.setText(ayahcursor.getString(language_id));

                String is_fav = ayahcursor.getString(7);
                Log.d(TAG, is_fav + " is fav");
                if(is_fav!=null){
                    if (is_fav.equals("1")) {
                        fav_btn.setImageResource(R.drawable.ic_favorite_black_24dp);
                        fav_btn.setTag("1");
                    } else {
                        fav_btn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        fav_btn.setTag("0");
                    }
                }

                Log.d(TAG, suraname + "-" + random_surah + " " + random_ayah);
            }catch (CursorIndexOutOfBoundsException ciobx){
                ++cursor_retry;
                if(cursor_retry<3){
                    //try recalling the cursor
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    makeCall();
                }
            }

        }
        else{
            ++cursor_retry;
            if(cursor_retry<3){
                //try recalling the cursor
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                makeCall();
            }
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
