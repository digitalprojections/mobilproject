package furqon.io.github.mobilproject.utils;

import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchasesUpdatedListener;

public class BillingClientSetup {
    private static BillingClient instance;

    public static BillingClient getInstance(Context context, PurchasesUpdatedListener purchasesUpdatedListener){
        return instance == null?setupBillingClient(context,purchasesUpdatedListener):instance;
    }

    private static BillingClient setupBillingClient(Context context, PurchasesUpdatedListener purchasesUpdatedListener) {
        BillingClient billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(purchasesUpdatedListener)
                .build();
        return billingClient;
    }
}
