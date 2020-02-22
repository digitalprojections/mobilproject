package furqon.io.github.mobilproject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.Objects;


public class Favourites extends AppCompatActivity {

    private ProgressBar progressBar;
    private DatabaseAccess mDatabase;
    private RecyclerView recyclerView;
    private FavouriteListAdapter mAdapter;
    String suranomer;
    public String suranomi;
    private Cursor cursor;
    private AdView mAdView;
    private static final String TAG = "FAVOURITES ACTIVITY";
    private static final int REQUEST_INVITE = 0;
    private InterstitialAd mInterstitialAd;
    private sharedpref sharedPref;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favourites);
        sharedPref = sharedpref.getInstance();
        sharedPref.init(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Favorites");

        progressBar = findViewById(R.id.progressBar2);
        recyclerView = findViewById(R.id.recyclerfavs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatabase = DatabaseAccess.getInstance(getApplicationContext());
        if(!mDatabase.isOpen()) {
            mDatabase.open();
        }

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        MobileAds.initialize(this, getString(R.string.app_id_mobileinitialize));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.AdUnitId_fullpage));
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
            case R.id.invite_i:

                return true;
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

    public void loadFavourites(){
        cursor = mDatabase.loadFavourites();
        mAdapter = new FavouriteListAdapter(this, cursor, suranomi, suranomer);
        recyclerView.setAdapter(mAdapter);


        progressBar.setVisibility(View.INVISIBLE);
    }
}
