package com.n00blife.lockit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.transition.TransitionManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.n00blife.lockit.R;
import com.n00blife.lockit.adapter.IntroAdapter;
import com.n00blife.lockit.database.RoomApplicationDatabase;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.util.ImageUtils;
import com.n00blife.lockit.util.IntroPagerTransformer;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ProgressBar progressBar;
    private Button getStartedButton;
    private ViewGroup parent;
    private String IS_INTRO_COMPLETE = "intro_complete";
    private int i = 0;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(IntroActivity.this);

        if(preferences.getBoolean(IS_INTRO_COMPLETE, false)) {
            startActivity(new Intent(IntroActivity.this, MainActivity.class));
            finish();
        }

        viewPager = findViewById(R.id.intro_viewpager);

        viewPager.setAdapter(new IntroAdapter(getSupportFragmentManager()));

        viewPager.setPageTransformer(false, new IntroPagerTransformer());

        Observable.interval(0, 3, TimeUnit.SECONDS)
                .take(4)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "AutoViewPagerScroll: Started");
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.d(TAG, "AutoViewPagerScroll: Page " + i);
                        viewPager.setCurrentItem(i++, true);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "AutoViewPagerScroll: Complete");
                    }
                });

        viewPager.setCurrentItem(0, true);

        progressBar = findViewById(R.id.loader_progress);

        getStartedButton = findViewById(R.id.get_started_button);

        parent = findViewById(android.R.id.content);

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
                finish();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(IntroActivity.this);
                preferences.edit()
                        .putBoolean(IS_INTRO_COMPLETE, true)
                        .apply();
            }
        });

        RoomApplicationDatabase.getInstance(this).applicationDao().getNumberOfRows()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        if (integer == 0)
                            retrieveApplicationList();
                        else {
                            TransitionManager.beginDelayedTransition(parent);
                            progressBar.setVisibility(View.GONE);
                            getStartedButton.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

        TabLayout tabLayout = findViewById(R.id.intro_tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void retrieveApplicationList() {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final RoomApplicationDatabase db = RoomApplicationDatabase.getInstance(this);

        Observable.fromIterable(getPackageManager().queryIntentActivities(mainIntent, 0))
                .sorted(new ResolveInfo.DisplayNameComparator(getPackageManager()))
                .subscribeOn(Schedulers.io())
                // RxJava FTW <3 <3
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<ResolveInfo>() {
                    @Override
                    public void accept(final ResolveInfo resolveInfo) throws Exception {
                        try {
                            Application a = new Application(
                                    resolveInfo.loadLabel(getPackageManager()).toString(),
                                    resolveInfo.activityInfo.packageName,
                                    getPackageManager().getPackageInfo(resolveInfo.activityInfo.packageName, 0).versionName,
                                    ImageUtils.encodeBitmapToBase64(ImageUtils.drawableToBitmap(resolveInfo.loadIcon(getPackageManager())))
                            );
                            db.applicationDao().addApplication(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                // RxAndroid FTW
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResolveInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        TransitionManager.beginDelayedTransition(parent);
                        progressBar.setVisibility(View.VISIBLE);
                        getStartedButton.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(final ResolveInfo resolveInfo) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        TransitionManager.beginDelayedTransition(parent);
                        progressBar.setVisibility(View.GONE);
                        getStartedButton.setVisibility(View.VISIBLE);
                    }
                });
    }

}
