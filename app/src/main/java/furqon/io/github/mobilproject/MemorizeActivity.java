package furqon.io.github.mobilproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

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

    private TextView startValue;
    private TextView endValue;
    private TextView repeatValue;

    private RecyclerView recyclerView;
    private MemorizeActivityAdapter adapter;
    private Integer lastSurah = 0;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize);
        sharedPreferences = SharedPreferences.getInstance();
        //DONE restore the last state
        if(sharedPreferences.contains(SharedPreferences.SELECTED_MEMORIZING_SURAH)){
            lastSurah = sharedPreferences.read(SharedPreferences.SELECTED_MEMORIZING_SURAH, 0);
        }

        //INITIALIZE UI ELEMENTS
        suranames_spinner = findViewById(R.id.surah_spinner);
        playVerse = findViewById(R.id.play_verse);
        decRepeat = findViewById(R.id.dec_repeat);
        incRepeat = findViewById(R.id.inc_repeat);

        startValue = findViewById(R.id.start_tv);
        endValue = findViewById(R.id.end_tv);
        repeatValue = findViewById(R.id.repeat_count_tv);

        recyclerView = findViewById(R.id.memorize_range_rv);
        adapter = new MemorizeActivityAdapter(this);

        //todo save state on exit
        //todo "memorize" button action

        //UI ACTION
        playVerse.setOnClickListener(this);
        decRepeat.setOnClickListener(this);
        incRepeat.setOnClickListener(this);

        suranames_spinner.setOnItemSelectedListener(this);

        //todo don't allow end number to be higher than the start
        /*todo end number never lower than the start
           if start number entered and it is higher than the end number, set the end number
            equal to the start number. But if the start number is changed to a lower value,
            reset the end number back to what it was before
         */
        //todo
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
    public void onClick(View v) {
        //TODO make an HTTP request to load the matching ayats
        //show the selected range

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Log.d(TAG, "p " + position);
        //DONE save the selected item for the resume
        lastSurah = position;
        //TODO HTTPrequest

        sharedPreferences.write(SharedPreferences.SELECTED_MEMORIZING_SURAH, position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
