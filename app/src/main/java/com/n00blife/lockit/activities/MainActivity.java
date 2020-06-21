package com.n00blife.lockit.activities;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.app.AppOpsManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.n00blife.lockit.R;
import com.n00blife.lockit.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ProfileFragment profileFragment = new ProfileFragment();

    private int RESULT = 23;

    private boolean isUsageStatsPermissionGranted() {
        boolean isPermissionGranted = false;
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                getPackageName());

        if(mode == AppOpsManager.MODE_DEFAULT) {
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
        } else if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION) {
            Log.d("Main", "onActivityResult: good");
        }
    }

    private AlertDialog usageStatDialog;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileFragment = new ProfileFragment();

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

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_layout, profileFragment, profileFragment.getClass().getSimpleName())
                .commit();

    }

}
