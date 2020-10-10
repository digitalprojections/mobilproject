package furqon.io.github.mobilproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.ResourceBundle;

public class MemorizeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final String TAG = MemorizeActivity.class.getSimpleName();

    //DEFINE UI ELEMENTS
    private Spinner suranames_spinner;
    private ImageButton playVerse;
    private ImageButton decRepeat;
    private ImageButton incRepeat;
    private ImageButton decStart;
    private ImageButton incStart;
    private ImageButton decEnd;
    private ImageButton incEnd;
    private Button commitBtn;
    private TextView startValue;
    private TextView endValue;
    private TextView repeatValue;
    private RecyclerView recyclerView;

    //DATA
    private TitleViewModel ayahViewModel;
    private ArrayList<Track> trackList;
    private ArrayAdapter<CharSequence> language_adapter;
    private MediaPlayer mediaPlayer;
    private ArrayList<JSONObject> jsonArrayResponse;

    private MemorizeActivityAdapter adapter;
    private Integer lastSurah = 0;

    SharedPreferences sharedPreferences;
    private String rct;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Context context;
    private String suraNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize);
        sharedPreferences = SharedPreferences.getInstance();
        //DONE restore the last state
        //There was a surah selected
        if(sharedPreferences.contains(SharedPreferences.SELECTED_MEMORIZING_SURAH)){
            lastSurah = sharedPreferences.read(SharedPreferences.SELECTED_MEMORIZING_SURAH, 0);
        }
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.memorizer);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        ayahViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);

        //INITIALIZE UI ELEMENTS
        suranames_spinner = findViewById(R.id.surah_spinner);
        playVerse = findViewById(R.id.play_verse);
        decRepeat = findViewById(R.id.dec_repeat);
        incRepeat = findViewById(R.id.inc_repeat);

        incStart = findViewById(R.id.inc_start);
        decStart = findViewById(R.id.dec_start);
        incEnd = findViewById(R.id.inc_end);
        decEnd = findViewById(R.id.dec_end);

        commitBtn = findViewById(R.id.commit_btn);

        startValue = findViewById(R.id.start_tv);
        endValue = findViewById(R.id.end_tv);
        repeatValue = findViewById(R.id.repeat_count_tv);

        recyclerView = findViewById(R.id.memorize_range_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MemorizeActivityAdapter(this);

        recyclerView.setAdapter(adapter);



        //UI ACTION
        playVerse.setOnClickListener(this);
        decRepeat.setOnClickListener(this);
        incRepeat.setOnClickListener(this);
        decStart.setOnClickListener(this);
        incStart.setOnClickListener(this);
        decEnd.setOnClickListener(this);
        incEnd.setOnClickListener(this);
        commitBtn.setOnClickListener(this);

        suranames_spinner.setOnItemSelectedListener(this);

        /*todo end number never lower than the start
           if start number entered and it is higher than the end number, set the end number
            equal to the start number. But if the start number is changed to a lower value,
            reset the end number back to what it was before
         */
        //todo
    }
    @Override
    public void onClick(View v) {
        //TODO make an HTTP request to load the matching ayats
        //show the selected range
        //TODO
        //todo "memorize" button action
        switch (v.getId()){
            case R.id.dec_start:
                adjustHighLowStart(-1);
                break;
            case R.id.inc_start:
                adjustHighLowStart(1);
                break;
            case R.id.dec_end:
                adjustHighLowEnd(-1);
                break;
            case R.id.inc_end:
                adjustHighLowEnd(1);
                break;
            case R.id.dec_repeat:
                adjustRepeat(-1);
                break;
            case R.id.inc_repeat:
                adjustRepeat(1);
                break;
            case R.id.commit_btn:
                loadRange();
                break;
        }
    }

    private void loadRange() {
        ayahViewModel.getAyahRange(suraNumber, "1", "3").observe(this, new Observer<List<AyahRange>>() {
            @Override
            public void onChanged(List<AyahRange> ayahRanges) {
                //TODO display the range
                //send to the adapter
                adapter.setText(ayahRanges);
                Log.d(TAG, "ADAPTER " + ayahRanges.size());

                if(ayahRanges.size()==0){
                    //The list is empty. DOWNLOAD
                    LoadSurah();
                }
            }
        });
    }
    private void LoadSurah() {

        Log.i(TAG, "CLICK clicking");
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = mFirebaseRemoteConfig.getString("server_php") + "/ajax_quran.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //progressBar.setVisibility(View.INVISIBLE);
                        // Convert String to json object
                        //httpresponse = true;
                        jsonArrayResponse = new ArrayList<JSONObject>();

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i=0; i<jsonArray.length();i++)
                            {
                                JSONObject object = new JSONObject(jsonArray.getString(i));
                                jsonArrayResponse.add(object);
                            }
                            //PASS to SPINNER
                            //load auction names and available lot/bid count
                            populateAyahList(jsonArrayResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(TAG, "error json ttttttttttttttttt");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "ERROR RESPONSE enable reload button");
                //progressBar.setVisibility(View.INVISIBLE);
                //tempbut.setVisibility(View.VISIBLE);

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("action", "izohsiz_text_obj"); //Add the data you'd like to send to the server.
                MyData.put("database_id", "1, 120, 59, 79");
                MyData.put("surah_id", suraNumber);
                //https://inventivesolutionste.ipage.com/ajax_quran.php
                //POST
                //action:names_as_objects
                //language_id:1
                return MyData;
            }
        };
        tempbut.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        queue.add(stringRequest);
    }
    void populateAyahList(ArrayList<JSONObject> auclist){

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(, android.R.layout.simple_spinner_item, auclist);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
        ChapterTextTable text;

        for (JSONObject i:auclist
        ) {

            try{
                //"ID":"31206","VerseID":"7","AyahText":"صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ","DatabaseID":"1","SuraID":"1","OrderNo":"5","SuraType":"Meccan","Note":null
                //Log.d("JSONOBJECT", i.toString());
                int verse_id = i.getInt("VerseID");
                int DatabaseID = i.getInt("DatabaseID");
                int chapter_id = i.getInt("SuraID");
                int OrderNo = i.getInt("OrderNo");
                String surah_type = i.getString("SuraType");
                String AyahText = i.getString("AyahText");
                //int sura_id, int verse_id, int favourite, int language_id, String ayah_text, String surah_type, int order_no, String comment, int read_count, int shared_count, int audio_position
                text = new ChapterTextTable(chapter_id, verse_id,0, DatabaseID, OrderNo, AyahText, "", surah_type);
                titleViewModel.insertText(text);
            }catch (Exception sx){
                Log.e(TAG, "EXCEPTION " + sx.getMessage());
            }
        }
    }
    private void adjustRepeat(int i) {
        int repeatCount = -1;
        try{
            repeatCount = Integer.parseInt(repeatValue.getText().toString());
        }catch (NumberFormatException nfx){
            //cant parse
        }
        if(repeatCount > -2){
            {
            repeatCount+=i;
            rct = ""+repeatCount;
            repeatValue.setText(rct);

        }
        }
    }

    void adjustHighLowStart(int i)
        {
        //todo don't allow end number to be higher than the start
            int sVal = 0;
            int eVal = 0;
            try {
                sVal = Integer.parseInt(startValue.getText().toString());
                eVal = Integer.parseInt(endValue.getText().toString());
            }catch (NumberFormatException nfx){
                //cant parse
            }
            if(i>0){
                if(sVal<eVal-1){
                    sVal+=i;
                    startValue.setText(""+sVal);
                }else if(sVal==0 && eVal==0){
                    sVal+=i;
                    startValue.setText(""+sVal);
                    eVal+=i+1;
                    endValue.setText(""+eVal);
                }
            }else{
                if(sVal>1){
                    sVal--;
                    startValue.setText(""+sVal);
                }
            }
        }
    void adjustHighLowEnd(int i)
    {
        //todo don't allow end number to be higher than the start
        int sVal = 0;
        int eVal = 0;
        try {
            sVal = Integer.parseInt(startValue.getText().toString());
            eVal = Integer.parseInt(endValue.getText().toString());
        }catch (NumberFormatException nfx){
            //cant parse
        }
        if(i>0){
            eVal+=i;
            endValue.setText(""+eVal);
        }else {
            if(sVal+1<eVal){
                eVal+=i;
                endValue.setText(""+eVal);
            }else if(sVal==0 && eVal==0){

            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //todo save state on exit
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Log.d(TAG, "p " + position);
        //DONE save the selected item for the resume
        lastSurah = position+1;
        //TODO HTTPrequest
        suraNumber = String.valueOf(position+1);

        if(sharedPreferences!=null && sharedPreferences.contains(SharedPreferences.SELECTED_MEMORIZING_SURAH))
        {
            sharedPreferences.write(SharedPreferences.SELECTED_MEMORIZING_SURAH, position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
