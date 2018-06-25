package com.n00blife.lockit.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.n00blife.lockit.LockActivity;
import com.n00blife.lockit.R;
import com.n00blife.lockit.util.Constants;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;

public class LockService extends Service {

    private final String TAG = getClass().getSimpleName();

    private class LocalBinder extends Binder {
        LockService getService() {
            return LockService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // TODO: Fetch all application list from a Local Database Instead
    private ArrayList<String> allApplicationPackages;

    private ArrayList<String> whitelistedApplicationPackages;
    private Observable<Long> timerObservable;
    private Observer<Long> timerObserver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        allApplicationPackages = new Gson().fromJson(intent.getStringExtra(Constants.EXTRA_ALL_APPS_PACKAGE_LIST), new TypeToken<ArrayList<String>>(){}.getType());

        whitelistedApplicationPackages = new Gson().fromJson(intent.getStringExtra(Constants.EXTRA_WHITELISTED_APPS_PACKAGE_LIST), new TypeToken<ArrayList<String>>(){}.getType());

        timerObservable = Observable
                .intervalRange(0, 100, 0, 1L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io());

        initTimerObserver();

        timerObservable.subscribe(timerObserver);

        return super.onStartCommand(intent, flags, startId);
    }


    public void initTimerObserver() {
        timerObserver = new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Long aLong) {
                UsageStatsManager usm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                final SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();

                Observable.fromIterable(usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.from(getMainLooper()))
                        .subscribe(new Observer<UsageStats>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(UsageStats usageStats) {
                                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                                    // Getting the packageName of the Application which was recently used
                                    String pkg = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                                    // TODO: Determine the default launcher and prevent LockActivity to start with it.
                                    if (!whitelistedApplicationPackages.contains(pkg) && allApplicationPackages.contains(pkg)) {
                                        Intent intent = new Intent(LockService.this, LockActivity.class);
                                        intent.putExtra(Constants.EXTRA_LOCKED_APP_PACKAGE_NAME, pkg);
                                        LockService.this.startActivity(intent);
                                    }
                                }
                            }
                        });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Show up the Lockscreen damnit! :P", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "LockService Complete");
                    }
                });
                LockService.this.onDestroy();
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        publishForegroundServiceNotification();
    }

    public void publishForegroundServiceNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "lockservice";
        int notificationId = 1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId,
                    "Productivity Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            // Allow notifications to be visible even if Security Features are enabled
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // NotificationManager would pipe any incoming notifications through this channel
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, channelId)
                .setContentText("Productivity") // Notification Content
                .setContentTitle("Games FTW!")// Notification Title
                // Seems like NotificationCompat mananges to
                // Not involve Channel Stuffs in pre-26
                .setPriority(PRIORITY_MIN)
                .setChannelId(channelId) // I want to go to this channel
                .setSmallIcon(R.mipmap.ic_launcher_round) // Icon which'd appear to left of AppTitle
                .setAutoCancel(true);

        startForeground(notificationId, notification.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // What's the use of a notification, when the service behind it is about to stop
        stopForeground(true);
        stopSelf();
    }
}
