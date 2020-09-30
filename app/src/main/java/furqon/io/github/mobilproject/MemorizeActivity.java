package furqon.io.github.mobilproject;

import   androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import furqon.io.github.mobilproject.Services.OnClearFromService;

import static furqon.io.github.mobilproject.BuildConfig.*;

public class MemorizeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final String TAG = MemorizeActivity.class.getSimpleName();

    //DEFINE UI ELEMENTS
    private Spinner suranames_spinner;
    private Button memorizeButton;
    private Button decButton;
    private Button incButton;
    private EditText startValue;
    private EditText endValue;
    private EditText repeatValue;

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
        memorizeButton = findViewById(R.id.memorize_button);
        decButton = findViewById(R.id.dec_button);
        incButton = findViewById(R.id.inc_button);

        startValue = findViewById(R.id.editTextNumber1);
        endValue = findViewById(R.id.editTextNumber2);
        repeatValue = findViewById(R.id.editTextNumber3);

        recyclerView = findViewById(R.id.memorize_range_rv);
        adapter = new MemorizeActivityAdapter(this);

        //todo save state on exit
        //todo "memorize" button action

        //UI ACTION
        memorizeButton.setOnClickListener(this);
        decButton.setOnClickListener(this);
        incButton.setOnClickListener(this);

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
