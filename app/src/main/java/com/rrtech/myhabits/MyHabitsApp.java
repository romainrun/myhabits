package com.rrtech.myhabits;

import android.app.Application;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryPurchasesParams;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.rrtech.myhabits.state.ProState;

public class MyHabitsApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialisation des outils nécessaires
        ProState.getInstance(this);
        AndroidThreeTen.init(this); // Pour les dates locales

        BillingClient billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener((billingResult, purchases) -> {
                    // Rien ici : les achats sont checkés ailleurs si besoin
                })
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    .build(),
                            (result, purchases) -> {
                                boolean foundPro = false;
                                if (purchases != null) {
                                    for (Purchase purchase : purchases) {
                                        if (purchase.getProducts().contains("myhabits_pro")) {
                                            foundPro = true;
                                            break;
                                        }
                                    }
                                }

                                // Met à jour le singleton et les prefs
                                ProState.getInstance(MyHabitsApp.this).setPro(foundPro);
                            });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Tu peux ici gérer une reconnexion auto si tu veux
            }
        });
    }
}
