package com.n00blife.lockit.activities;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.n00blife.lockit.R;
import com.n00blife.lockit.fragments.AboutFragment;
import com.n00blife.lockit.fragments.AppPreferenceFragment;
import com.n00blife.lockit.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ProfileFragment profileFragment = new ProfileFragment();
    private AboutFragment aboutFragment = new AboutFragment();
    private AppPreferenceFragment appPreferenceFragment = new AppPreferenceFragment();
    private int previousSelectedItemResId;

    private int RESULT = 23;

    private boolean isUsageStatsPermissionGranted() {
        boolean isPermissionGranted = false;
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                getPackageName());

        if(mode == AppOpsManager.MODE_DEFAULT) {
            Log.d("MAINRESULT", "Trouble");
            isPermissionGranted = checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED;
        } else {
            isPermissionGranted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return isPermissionGranted;
    }
    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RESULT) {
            if(isUsageStatsPermissionGranted()) {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                usageStatDialog.dismiss();
                            }
                        });
                    }
                }, 1000);
            } else {
                Toast.makeText(MainActivity.this, "Permission Not Granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private AlertDialog usageStatDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileFragment = new ProfileFragment();
        aboutFragment = new AboutFragment();
        appPreferenceFragment = new AppPreferenceFragment();

        AlertDialog.Builder usageStatDialogBuilder = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.usage_status_prompt_messsage))
                .setPositiveButton("OPEN SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), RESULT);
                    }
                })
                .setCancelable(false)
                .setTitle("Permission Required");

        usageStatDialog = usageStatDialogBuilder.create();

        if(!isUsageStatsPermissionGranted())
            usageStatDialog.show();

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

                if (previousSelectedItemResId == item.getItemId())
                    return false;

                Fragment fragment;

                switch (item.getItemId()) {
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
