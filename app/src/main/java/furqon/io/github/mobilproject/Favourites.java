package furqon.io.github.mobilproject;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class Favourites extends AppCompatActivity {

    private ProgressBar progressBar;
    private DatabaseAccess mDatabase;
    private RecyclerView recyclerView;
    private FavouriteListAdapter mAdapter;
    String suranomer;
    public String suranomi;
    private Cursor cursor;


    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Favorites");

        progressBar = findViewById(R.id.progressBar2);
        recyclerView = findViewById(R.id.recyclerfavs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatabase = DatabaseAccess.getInstance(getApplicationContext());
        mDatabase.openWrite();


        MobileAds.initialize(this, "ca-app-pub-3838820812386239~2342916878");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        Log.i("FAVOURITES","loaded");

        loadFavourites();


    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settings_i:
                open_settings();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInterstitialAd.show();
        mDatabase.close();
    }

    private void loadFavourites(){
        cursor = mDatabase.loadFavourites();
        mAdapter = new FavouriteListAdapter(this, cursor, suranomi, suranomer);
        recyclerView.setAdapter(mAdapter);


        progressBar.setVisibility(View.INVISIBLE);
    }
}
