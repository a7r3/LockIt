package com.n00blife.lockit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.n00blife.lockit.util.IntroPagerTransformer;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private Button getStartedButton;
    private ViewGroup parent;
    private String IS_INTRO_COMPLETE = "intro_complete";
    private int i = 0;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(preferences.getBoolean(IS_INTRO_COMPLETE, false)) {
            startActivity(new Intent(this.getApplicationContext(), MainActivity.class));
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

        getStartedButton = findViewById(R.id.get_started_button);
        ProgressBar progressBar = findViewById(R.id.loader_progress);

        parent = findViewById(android.R.id.content);

        TransitionManager.beginDelayedTransition(parent);
        progressBar.setVisibility(View.GONE);
        getStartedButton.setVisibility(View.VISIBLE);


        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntroActivity.this.getApplicationContext(), MainActivity.class));
                finish();
                preferences.edit()
                        .putBoolean(IS_INTRO_COMPLETE, true)
                        .apply();
            }
        });

        TabLayout tabLayout = findViewById(R.id.intro_tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

}
