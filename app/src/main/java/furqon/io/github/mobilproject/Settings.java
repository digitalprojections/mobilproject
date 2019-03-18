package furqon.io.github.mobilproject;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH1 = "switch1";
    public static final String SWITCH2 = "switch2";
SharedPreferences sharedPreferences;

    private boolean sw_ar_on;
    private boolean sw_uz_on;

    private Switch sw_ar;
    private Switch sw_uz;
    private Button ok_but;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sw_ar = findViewById(R.id.arabic_sw);
        sw_uz = findViewById(R.id.uzbek_sw);
        ok_but = findViewById(R.id.done_setting);

        ok_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_settings();
            }
        });



        loadData();
        updateView();
    }

    private void save_settings()
    {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCH1, sw_ar.isChecked());
        editor.putBoolean(SWITCH2, sw_uz.isChecked());

        editor.apply();

        Toast.makeText(this,"Setting are saved", Toast.LENGTH_SHORT);
    }

    public void loadData()
    {

        sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        if(sharedPreferences.contains(SWITCH1)) {
            sw_ar_on = sharedPreferences.getBoolean(SWITCH1, false);

        if(sharedPreferences.contains(SWITCH2)){
            sw_uz_on = sharedPreferences.getBoolean(SWITCH2, false);
        }
        }
    }

    public void updateView(){
        sw_ar.setChecked(sw_ar_on);
        sw_uz.setChecked(sw_uz_on);
    }

}
