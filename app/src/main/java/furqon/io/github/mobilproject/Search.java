package furqon.io.github.mobilproject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Objects;

public class Search extends AppCompatActivity {
    private static final String TAG = "SEARCH";
    private SearchListAdapter mAdapter;
    //public DatabaseAccess mDatabase;
    //Cursor ayahcursor;
    String searchtxt;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    EditText search_txt;
    ImageButton ib_search;
    TextView result_count;
    private sharedpref sharedPref;
    InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        sharedPref = sharedpref.getInstance();
        sharedPref.init(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.search);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBarSearch);
        progressBar.setVisibility(View.INVISIBLE);
        search_txt = findViewById(R.id.editTextSearch);
        ib_search = findViewById(R.id.imageButtonSearch);
        result_count = findViewById(R.id.result_count_txt);
        result_count.setText(R.string.found);
        recyclerView = findViewById(R.id.recyclerSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //TODO Serch functionality missing
//        mDatabase = DatabaseAccess.getInstance(getApplicationContext());
//        if (!mDatabase.isOpen()) {
//            mDatabase.open();
//        }


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
        mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_i:
                //shareDeepLink(deepLink.toString());
                return true;
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

    //SEARCH
    private void search_word(Context context, String word) {
        if(word.length()>2) {
            ib_search.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            //ayahcursor = mDatabase.searchText(word);

                setProgressBarState(0);


            //mAdapter = new SearchListAdapter(context, ayahcursor);
            //recyclerView.setAdapter(mAdapter);
            //setProgressBarState(ayahcursor.getCount());
        }else{
            Toast.makeText(context, "Try a longer word", Toast.LENGTH_SHORT).show();
        }
    }

    public void setProgressBarState(int c) {
        ib_search.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        String found_text = String.valueOf(R.string.found + c);
        result_count.setText(found_text);
    }

    private void open_settings() {
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
    protected void onDestroy() {
        super.onDestroy();
        mInterstitialAd.show();
    }
}
