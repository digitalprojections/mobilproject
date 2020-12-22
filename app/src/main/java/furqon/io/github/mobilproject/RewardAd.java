package furqon.io.github.mobilproject;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RewardAd{

    private static RewardedAd mRewardedAd;
    int currentSurahNumber;
    boolean titleListCall;
    private final Context mContext;
    FirebaseRemoteConfig mFireBaseConfig;
    private boolean nomore;
    RewardedAdCallback rewardedAdCallback;

    public RewardAd(Context context){
        mContext = context;
        Init();
    }

    public void Init() {

        mFireBaseConfig = FirebaseRemoteConfig.getInstance();
        //ca-app-pub-3838820812386239/1790049383
        //test ca-app-pub-3940256099942544/5224354917
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            mRewardedAd = new RewardedAd(mContext,
                    "ca-app-pub-3940256099942544/5224354917");
        } else {
            mRewardedAd = new RewardedAd(mContext,
                    "ca-app-pub-3838820812386239/1790049383");
        }


        // Get reference to singleton RewardedVideoAd object

        final RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                super.onRewardedAdLoaded();
                if (BuildConfig.BUILD_TYPE.equals("debug")) {
                    Toast.makeText(mContext, "TEST " + mContext.getString(R.string.ad_loaded_toast), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, R.string.ad_loaded_toast, Toast.LENGTH_SHORT).show();
                }

                ManageCoins manageCoins;
                if (mContext instanceof ManageCoins) {
                    manageCoins = (ManageCoins) mContext;
                    manageCoins.SetCoinValues();
                }
                ManageDownloadIconState manageIcons;
                if (mContext instanceof ManageDownloadIconState) {
                    manageIcons = (ManageDownloadIconState) mContext;
                    manageIcons.SetDownloadIconState(true);
                }
            }
        };


        rewardedAdCallback = new RewardedAdCallback() {
            @Override
            public void onRewardedAdOpened() {
                // Ad opened.
            }

            @Override
            public void onRewardedAdClosed() {
                // Ad closed.
                mRewardedAd.loadAd(new AdRequest.Builder().build(), rewardedAdLoadCallback);
            }

            @Override
            public void onRewardedAdFailedToShow(AdError adError) {
                // Ad failed to display.
            }

            @Override
            public void onUserEarnedReward(@NonNull com.google.android.gms.ads.rewarded.RewardItem rewardItem) {
                if (titleListCall) {
                    MyListener myListener;
                    myListener = (MyListener) mContext;
                    myListener.MarkAsAwarded(currentSurahNumber);
                    titleListCall = false;
                    ManageDownloadIconState manageIcons;
                    if (mContext instanceof ManageDownloadIconState) {
                        manageIcons = (ManageDownloadIconState) mContext;
                        manageIcons.SetDownloadIconState(false);
                    }
                } else if (nomore) {
                    SharedPreferences.NoMoreAds(true);
                    nomore = false;
                    Date c = Calendar.getInstance().getTime();

                    SharedPreferences.getInstance().write(SharedPreferences.PREVIOUS_SET, String.valueOf(c.getTime()));
                } else {
                    int coins = (int) (rewardItem.getAmount() * mFireBaseConfig.getLong("rewardad_multiplier"));
                    SharedPreferences.AddCoins(mContext, coins);
                    ManageCoins manageCoins;
                    manageCoins = (ManageCoins) mContext;
                    manageCoins.SetCoinValues();
                }
            }

        };

        mRewardedAd.loadAd(new AdRequest.Builder().build(), rewardedAdLoadCallback);
    }


    public void SHOW(Activity activity) {
        //currentSurahNumber = Integer.parseInt(s);
        if (mRewardedAd.isLoaded()) {
            mRewardedAd.show(activity, rewardedAdCallback);
        }
    }

    public void NOMORE(Activity activity) {
        nomore = true;
        if (mRewardedAd.isLoaded()) {
            mRewardedAd.show(activity, rewardedAdCallback);
        }
    }

    public void SHOW(String s) {
        titleListCall = true;
        currentSurahNumber = Integer.parseInt(s);
        if (mRewardedAd.isLoaded()) {
            //mRewardedAd.show();
        } else {
            /*
             * If user has coins, simply allow download
             * */
            int xcoins = SharedPreferences.getInstance().read(SharedPreferences.getInstance().COINS, 0);
            int newtotal;
            if (xcoins >= 100) {
                newtotal = xcoins - 100;
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
