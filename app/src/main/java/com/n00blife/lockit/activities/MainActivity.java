package com.n00blife.lockit.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.n00blife.lockit.R;
import com.n00blife.lockit.adapter.ProfileAdapter;
import com.n00blife.lockit.database.ApplicationDatabase;
import com.n00blife.lockit.database.WhiteListedApplicationDatabase;
import com.n00blife.lockit.model.Profile;
import com.n00blife.lockit.services.LockService;
import com.n00blife.lockit.util.Constants;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private PackageManager pm;
    private FloatingActionButton createProfileButton;
    private RecyclerView profileListView;
    private ApplicationDatabase applicationDatabase;
    private ArrayList<Profile> profiles = new ArrayList<>();
    private ProfileAdapter adapter;
    private LinearLayout noProfileContainer;

    private void showNoProfileContainer() {
        noProfileContainer.setVisibility(View.VISIBLE);
        profileListView.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noProfileContainer = findViewById(R.id.no_profile_container);

        profileListView = findViewById(R.id.profile_recyclerview);
        profileListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        profileListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new ProfileAdapter(this, profiles);

        pm = getPackageManager();

        if (profiles.size() == 0)
            showNoProfileContainer();

        adapter.setOnItemClickedListener(new ProfileAdapter.OnItemClickedListener() {
            @Override
            public void onItemClick(int position, final Profile profile) {
                View numberPickerView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_activate_profile, null, false);
                final NumberPicker numberPicker = numberPickerView.findViewById(R.id.timer_picker);
                numberPicker.setMaxValue(30);
                numberPicker.setMinValue(1);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setView(numberPickerView)
                        .setPositiveButton("Activate", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent lockServiceIntent = new Intent(MainActivity.this, LockService.class);
                                lockServiceIntent.putExtra(Constants.EXTRA_TIMER, numberPicker.getValue());
                                lockServiceIntent.putExtra(Constants.EXTRA_PROFILE_NAME, profile.getProfileName());
                                ContextCompat.startForegroundService(MainActivity.this, lockServiceIntent);
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setTitle("Activate '" + profile.getProfileName() + "'");

                builder.create().show();
            }
        });

        profileListView.setAdapter(adapter);

        createProfileButton = findViewById(R.id.create_profile_button);

        createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileCreationActivity.class));
                finish();
            }
        });

        WhiteListedApplicationDatabase whiteListedApplicationDatabase = WhiteListedApplicationDatabase.getInstance(this);

        Observable.fromIterable(whiteListedApplicationDatabase.getProfiles())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Profile>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Profile profile) {
                        profiles.add(profile);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(MainActivity.this, "ProfileComplete", Toast.LENGTH_LONG).show();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

}
