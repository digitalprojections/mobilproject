package furqon.io.github.mobilproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class SuraNameList extends AppCompatActivity {

    private SuraNameListAdapter mAdapter;
    private DatabaseAccess mDatabase;
    private Cursor suralist;
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sura_name_list);
        getSupportActionBar().setTitle("Suralar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDatabase = DatabaseAccess.getInstance(getApplicationContext());


        final RecyclerView recyclerView = findViewById(R.id.recyclerview);
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







