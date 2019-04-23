package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

import com.google.android.material.snackbar.Snackbar;

public class Search extends AppCompatActivity {
    private static final String TAG = "SEARCH";
    private SearchListAdapter mAdapter;
    public DatabaseAccess mDatabase;
    Cursor ayahcursor;
    String searchtxt;
    RecyclerView recyclerView;
    static ProgressBar progressBar;
    EditText search_txt;
    static ImageButton ib_search;
    static TextView result_count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        SharedPref.init(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.search);
        getSupportActionBar().setTitle(title);

        progressBar = findViewById(R.id.progressBarSearch);
        progressBar.setVisibility(View.INVISIBLE);
        search_txt = findViewById(R.id.editTextSearch);
        ib_search = findViewById(R.id.imageButtonSearch);
        result_count = findViewById(R.id.result_count_txt);
        result_count.setText("search result");
        recyclerView = findViewById(R.id.recyclerSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatabase = DatabaseAccess.getInstance(getApplicationContext());
        if (!mDatabase.isOpen()) {
            mDatabase.open();
        }


        ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, String.valueOf(search_txt.getText()));
                if (String.valueOf(search_txt.getText()) != "") {
                    search_word(getApplicationContext(), String.valueOf(search_txt.getText()));
                }
            }
        });
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
            ayahcursor = mDatabase.searchText(word);

                setProgressBarState(0);


            mAdapter = new SearchListAdapter(context, ayahcursor);
            recyclerView.setAdapter(mAdapter);
            setProgressBarState(ayahcursor.getCount());
        }else{
            Toast.makeText(context, "Try a longer word", Toast.LENGTH_SHORT);
        }
    }

    public static void setProgressBarState(int c) {
        ib_search.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        result_count.setText("search result: " + c);
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

}
