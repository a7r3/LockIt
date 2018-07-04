package com.n00blife.lockit.activities;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.n00blife.lockit.R;
import com.n00blife.lockit.adapter.IntroAdapter;
import com.n00blife.lockit.database.ApplicationDatabase;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.util.ImageUtils;
import com.n00blife.lockit.util.IntroPagerTransformer;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ProgressBar progressBar;
    private Button getStartedButton;
    private ViewGroup parent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        viewPager = findViewById(R.id.intro_viewpager);

        viewPager.setAdapter(new IntroAdapter(getSupportFragmentManager()));

        viewPager.setPageTransformer(false, new IntroPagerTransformer());

        progressBar = findViewById(R.id.loader_progress);

        getStartedButton = findViewById(R.id.get_started_button);

        parent = findViewById(android.R.id.content);

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
            }
        });

        if (ApplicationDatabase.getInstance(this).getRowCount() == 0)
            retrieveApplicationList();
        else {
            TransitionManager.beginDelayedTransition(parent);
            progressBar.setVisibility(View.GONE);
            getStartedButton.setVisibility(View.VISIBLE);
        }

        viewPager.setOffscreenPageLimit(4);
        TabLayout tabLayout = findViewById(R.id.intro_tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void retrieveApplicationList() {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final ApplicationDatabase applicationDatabase = ApplicationDatabase.getInstance(this);

        Observable.fromIterable(getPackageManager().queryIntentActivities(mainIntent, 0))
                .sorted(new ResolveInfo.DisplayNameComparator(getPackageManager()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResolveInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        TransitionManager.beginDelayedTransition(parent);
                        progressBar.setVisibility(View.VISIBLE);
                        getStartedButton.setVisibility(View.GONE);
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
                            applicationDatabase.addApplication(a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
