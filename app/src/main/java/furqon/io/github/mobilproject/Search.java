package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.List;
import java.util.Objects;

public class Search extends  AppCompatActivity implements ManageSpecials  {
    public static final String TAG = Search.class.getSimpleName();
    private SearchResultAdapter mAdapter;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private TitleViewModel searchResultViewModel;

    String searchtxt;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    EditText search_txt;
    ImageButton ib_search;
    TextView result_count;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        SharedPreferences sharedPref = SharedPreferences.getInstance();
        sharedPref.init(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.search);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBarSearch);
        progressBar.setVisibility(View.INVISIBLE);
        search_txt = findViewById(R.id.editTextSearch);
        ib_search = findViewById(R.id.imageButtonSearch);
        result_count = findViewById(R.id.result_count_txt);
        result_count.setText(getString(R.string.found));
        recyclerView = findViewById(R.id.recyclerSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SearchResultAdapter(this);
        recyclerView.setAdapter(mAdapter);

        searchResultViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);


        ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, String.valueOf(search_txt.getText()));
                if (!String.valueOf(search_txt.getText()).isEmpty()) {
                    search_word(getApplicationContext(), String.valueOf(search_txt.getText()));
                }
            }
        });
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE == "debug") {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        AdView mAdView = findViewById(R.id.adViewSearch);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    //SEARCH
    private void search_word(Context context, String word) {
        if(word.length()>2) {
            ib_search.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            searchResultViewModel.getSearchResults(word).observe(this, new Observer<List<SearchResult>>() {
                @Override
                public void onChanged(@Nullable List<SearchResult> surahText) {
                    //Toast.makeText(SuraNameList.this, "LOADING TITLES " + surahTitles.size(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "WORDS FOUND IN AYAH " + surahText.size());
                    mAdapter.setResults(surahText);
                    setProgressBarState(surahText.size());
                }
            });
        }else{
            Toast.makeText(context, "Try a longer word", Toast.LENGTH_SHORT).show();
        }
    }

    public void setProgressBarState(int c) {
        ib_search.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        String found_text = getString(R.string.found).concat(String.valueOf(c));
        result_count.setText(found_text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!SharedPreferences.getInstance().read(SharedPreferences.NOMOREADS, false))
            mInterstitialAd.show();
    }

    @Override
    public void UpdateSpecialItem(ChapterTextTable text) {

    }
}
