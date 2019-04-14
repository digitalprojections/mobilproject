package furqon.io.github.mobilproject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SuraNameList extends AppCompatActivity {

    private SuraNameListAdapter mAdapter;
    private DatabaseAccess mDatabase;
    private Cursor suralist;

    RecyclerView recyclerView;
    InterstitialAd mInterstitialAd;


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settings_i:
                open_settings();
                return true;
            case R.id.favourites_i:
                open_favourites();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void open_settings(){
        Intent intent;
        intent = new Intent(this, furqon.io.github.mobilproject.Settings.class);
        startActivity(intent);
    }
    private void open_favourites() {
        Intent intent;
        intent = new Intent(this, furqon.io.github.mobilproject.Favourites.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sura_name_list);
        SharedPref.init(getApplicationContext());
        //getSupportActionBar().setTitle("Suralar");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);


        mDatabase = DatabaseAccess.getInstance(getApplicationContext());
        if(!mDatabase.isOpen()) {
            mDatabase.open();
        }

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        suralist = mDatabase.getSuraTitles();




        mAdapter = new SuraNameListAdapter(this, suralist);
        recyclerView.setAdapter(mAdapter);



        MobileAds.initialize(this, "ca-app-pub-3838820812386239~2342916878");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInterstitialAd.show();
        if(mDatabase!=null) {
            mDatabase.close();
        }
    }
}