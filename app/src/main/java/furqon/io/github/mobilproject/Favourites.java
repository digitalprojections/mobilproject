package furqon.io.github.mobilproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.List;


public class Favourites extends AppCompatActivity implements ManageSpecials {

    private ProgressBar progressBar;
    //private DatabaseAccess mDatabase;

    private TitleViewModel viewModel;
    private RecyclerView recyclerView;
    FavouriteListAdapter mAdapter;
    String suranomer;
    public String suranomi;
    //private Cursor cursor;
    AdView mAdView;
    private static final String TAG = "FAVOURITES ACTIVITY";
    private static final int REQUEST_INVITE = 0;
    private InterstitialAd mInterstitialAd;
    SharedPreferences sharedPref;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favourites);
        viewModel = ViewModelProviders.of(this).get(TitleViewModel.class);



        sharedPref = SharedPreferences.getInstance();
        sharedPref.init(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.favorites);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        progressBar = findViewById(R.id.progressBar2);
        recyclerView = findViewById(R.id.recyclerfavs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        MobileAds.initialize(this, getString(R.string.addmob_app_id));
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        if (BuildConfig.BUILD_TYPE.equals("debug"))
            Log.i("FAVOURITES","loaded");



        mAdapter = new FavouriteListAdapter(this);
        recyclerView.setAdapter(mAdapter);
        viewModel.getFavourites().observe(this, new Observer<List<FavouriteAyah>>() {
            @Override
            public void onChanged(@Nullable List<FavouriteAyah> favouriteAyahs) {
                if (BuildConfig.BUILD_TYPE.equals("debug"))
                    Log.e("FAVOURITES", favouriteAyahs.size() + " ");
                if(favouriteAyahs.size()>0){
                    mAdapter.setText(favouriteAyahs);
                    recyclerView.scheduleLayoutAnimation();
                }

            }
        });
        loadFavourites();



    }

    /*public boolean onCreateOptionsMenu(Menu menu) {
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
*/
    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private void open_settings(){
        Intent intent;
        intent = new Intent(this, furqon.io.github.mobilproject.Settings.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!SharedPreferences.getInstance().read(SharedPreferences.NOMOREADS, false))
            mInterstitialAd.show();
        //mDatabase.close();
    }

    public void loadFavourites(){
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void UpdateSpecialItem(ChapterTextTable text) {
        viewModel.updateText(text);
    }
}
