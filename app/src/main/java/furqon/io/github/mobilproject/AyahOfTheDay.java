package furqon.io.github.mobilproject;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

public class AyahOfTheDay extends AppCompatActivity {
    private static final String TAG = "RANDOM VERSE";
    private int random_surah;
    private int random_ayah;
    public DatabaseAccess mDatabase;
    Cursor ayahcursor;
    ProgressBar pbar;
    FloatingActionButton fab;
    TextView uztxt;
    TextView rutxt;
    TextView entxt;
    boolean isOpen = false;
    Animation fabopen, fabclose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayah_of_the_day);
        pbar = findViewById(R.id.progBar);
        uztxt = findViewById(R.id.uztxt);
        rutxt = findViewById(R.id.rutxt);
        entxt = findViewById(R.id.entxt);
        fabopen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabclose = AnimationUtils.loadAnimation(this, R.anim.fab_close);



        pbar.setVisibility(View.VISIBLE);
        //TODO upo create choose a random sura and ayah
        random_surah = (int) Math.round(Math.random() * 113);
        random_ayah = QuranMap.AYAHCOUNT[random_surah];
        //TODO get a random verse within the range available in that sura


        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Language Changed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if(isOpen){

                    uztxt.startAnimation(fabclose);
                    rutxt.startAnimation(fabclose);
                    entxt.startAnimation(fabclose);

                    uztxt.setClickable(false);
                    rutxt.setClickable(false);
                    entxt.setClickable(false);
                    isOpen = false;
                }else{
                    uztxt.startAnimation(fabopen);
                    rutxt.startAnimation(fabopen);
                    entxt.startAnimation(fabopen);

                    uztxt.setClickable(true);
                    rutxt.setClickable(true);
                    entxt.setClickable(true);

                    isOpen = true;

                }

                Log.i(TAG,   " random number");
            }
        });



    }
}
