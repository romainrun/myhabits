package com.rrtech.myhabits.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.rrtech.myhabits.R;
import com.rrtech.myhabits.databinding.FragmentProBinding;
import com.rrtech.myhabits.manager.BillingManager;
import com.rrtech.myhabits.state.ProState;

public class ProController {

    private final FragmentProBinding binding;
    private final Context context;
    private BillingManager billingManager;

    private final String[] features = new String[]{
            "Habitudes illimitées",
            "Emojis et couleurs exclusifs",
            "Rappels avancés",
            "Ajout de plusieurs routines",
            "Icônes premium"
    };

    private final int[] iconIds = new int[]{
            R.drawable.ic_check_circle,
            R.drawable.ic_emoji,
            R.drawable.ic_alarm,
            R.drawable.ic_layers,
            R.drawable.ic_star
    };

    public ProController(Context context, FragmentProBinding binding) {
        this.context = context;
        this.binding = binding;
    }

    public void setup() {
        if (ProState.getInstance(context).isPro()) {
            showProPurchasedLayout();
        } else {
            populateFeatureListInNotPurchasedLayout(); // <= Nouvel appel ici
        }

        initBilling();

        binding.buttonBuyPro.setOnClickListener(v -> {
            if (billingManager != null) {
                billingManager.launchPurchaseFlow();
            }
        });
    }

    private void initBilling() {
        billingManager = new BillingManager((Activity) context, new BillingManager.BillingListener() {
            @Override
            public void onPurchaseSuccess() {
                showProPurchasedLayout();
                Toast.makeText(context, "Version Pro activée ! 🎉", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPurchaseError() {
                Toast.makeText(context, "Erreur lors de l'achat", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAlreadyOwned() {
                showProPurchasedLayout();
            }
        });
    }

    private void showProPurchasedLayout() {
        binding.layoutProNotPurchased.setVisibility(View.GONE);

        View layout = binding.layoutProPurchased;
        layout.setAlpha(0f);
        layout.setScaleX(0.9f);
        layout.setScaleY(0.9f);
        layout.setVisibility(View.VISIBLE);

        layout.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator())
                .start();

        populateFeatureList(binding.textProFeatures);
    }

    private void populateFeatureListInNotPurchasedLayout() {
        if (binding.textProFeaturesList != null) {
            populateFeatureList(binding.textProFeaturesList);
        }
    }

    private void populateFeatureList(android.widget.TextView targetTextView) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (int i = 0; i < features.length; i++) {
            Drawable icon = ContextCompat.getDrawable(context, iconIds[i]);
            if (icon == null) continue;

            icon.setBounds(0, 0, 48, 48); // ajustable
            ImageSpan imageSpan = new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM);

            int start = builder.length();
            builder.append(" "); // placeholder
            builder.setSpan(imageSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append(" ").append(features[i]).append("\n");
        }

        targetTextView.setText(builder);
        targetTextView.setVisibility(View.VISIBLE);
    }

    public void release() {
        billingManager = null;
    }
}
