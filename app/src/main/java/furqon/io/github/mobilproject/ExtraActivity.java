package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

public class ExtraActivity extends AppCompatActivity {
    Button favourite_but;
    Button search_but;
    Button youtube_but;
    Button rate_but;
    Button message_but;
    TextView nbadge;

    Button coins_but;

    private TitleViewModel titleViewModel;

    private Animation scaler;
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//
//        inflater.inflate(R.menu.options_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.invite_i:
//                //shareDeepLink();
//                return true;
//            case R.id.settings_i:
//                //open_settings();
//                return true;
//            case R.id.favourites_i:
//                open_favourites();
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);
        //=============================================
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.extra_title);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        favourite_but = findViewById(R.id.favouritebut);
        search_but = findViewById(R.id.searchbtn);
        youtube_but = findViewById(R.id.youtubebut);
        rate_but = findViewById(R.id.ratebtn);
        coins_but = findViewById(R.id.earn_coins_button);

        favourite_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_favourites();
            }
        });
        search_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_search();
            }
        });
        youtube_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=Cj7CLGGgRao"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        rate_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Rateus();
            }
        });
        coins_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_earn_coins();
            }
        });


        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(3) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();


        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
        titleViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);

        scaler = AnimationUtils.loadAnimation(this, R.anim.bounce);
        titleViewModel.getUnreadCount().observe(this, new Observer<List<NewMessages>>() {
            @Override
            public void onChanged(List<NewMessages> newMessages) {
                if (newMessages.size() > 0) {
                    nbadge.setVisibility(View.VISIBLE);
                    nbadge.setText(String.valueOf(newMessages.size()));
                    nbadge.startAnimation(scaler);
                } else {
                    nbadge.setVisibility(View.INVISIBLE);
                    nbadge.setText("0");
                }
            }
        });
        message_but = findViewById(R.id.messageButton);
        message_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_messages();
            }
        });

        nbadge = findViewById(R.id.numeric_badge_txt);
        nbadge.bringToFront();
    }

    private void open_earn_coins() {
        Toast.makeText(getApplicationContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();

//        Intent intent;
//        intent = new Intent(this, EarnCoinsActivity.class);
//        startActivity(intent);
    }

    private void Rateus() {
        AppRate.with(this).showRateDialog(this);
    }
    private void open_favourites() {
        Intent intent;
        intent = new Intent(this, Favourites.class);
        startActivity(intent);
    }

    private void open_search() {
        Intent intent;
        intent = new Intent(this, Search.class);
        startActivity(intent);
    }
    private void open_messages() {
        Intent intent;
        intent = new Intent(this, MessageList.class);
        startActivity(intent);
    }
}
