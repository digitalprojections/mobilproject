package furqon.io.github.mobilproject;

import android.content.Context;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class RewardAd{

    private static RewardedVideoAd mRewardedVideoAd;
    int currentSurahNumber;
    boolean titleListCall;
    private Context mContext;
    FirebaseRemoteConfig mFireBaseConfig;


    public RewardAd(Context context){
        mContext = context;
        Init();
    }

    public void Init(){

        mFireBaseConfig = FirebaseRemoteConfig.getInstance();
        //ca-app-pub-3838820812386239/1790049383
        //test ca-app-pub-3940256099942544/5224354917



        // Get reference to singleton RewardedVideoAd object

        if(mRewardedVideoAd==null){
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(mContext);
        }


            mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    if (BuildConfig.BUILD_TYPE == "debug") {
                        Toast.makeText(mContext, "TEST " + mContext.getString(R.string.ad_loaded_toast), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, R.string.ad_loaded_toast, Toast.LENGTH_SHORT).show();
                    }

                    ManageCoins manageCoins;
                    if(mContext instanceof ManageCoins){
                        manageCoins = (ManageCoins) mContext;
                        manageCoins.SetCoinValues();
                    }
                }

                @Override
                public void onRewardedVideoAdOpened() {
                    //Toast.makeText(mContext,                            "Ad opened.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewardedVideoStarted() {
                    //Toast.makeText(mContext,                            "Ad started.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewardedVideoAdClosed() {

                    if (BuildConfig.BUILD_TYPE == "debug") {
                        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build()); //TEST
                        //Toast.makeText(mContext,"TEST AD.", Toast.LENGTH_SHORT).show();
                    } else {
                        mRewardedVideoAd.loadAd(mContext.getString(R.string.surahAudioUnlockAd), new AdRequest.Builder().build());
                    }
                }

                @Override
                public void onRewarded(RewardItem rewardItem) {
                    if (titleListCall) {
                        MyListener myListener;
                        if (mContext instanceof MyListener) {

                        }
                        myListener = (MyListener) mContext;
                        myListener.MarkAsAwarded(currentSurahNumber);
                        titleListCall = false;
                    } else {
                        int coins = (int) (rewardItem.getAmount() * mFireBaseConfig.getLong("rewardad_multiplier"));
                        SharedPreferences.AddCoins(mContext, coins);
                        ManageCoins manageCoins;
                        manageCoins = (ManageCoins) mContext;
                        manageCoins.SetCoinValues();
                    }
                }

                @Override
                public void onRewardedVideoAdLeftApplication() {
                    //Toast.makeText(mContext,                            "Ad left application.", Toast.LENGTH_SHORT).show();
                    Crashlytics.log("rewardAd left application");
                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {
                    //Toast.makeText(mContext,"Ad failed to load.", Toast.LENGTH_SHORT).show();
                    //enable direct access?
                    Crashlytics.log("rewardAd failed to load");
                }

                @Override
                public void onRewardedVideoCompleted() {
                    //Toast.makeText(mContext,                            "onRewardedVideoCompleted.", Toast.LENGTH_SHORT).show();
                }
            });
        if (BuildConfig.BUILD_TYPE == "debug") {
            mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build()); //TEST
            //Toast.makeText(mContext,"TEST AD.", Toast.LENGTH_SHORT).show();
        } else {
            mRewardedVideoAd.loadAd(mContext.getString(R.string.surahAudioUnlockAd), new AdRequest.Builder().build());
        }

    }


    public void SHOW() {
        //currentSurahNumber = Integer.parseInt(s);
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    public void SHOW(String s) {
        titleListCall = true;
        currentSurahNumber = Integer.parseInt(s);
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }else{
            /*
             * If user has coins, simply allow download
             * */
            int xcoins = SharedPreferences.getInstance().read(SharedPreferences.getInstance().COINS, 0);
            int newtotal;
            if(xcoins>=100){
                newtotal = xcoins-100;
                SharedPreferences.getInstance().write(SharedPreferences.getInstance().COINS, newtotal);
                MyListener myListener;
                myListener = (MyListener) mContext;
                myListener.MarkAsAwarded(currentSurahNumber);
            }else{
                //Toast.makeText(mContext, mContext.getString(R.string.you_need) + (100 - xcoins) + " " + mContext.getString(R.string._coins) + mContext.getString(R.string.uz_kam), Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, mContext.getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
