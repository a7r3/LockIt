package com.n00blife.lockit.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.transition.TransitionManager;
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

import com.n00blife.lockit.R;
import com.n00blife.lockit.adapter.ApplicationAdapter;
import com.n00blife.lockit.database.ApplicationDatabase;
import com.n00blife.lockit.database.WhiteListedApplicationDatabase;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.util.ImageUtils;
import com.n00blife.lockit.util.MarginDividerItemDecoration;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ProfileCreationActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private ArrayList<Application> applicationArrayList = new ArrayList<>();
    private ArrayList<Application> whitelistedApplicationList = new ArrayList<>();

    private ApplicationAdapter applicationAdapter;
    private ApplicationAdapter whitelistedApplicationAdapter;
    private RecyclerView applicationListRecycler;
    private ConstraintLayout whiteListCard;
    private ApplicationDatabase applicationDatabase;
    private Button addButton;
    private TextView cancelButton;
    private PackageManager pm;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creator);

        pm = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        applicationListRecycler = findViewById(R.id.apps_list);
        applicationListRecycler.addItemDecoration(new MarginDividerItemDecoration(this));
        applicationAdapter = new ApplicationAdapter(this, applicationArrayList, R.layout.app_item);
        applicationAdapter.setOnItemClicked(new ApplicationAdapter.onItemClicked() {
            @Override
            public void onHolderClick(int position, Application application) {
                applicationArrayList.remove(application);
                applicationAdapter.notifyItemRemoved(position);
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                whiteListCard.setVisibility(View.VISIBLE);
                whitelistedApplicationList.add(application);
                whitelistedApplicationAdapter.notifyItemInserted(whitelistedApplicationList.size() - 1);
            }
        });

        applicationListRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        applicationListRecycler.setAdapter(applicationAdapter);

        final ProgressBar progressBar = findViewById(R.id.progressbar);

        whitelistedApplicationAdapter = new ApplicationAdapter(this, whitelistedApplicationList, R.layout.app_item_grid);
        whitelistedApplicationAdapter.setOnItemClicked(new ApplicationAdapter.onItemClicked() {
            @Override
            public void onHolderClick(int position, Application application) {

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
                ArrayList<String> pkgList = new ArrayList<>();
                Log.d(TAG, "Creating Profile " + "Foo");
                for(Application a : whitelistedApplicationList) {
                    pkgList.add(a.getApplicationPackageName());
                }
                // Adding Default Launcher to the whitelist
                WhiteListedApplicationDatabase.getInstance(ProfileCreationActivity.this).createProfile("DefaultProfile", pkgList);
                startActivity(new Intent(ProfileCreationActivity.this, MainActivity.class));
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whitelistedApplicationList.clear();
                applicationArrayList.clear();
                applicationAdapter.notifyDataSetChanged();
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                whiteListCard.setVisibility(View.GONE);
            }
        });

        applicationDatabase = ApplicationDatabase.getInstance(this);

        // Retrieve a list of installed applications if this App is opened for the first time
        // One-Time process
        if(applicationDatabase.getRowCount() == 0)
            retrieveApplicationList();
        else
            retrieveApplicationListFromDatabase();

    }

    public void retrieveApplicationListFromDatabase() {
        Observable.fromIterable(ApplicationDatabase.getInstance(this).getAllApplications())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Application>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNext(Application application) {
                        applicationArrayList.add(application);
                        applicationAdapter.notifyItemInserted(applicationArrayList.size());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileCreationActivity.this, "Complete", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // TODO Do this retrieval during the start of the Application Itself
    public void retrieveApplicationList() {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        Observable.fromIterable(pm.queryIntentActivities(mainIntent, 0))
                .sorted(new ResolveInfo.DisplayNameComparator(pm))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResolveInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        progressBar.setVisibility(View.VISIBLE);
                        applicationListRecycler.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(ResolveInfo resolveInfo) {
                        try {
                            Application a = new Application(
                                    resolveInfo.loadLabel(pm).toString(),
                                    resolveInfo.activityInfo.packageName,
                                    pm.getPackageInfo(resolveInfo.activityInfo.packageName, 0).versionName,
                                    ImageUtils.drawableToBitmap(resolveInfo.loadIcon(pm))
                            );
                            applicationDatabase.addApplication(a);
                            // TODO Remove this call when moving this method out of this Activity (As mentioned in above TODO (1)
                            applicationArrayList.add(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        // TODO Remove this call when moving this method out of this Activity (As mentioned in above TODO (2)
                        applicationAdapter.notifyDataSetChanged();
                        // TODO Remove this call when moving this method out of this Activity (As mentioned in above TODO (3)
                        applicationListRecycler.setVisibility(View.VISIBLE);
                        // TODO Remove this call when moving this method out of this Activity (As mentioned in above TODO (4)
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

}
