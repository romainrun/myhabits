package com.rrtech.myhabits.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.billingclient.api.*;
import com.rrtech.myhabits.state.ProState;

import java.util.List;

public class BillingManager implements PurchasesUpdatedListener {

    private final BillingClient billingClient;
    private final Activity activity;
    private final String skuPro = "myhabits_pro";
    private final BillingListener listener;
    private final SharedPreferences prefs;

    public interface BillingListener {
        void onPurchaseSuccess();
        void onPurchaseError();
        void onAlreadyOwned();
    }

    public BillingManager(Activity activity, BillingListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.prefs = activity.getSharedPreferences("pro_prefs", Context.MODE_PRIVATE);

        billingClient = BillingClient.newBuilder(activity)
                .enablePendingPurchases()
                .setListener(this)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    checkIfProAlreadyOwned();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Optionnel : gérer reconnexion
            }
        });
    }

    private void checkIfProAlreadyOwned() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, (billingResult, purchases) -> {
            boolean owned = false;
            if (purchases != null) {
                for (Purchase purchase : purchases) {
                    if (purchase.getProducts().contains(skuPro)) {
                        owned = true;
                        break;
                    }
                }
            }
            setProStatus(owned);
            if (owned) listener.onAlreadyOwned();
        });
    }

    public void launchPurchaseFlow() {
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(skuPro)
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(List.of(product)).build(),
                (billingResult, productDetailsList) -> {
                    if (!productDetailsList.isEmpty()) {
                        ProductDetails productDetails = productDetailsList.get(0);
                        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(List.of(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(productDetails)
                                                .build()
                                )).build();
                        billingClient.launchBillingFlow(activity, flowParams);
                    }
                }
        );
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase.getProducts().contains(skuPro)) {
                    setProStatus(true);
                    listener.onPurchaseSuccess();
                    return;
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            setProStatus(true);
            listener.onAlreadyOwned();
        } else {
            setProStatus(false);
            listener.onPurchaseError();
        }
    }

    private void setProStatus(boolean isPro) {
        proPurchased = isPro;
        ProState.getInstance(activity).setPro(isPro);
        prefs.edit().putBoolean("is_pro", isPro).apply();
    }

    // getter local
    private boolean proPurchased = false;

    public boolean isProPurchased() {
        return proPurchased;
    }
}
