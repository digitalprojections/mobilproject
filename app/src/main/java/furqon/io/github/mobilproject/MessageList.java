package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.List;

public class MessageList extends AppCompatActivity {
    InterstitialAd mInterstitialAd;
    private RecyclerView recyclerView;
    private MessageListAdapter listAdapter;
    private TitleViewModel messageViewModel;
    private AdView mAdView;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        //============================================

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        String title = getString(R.string.messages_button_text);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent incoming_intent = getIntent();
        int reward_coins = incoming_intent.getIntExtra("personalReward", 0);
        mSharedPref = SharedPreferences.getInstance();
        mSharedPref.init(getApplicationContext());
        if (reward_coins > 0) {
            int existingCoins = mSharedPref.read(mSharedPref.COINS, 0);
            int totalCoins = existingCoins + mSharedPref.read(SharedPreferences.PERSONAL_REWARD, 50);
            mSharedPref.write(mSharedPref.COINS, totalCoins);
            Toast.makeText(this, R.string.free_coin_awards_received, Toast.LENGTH_SHORT).show();
        }

        recyclerView = findViewById(R.id.message_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listAdapter = new MessageListAdapter(this);
        recyclerView.setAdapter(listAdapter);

        messageViewModel = ViewModelProviders.of(this).get(TitleViewModel.class);
        messageViewModel.getMessages().observe(this, new Observer<List<MessageTable>>() {
            @Override
            public void onChanged(List<MessageTable> messageTables) {
                Log.e("MESSAGETABLE", messageTables.size() + " long");
                if(messageTables.size()>0){
                    listAdapter.setItems(messageTables);
                    //recyclerView.scheduleLayoutAnimation();
                }

            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                messageViewModel.deleteMessage(listAdapter.getItemAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MessageList.this, "Message has been deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
        mAdView = findViewById(R.id.adViewMessageList);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        if (BuildConfig.BUILD_TYPE == "debug") {
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            mInterstitialAd.setAdUnitId("ca-app-pub-3838820812386239/2551267023");
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageViewModel.markAllAsRead();
        mInterstitialAd.show();
    }
}
