package furqon.io.github.mobilproject;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.reward.AdMetadataListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class RewardAd{

    private RewardedVideoAd mRewardedVideoAd;
    int currentSurahNumber;
    Context mContext;

    public RewardAd(Context context){
        mContext = context;
        Init();
    }

    public void Init(){

        //ca-app-pub-3838820812386239/1790049383
        //test ca-app-pub-3940256099942544/5224354917
        //mRewardedAd.loadAd(mContext.getString(R.string.surahAudioUnlockAd),


        // Get reference to singleton RewardedVideoAd object

            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(mContext);

            mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    Toast.makeText(mContext,
                            "Ad loaded.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewardedVideoAdOpened() {
                    Toast.makeText(mContext,
                            "Ad opened.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewardedVideoStarted() {
                    Toast.makeText(mContext,
                            "Ad started.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    Toast.makeText(mContext,
                            "Ad closed.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewarded(RewardItem rewardItem) {
                    Toast.makeText(mContext,
                            "Ad triggered reward. Sura Number: " + currentSurahNumber + " " + rewardItem.getAmount(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onRewardedVideoAdLeftApplication() {
                    Toast.makeText(mContext,
                            "Ad left application.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {
                    Toast.makeText(mContext,
                            "Ad failed to load.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRewardedVideoCompleted() {

                }
            });
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
    }

    public void pause(Context context) {
        this.pause(context);
    }

    public void resume(Context context) {
        this.resume(context);
    }

    public void destroy() {

    }

    public void destroy(Context context) {
        this.destroy(context);
    }

    public void SHOW(String s) {
        currentSurahNumber = Integer.parseInt(s);
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

}
