package furqon.io.github.mobilproject;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public class AyahList extends AppCompatActivity {
    private AyahListAdapter mAdapter;
    private DatabaseAccess mDatabase;
    private Cursor ayahcursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_view);

        Bundle intent = getIntent().getExtras();
        String extratext = intent.getString("SURANAME");

        String suranomi = extratext.substring(0, extratext.indexOf(":"));
        String suranomer = extratext.substring(extratext.indexOf(":")+1);

        Log.i("LOADED SURA", suranomer + " " + suranomi);

        getSupportActionBar().setTitle(suranomi);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = DatabaseAccess.getInstance(getApplicationContext());


        final RecyclerView recyclerView = findViewById(R.id.chapter_scroll);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatabase.open();

        ayahcursor = mDatabase.getSuraText(suranomer);

        mAdapter = new AyahListAdapter(this, ayahcursor);
        recyclerView.setAdapter(mAdapter);

    }
}
