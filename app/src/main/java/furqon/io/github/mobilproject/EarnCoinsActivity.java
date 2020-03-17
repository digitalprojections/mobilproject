package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class EarnCoinsActivity extends AppCompatActivity {

    private ImageButton share_btn;
    private ImageButton watchAds_btn;
    private TextView coins_txt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earn_coins);
        //=============================================
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

    }
    private void shareDeepLink(String deepLink) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Firebase Deep Link");
        intent.putExtra(Intent.EXTRA_TEXT, "QURAN KAREEM Android " + deepLink);

        startActivity(intent);
    }
}
