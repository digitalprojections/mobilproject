package furqon.io.github.mobilproject;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.reward.AdMetadataListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import io.reactivex.internal.schedulers.NewThreadWorker;

public class RewardAd{

    private static RewardedVideoAd mRewardedVideoAd;
    int currentSurahNumber;
    private Context mContext;


    public RewardAd(Context context){
        mContext = context;
        Init();
    }

    public void Init(){

        //ca-app-pub-3838820812386239/1790049383
        //test ca-app-pub-3940256099942544/5224354917



        // Get reference to singleton RewardedVideoAd object

        if(mRewardedVideoAd==null){
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(mContext);
        }


            mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    //Toast.makeText(mContext,                            "Ad loaded.", Toast.LENGTH_SHORT).show();
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
                    //Toast.makeText(mContext,                            "Ad closed.", Toast.LENGTH_SHORT).show();
                    mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build()); //TEST
                    //mRewardedVideoAd.loadAd(mContext.getString(R.string.surahAudioUnlockAd), new AdRequest.Builder().build());
                }

                @Override
                public void onRewarded(RewardItem rewardItem) {
                    int coins = rewardItem.getAmount()*50;
                    int existingCoins = sharedpref.getInstance().read(sharedpref.getInstance().COINS, 0);
                    int totalCoins = existingCoins + coins;
                    sharedpref.getInstance().write(sharedpref.getInstance().COINS, totalCoins);
                    Toast.makeText(mContext,"Ad triggered reward. Coins amount: " + totalCoins, Toast.LENGTH_LONG).show();
                    ManageCoins manageCoins;

                    manageCoins = (ManageCoins) mContext;
                    manageCoins.SetCoinValues();

                }

                @Override
                public void onRewardedVideoAdLeftApplication() {
                    //Toast.makeText(mContext,                            "Ad left application.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {
                    //Toast.makeText(mContext,                            "Ad failed to load.", Toast.LENGTH_SHORT).show();
                    //enable direct access
                    //
                }

                @Override
                public void onRewardedVideoCompleted() {
                    //Toast.makeText(mContext,                            "onRewardedVideoCompleted.", Toast.LENGTH_SHORT).show();



                }
            });
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build()); //TEST
        //mRewardedVideoAd.loadAd(mContext.getString(R.string.surahAudioUnlockAd), new AdRequest.Builder().build());
    }


    public void SHOW() {
        //currentSurahNumber = Integer.parseInt(s);
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

}
