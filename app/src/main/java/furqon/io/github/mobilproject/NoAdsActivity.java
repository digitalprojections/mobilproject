package furqon.io.github.mobilproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import furqon.io.github.mobilproject.utils.BillingClientSetup;

public class NoAdsActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    private PurchasesUpdatedListener purchasesUpdatedListener;
    private ConsumeResponseListener consumeResponseListener;

    private final String SKU_MONTHLY = "product_monthly";
    private final String SKU_PERMANENT = "no_ads_forever";
    private ArrayList<SkuDetails> skuDetailsList;
    private BillingClient billingClient;
    private ConstraintLayout container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_ads);
        skuDetailsList = new ArrayList<SkuDetails>();
        container = findViewById(R.id.noads_clayout);

        setupBillingClient();
    }

    private void setupBillingClient() {
        consumeResponseListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    Snackbar.make(container, "Consume OK!", BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }
        };

        billingClient = BillingClientSetup.getInstance(this, this);
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    Snackbar.make(container, "Connection Successful", BaseTransientBottomBar.LENGTH_LONG).show();
                    List<Purchase> purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                            .getPurchasesList();
                    handleItemsAlreadPurchased(purchases);
                }else{
                    Snackbar.make(container, "Error code:"+
                            billingResult.getResponseCode(), BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Snackbar.make(container, "You are disconnected from billing services!", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

    private void handleItemsAlreadPurchased(List<Purchase> purchases) {
        for (Purchase purchase:purchases){
            if(purchase.getSku().equals(SKU_PERMANENT)){
                ConsumeParams consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.consumeAsync(consumeParams, consumeResponseListener);
            }
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

    }
}