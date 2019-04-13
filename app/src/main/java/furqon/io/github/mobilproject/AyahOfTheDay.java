package furqon.io.github.mobilproject;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

public class AyahOfTheDay extends AppCompatActivity {
    private static final String TAG = "RANDOM VERSE";
    private int random_surah;
    public DatabaseAccess mDatabase;
    Cursor ayahcursor;
    ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayah_of_the_day);
        pbar = findViewById(R.id.progBar);
        pbar.setVisibility(View.VISIBLE);
        //TODO upo create choose a random sura
        random_surah = (int) Math.round(Math.random() * 113) + 1;
        //TODO get a random verse within the range available in that sura


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Language Changed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        Log.i(TAG, " random number");
    }
}
