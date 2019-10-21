package com.n00blife.lockit.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputLayout;
import androidx.transition.TransitionManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.n00blife.lockit.R;
import com.n00blife.lockit.adapter.ApplicationAdapter;
import com.n00blife.lockit.database.ProfileDatabase;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.model.Profile;
import com.n00blife.lockit.util.ImageUtils;
import com.n00blife.lockit.util.MarginDividerItemDecoration;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Observer;
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
    private EditText profileNameInput;
    private TextInputLayout profileName;
    private ConstraintLayout whiteListCard;
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

        profileName = findViewById(R.id.profile_name_layout);
        profileNameInput = findViewById(R.id.profile_name_edittext);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        applicationListRecycler = findViewById(R.id.apps_list);
        applicationListRecycler.addItemDecoration(new MarginDividerItemDecoration(this));
        applicationAdapter = new ApplicationAdapter(this, applicationArrayList, R.layout.app_item);
        applicationListRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        applicationListRecycler.setAdapter(applicationAdapter);

        applicationAdapter.setOnItemClicked(new ApplicationAdapter.onItemClicked() {
            @Override
            public void onHolderClick(int position, Application application) {
                whitelistedApplicationList.add(application);
                whitelistedApplicationAdapter.notifyItemInserted(whitelistedApplicationList.size() - 1);
                applicationArrayList.remove(position);
                applicationAdapter.notifyItemRemoved(position);
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                whiteListCard.setVisibility(View.VISIBLE);
                profileName.setVisibility(View.VISIBLE);
            }
        });

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
                final String profileName = profileNameInput.getText().toString();

                if (profileName.equals("")) {
                    profileNameInput.setError("Profile Name cannot be empty");
                    return;
                }

                final ArrayList<String> pkgList = new ArrayList<>();

                Log.d(TAG, "Creating Profile " + profileName);

                for (Application a : whitelistedApplicationList) {
                    pkgList.add(a.getApplicationPackageName());
                }

                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        ProfileDatabase.getInstance(ProfileCreationActivity.this).profileDao().createProfile(new Profile(profileName, pkgList));
                    }
                });

//                if (status == -1) {
//                    profileNameInput.setError("This profile already exists");
//                } else {
                    Toast.makeText(ProfileCreationActivity.this.getApplicationContext(),
                            "Profile '" + profileName + "' created", Toast.LENGTH_LONG).show();
                    finish();
//                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(whitelistedApplicationList.size() == 0) return;

                whitelistedApplicationList.clear();
                // FIXME This is bad ;-;
                applicationArrayList = new ArrayList<>();
                applicationAdapter.notifyDataSetChanged();
                applicationListRecycler.setAdapter(null);
                applicationListRecycler.setAdapter(applicationAdapter);
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(android.R.id.content));
                whiteListCard.setVisibility(View.GONE);
                profileName.setVisibility(View.GONE);
            }
        });

        retrieveApplicationList();
    }

    public void retrieveApplicationList() {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager pm = getPackageManager();

        Observable.fromIterable(getPackageManager().queryIntentActivities(mainIntent, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResolveInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNext(ResolveInfo resolveInfo) {
                        try {
                            Application a = new Application(
                                    resolveInfo.loadLabel(getPackageManager()).toString(),
                                    resolveInfo.activityInfo.packageName,
                                    getPackageManager().getPackageInfo(resolveInfo.activityInfo.packageName, 0).versionName,
                                    ImageUtils.encodeBitmapToBase64(ImageUtils.drawableToBitmap(resolveInfo.loadIcon(getPackageManager())))
                            );
                            applicationArrayList.add(a);
                        } catch (PackageManager.NameNotFoundException nnfe) {
                            nnfe.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        progressBar.setVisibility(View.GONE);
                        applicationAdapter.notifyDataSetChanged();
                        applicationListRecycler.setVisibility(View.VISIBLE);
                    }
                });
    }
}
