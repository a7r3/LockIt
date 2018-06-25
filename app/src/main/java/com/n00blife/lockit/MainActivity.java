package com.n00blife.lockit;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.n00blife.lockit.adapter.ApplicationAdapter;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.services.LockService;
import com.n00blife.lockit.util.Constants;
import com.n00blife.lockit.util.MarginDividerItemDecoration;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    // FIXME PLEASE! THIS IS SO BAD (SQLite Implementation Pls)
    private ArrayList<Application> applicationArrayList = new ArrayList<>();
    private ArrayList<Application> whitelistedApplicationList = new ArrayList<>();
    private ArrayList<Application> applicationArrayListBackup = new ArrayList<>();

    private ApplicationAdapter applicationAdapter;
    private ApplicationAdapter whitelistedApplicationAdapter;
    private RecyclerView applicationListRecycler;
    private ConstraintLayout whiteListCard;

    private Button addButton;
    private TextView cancelButton;
    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pm = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        applicationListRecycler = findViewById(R.id.apps_list);
        applicationListRecycler.addItemDecoration(new MarginDividerItemDecoration(this));
        applicationAdapter = new ApplicationAdapter(this, applicationArrayList, R.layout.app_item);
        applicationAdapter.setOnItemClicked(new ApplicationAdapter.onItemClicked() {
            @Override
            public void onHolderClick(ApplicationAdapter.ViewHolder viewHolder, Application application) {
                applicationArrayList.remove(application);
                applicationAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                whiteListCard.setVisibility(View.VISIBLE);
                whitelistedApplicationList.add(application);
                whitelistedApplicationAdapter.notifyItemInserted(whitelistedApplicationList.size() - 1);
            }
        });

        applicationListRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        applicationListRecycler.setAdapter(applicationAdapter);

        final ProgressBar progressBar = findViewById(R.id.progressbar);

        Observable.fromIterable(pm.queryIntentActivities(mainIntent, 0))
                .sorted(new ResolveInfo.DisplayNameComparator(pm))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResolveInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNext(ResolveInfo resolveInfo) {
                        ActivityInfo info = resolveInfo.activityInfo;
                        applicationArrayList.add(new Application(
                                resolveInfo.loadLabel(pm).toString(),
                                info.packageName,
                                "0.01",
                                resolveInfo.loadIcon(pm)
                        ));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(MainActivity.this, "Complete", Toast.LENGTH_LONG).show();
                        applicationAdapter.notifyDataSetChanged();
                        applicationArrayListBackup.addAll(applicationArrayList);
                        progressBar.setVisibility(View.GONE);
                    }
                });

        whitelistedApplicationAdapter = new ApplicationAdapter(this, whitelistedApplicationList, R.layout.app_item_grid);
        whitelistedApplicationAdapter.setOnItemClicked(new ApplicationAdapter.onItemClicked() {
            @Override
            public void onHolderClick(ApplicationAdapter.ViewHolder viewHolder, final Application application) {
                whitelistedApplicationList.remove(application);
                if (whitelistedApplicationList.isEmpty()) {
                    TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                    whiteListCard.setVisibility(View.GONE);
                }
                whitelistedApplicationAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                for (int i = -1; i < applicationArrayList.size() - 1; i++) {
                    if (applicationArrayList.get(i + 1).isAfter(application)) {
                        int index = (i == -1) ? 0 : i;
                        Log.d(TAG, "Added " + application.getApplicationName() + " to " + index);
                        applicationArrayList.add(index, application);
                        applicationAdapter.notifyItemInserted(index);
                        break;
                    }
                }
            }
        });

        RecyclerView whiteListRecycler = findViewById(R.id.whitelisted_apps);
        whiteListRecycler.setAdapter(whitelistedApplicationAdapter);
        whiteListRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        whiteListCard = findViewById(R.id.whitelist_card);

        addButton = findViewById(R.id.add_to_whitelist_button);
        cancelButton = findViewById(R.id.cancel_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Lock Initiated", Toast.LENGTH_LONG).show();
                Intent lockServiceIntent = new Intent(MainActivity.this, LockService.class);
                ArrayList<String> pkgList = new ArrayList<>();
                ArrayList<String> allPkgList = new ArrayList<>();
                for (Application a : whitelistedApplicationList) {
                    pkgList.add(a.getApplicationPackageName());
                }
                for (Application a : applicationArrayList) {
                    allPkgList.add(a.getApplicationPackageName());
                }
                Log.d(TAG, "Starting LockService for 100s (Fixed)");
                lockServiceIntent.putExtra(Constants.EXTRA_WHITELISTED_APPS_PACKAGE_LIST, new Gson().toJson(pkgList));
                lockServiceIntent.putExtra(Constants.EXTRA_ALL_APPS_PACKAGE_LIST, new Gson().toJson(allPkgList));
                ContextCompat.startForegroundService(MainActivity.this, lockServiceIntent);
                supportFinishAfterTransition();
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
