package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class CommunicationActivity extends AppCompatActivity {
    InterstitialAd mInterstitialAd;
    TextView message_txt;
    Button send;
    RecyclerView message_rv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        message_rv = findViewById(R.id.communication_rv);
        message_txt = findViewById(R.id.message_to_author_et);
        send = findViewById(R.id.send_btn);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE == "debug") {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mInterstitialAd.show();
    }
}
