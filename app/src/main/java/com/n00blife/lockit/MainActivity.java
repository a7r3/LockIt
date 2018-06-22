package com.n00blife.lockit;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.constraint.ConstraintLayout;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.n00blife.lockit.adapter.ApplicationAdapter;
import com.n00blife.lockit.adapter.WhitelistedApplicationAdapter;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.util.MarginDividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Application> applicationArrayList = new ArrayList<>();
    private ArrayList<Application> whitelistedApplicationList = new ArrayList<>();
    private ArrayList<Application> applicationArrayListBackup = new ArrayList<>();
    private ApplicationAdapter applicationAdapter;
    private RecyclerView applicationListRecycler;
    private WhitelistedApplicationAdapter whitelistedApplicationAdapter;
    private ConstraintLayout whiteListCard;

    private Button addButton;
    private TextView cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pm = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> launchables = pm.queryIntentActivities(mainIntent, 0);

        Collections.sort(launchables, new ResolveInfo.DisplayNameComparator(pm));

        for (ResolveInfo resolveInfo : launchables) {
            ActivityInfo info = resolveInfo.activityInfo;
            applicationArrayList.add(new Application(
                    resolveInfo.loadLabel(pm).toString(),
                    info.packageName,
                    "0.01",
                    resolveInfo.loadIcon(pm)
            ));
        }

        applicationArrayListBackup.addAll(applicationArrayList);

        whitelistedApplicationAdapter = new WhitelistedApplicationAdapter(this, whitelistedApplicationList, new WhitelistedApplicationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Application application, int position) {
//                whitelistedApplicationAdapter.notifyDataSetChanged();
//                whitelistedApplicationList.remove(application);
//                applicationArrayList.add(application.getPositionInApplicationList(), application);
//                applicationAdapter.notifyItemInserted(application.getPositionInApplicationList());
            }
        });

        RecyclerView whiteListRecycler = findViewById(R.id.whitelisted_apps);
        whiteListRecycler.setAdapter(whitelistedApplicationAdapter);
        whiteListRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        applicationListRecycler = findViewById(R.id.apps_list);
        applicationListRecycler.addItemDecoration(new MarginDividerItemDecoration(this));
        applicationAdapter = new ApplicationAdapter(this, applicationArrayList, new ApplicationAdapter.OnItemClicked() {
            @Override
            public void onHolderClick(ApplicationAdapter.ViewHolder viewHolder, Application application) {
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                whiteListCard.setVisibility(View.VISIBLE);
                applicationArrayList.remove(application);
                applicationAdapter.notifyItemRemoved(application.getPositionInApplicationList());
                whitelistedApplicationList.add(application);
                whitelistedApplicationAdapter.notifyItemInserted(whitelistedApplicationList.size() - 1);
            }
        });

        applicationListRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        applicationListRecycler.setAdapter(applicationAdapter);

        whiteListCard = findViewById(R.id.whitelist_card);

        addButton = findViewById(R.id.add_to_whitelist_button);
        cancelButton = findViewById(R.id.cancel_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Specified Apps added to Whitelist", Toast.LENGTH_LONG).show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whitelistedApplicationList.clear();
                applicationArrayList.clear();
                applicationArrayList.addAll(applicationArrayListBackup);
                applicationAdapter.notifyDataSetChanged();
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                whiteListCard.setVisibility(View.GONE);
            }
        });

    }

}
