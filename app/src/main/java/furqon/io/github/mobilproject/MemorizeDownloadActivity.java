package furqon.io.github.mobilproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemorizeDownloadActivity extends AppCompatActivity {

    private String TAG = MemorizeDownloadActivity.class.getSimpleName();

    //TODO UI elements
    RecyclerView recyclerView;
    private TitleViewModel titleViewModel;
    private MemorizeDownloadAdapter mAdapter;
    private Context context;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;


    //TODO DATABASE connection
    boolean download_attempted = false;
    private ArrayList<JSONObject> jsonArrayResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize_download);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);


        recyclerView = findViewById(R.id.verseDownloadList_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MemorizeDownloadAdapter();
        recyclerView.setAdapter(mAdapter);


        LoadTheList();
    }

    private void LoadTheList() {


        titleViewModel.getAllTitles().observe(this, new Observer<List<ChapterTitleTable>>() {
            @Override
            public void onChanged(@Nullable List<ChapterTitleTable> surahTitles) {
                //Toast.makeText(SuraNameList.this, "LOADING TITLES " + surahTitles.size(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "LOADING SURAH NAMES");
                if (surahTitles.size() != 114) {
                    //tempbut.setVisibility(View.VISIBLE);

                    if (!download_attempted) {
                        Log.e(TAG, "LOADING LIST");
                        download_attempted = true;
                        LoadTitles();

                    }

                    //titleViewModel.deleteAll();
                }
                mAdapter.setTitles(surahTitles);
            }
        });
    }

    private void LoadTitles() {
        Log.i(TAG, "LOAD AUDIO CLICKED");
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/ajax_quran.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        // Convert String to json object
                        jsonArrayResponse = new ArrayList<JSONObject>();

                        try {
                            Log.i(TAG, response);
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }
                            //load auction names and available lot/bid count
                            populateSurahTitleList(jsonArrayResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Log.i("error json", "tttttttttttttttt");
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //progressBar.setVisibility(View.INVISIBLE);
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("action", "names_as_objects"); //Add the data you'd like to send to the server.
                MyData.put("language_id", "1");
                //https://inventivesolutionste.ipage.com/ajax_quran.php
                //POST
                //action:names_as_objects
                //language_id:1
                return MyData;
            }
        };
        queue.add(stringRequest);
    }

    void populateSurahTitleList(ArrayList<JSONObject> surahTitleList) {

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(, android.R.layout.simple_spinner_item, auclist);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
        ChapterTitleTable title;

        for (JSONObject i : surahTitleList
        ) {

            try {
                //Log.d(TAG, "JSONOBJECT "+ i.getString("language_no") + i.getString("uzbek"));
                //int language_no = i.getInt("language_no");
                int order_no = i.getInt("order_no");
                int chapter_id = i.getInt("chapter_id");
                String surah_type = i.getString("surah_type");
                String uzbek = i.getString("uzbek");
                String arabic = i.getString("arabic");

                title = new ChapterTitleTable(1, order_no, chapter_id, uzbek, arabic, surah_type);
                titleViewModel.insert(title);


            } catch (Exception sx) {
                Log.e("EXCEPTION", sx.getMessage());
            }
        }
    }
}