package com.n00blife.lockit.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.n00blife.lockit.R;
import com.n00blife.lockit.fragments.AboutFragment;
import com.n00blife.lockit.fragments.AppPreferenceFragment;
import com.n00blife.lockit.fragments.ProfileFragment;
import com.n00blife.lockit.receiver.PackageBroadcastReceiver;

public class MainActivity extends AppCompatActivity {

    private PackageBroadcastReceiver packageBroadcastReceiver;
    private BottomNavigationView bottomNavigationView;
    private ProfileFragment profileFragment = new ProfileFragment();
    private AboutFragment aboutFragment = new AboutFragment();
    private AppPreferenceFragment appPreferenceFragment = new AppPreferenceFragment();
    private int previousSelectedItemResId;

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(packageBroadcastReceiver);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileFragment = new ProfileFragment();
        aboutFragment = new AboutFragment();
        appPreferenceFragment = new AppPreferenceFragment();

        packageBroadcastReceiver = new PackageBroadcastReceiver();
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);

        registerReceiver(packageBroadcastReceiver, packageFilter);

        bottomNavigationView = findViewById(R.id.main_navbar);

        bottomNavigationView.getMenu().getItem(0).setChecked(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_layout, profileFragment, profileFragment.getClass().getSimpleName())
                .commit();

        previousSelectedItemResId = R.id.profiles_item;

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(previousSelectedItemResId == item.getItemId())
                    return false;

                Fragment fragment;

                switch(item.getItemId()) {
                    case R.id.profiles_item:
                        previousSelectedItemResId = R.id.profiles_item;
                        fragment = profileFragment;
                        break;
                    case R.id.preferences_item:
                        previousSelectedItemResId = R.id.preferences_item;
                        fragment = appPreferenceFragment;
                        break;
                    case R.id.about_item:
                        previousSelectedItemResId = R.id.about_item;
                        fragment = aboutFragment;
                        break;
                    default:
                        return false;
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.main_fragment_layout, fragment, fragment.getClass().getSimpleName())
                        .commit();
                return true;
            }
        });
    }

}
