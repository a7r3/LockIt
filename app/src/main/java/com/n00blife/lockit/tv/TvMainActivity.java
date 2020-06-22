package com.n00blife.lockit.tv;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.n00blife.lockit.R;
import com.n00blife.lockit.adapter.ApplicationAdapter;
import com.n00blife.lockit.database.BlacklistDatabase;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.model.Blacklist;
import com.n00blife.lockit.services.LockService;
import com.n00blife.lockit.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class TvMainActivity extends Activity {

    ArrayList<Application> applications = new ArrayList<>();
    RecyclerView appList;
    ApplicationAdapter adapter;
    TextView selectAll, resetOptions, startSession;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_main);
        appList = findViewById(R.id.apps_list);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4, RecyclerView.VERTICAL, false);
        appList.setLayoutManager(layoutManager);
        adapter = new ApplicationAdapter(this, applications, R.layout.app_item);

        // Remote Lock (only for Android TVs) - Don't copy it over to Phone Impl
        FirebaseMessaging.getInstance().subscribeToTopic("locker").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: Subbed!");
            }
        });

        startSession = findViewById(R.id.start_session);
        startSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Application> applications = adapter.getSelectedApplications();
                if (applications.isEmpty()) {
                    Toast.makeText(TvMainActivity.this, "Select at least one application", Toast.LENGTH_LONG).show();
                    return;
                }

                final ArrayList<String> pkgList = new ArrayList<>();

                for (Application a : applications) {
                    pkgList.add(a.getApplicationPackageName());
                }

                BlacklistDatabase.getInstance(TvMainActivity.this).profileDao().createBlacklist(new Blacklist(pkgList));
//                Toast.makeText(TvMainActivity.this, "Device Locked", Toast.LENGTH_LONG).show();
                Utils.startLockService(TvMainActivity.this);
                finish();
            }
        });

        resetOptions = findViewById(R.id.reset);
        resetOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.applyBlacklistData(TvMainActivity.this, applications);
                adapter.notifyDataSetChanged();
            }
        });

        selectAll = findViewById(R.id.select_all);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Application a : applications)
                    a.setSelected(true);
                adapter.notifyDataSetChanged();
            }
        });

        Utils.retrieveApplicationList(this, new Utils.AppRetrivalInterface() {
            @Override
            public void onProgress() {

            }

            @Override
            public void onComplete(List<Application> applications) {
                TvMainActivity.this.applications.addAll(Utils.applyBlacklistData(TvMainActivity.this, applications));
                appList.setAdapter(adapter);
            }
        });
    }
}