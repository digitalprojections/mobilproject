package furqon.io.github.mobilproject;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class SuraNameList extends AppCompatActivity {

    private SuraNameListAdapter mAdapter;
    private DatabaseAccess mDatabase;
    private Cursor suralist;
    private AdView mAdView;
    RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sura_name_list);
        getSupportActionBar().setTitle("Suralar");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDatabase = DatabaseAccess.getInstance(getApplicationContext());


        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatabase.open();

        suralist = mDatabase.getSuraTitles();

        mAdapter = new SuraNameListAdapter(this, suralist);
        recyclerView.setAdapter(mAdapter);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    }







