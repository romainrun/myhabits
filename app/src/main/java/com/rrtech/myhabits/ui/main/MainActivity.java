package com.rrtech.myhabits.ui.main;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.badge.BadgeDrawable;
import com.rrtech.myhabits.R;
import com.rrtech.myhabits.databinding.ActivityMainBinding;
import com.rrtech.myhabits.utils.ProManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // ✅ Ajout de statsFragment dans la configuration
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.groupsFragment, R.id.statsFragment, R.id.proFragment
        ).build();
        NavigationUI.setupWithNavController(binding.topAppBar, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        // ✅ Gérer le bouton flottant selon les fragments
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.homeFragment || destination.getId() == R.id.groupsFragment) {
                binding.fabAddHabit.show();
            } else {
                binding.fabAddHabit.hide();
            }
        });

        binding.fabAddHabit.setOnClickListener(v -> {
            navController.navigate(R.id.addEditHabitFragment);
        });

        // ✅ Badge pour version Pro
        if (ProManager.getInstance(this).isPro()) {
            BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.proFragment);
            badge.setVisible(true);
            badge.setNumber(1);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            badge.setBadgeTextColor(Color.WHITE);
            badge.setHorizontalOffset(6);
            badge.setVerticalOffset(6);
            badge.clearNumber();
        }
    }

    private void updateProBadge() {
        if (ProManager.getInstance(this).isPro()) {
            binding.bottomNavigation.getOrCreateBadge(R.id.proFragment).setVisible(true);
        } else {
            binding.bottomNavigation.removeBadge(R.id.proFragment);
        }
    }
}
