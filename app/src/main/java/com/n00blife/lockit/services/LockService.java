package com.n00blife.lockit.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.n00blife.lockit.R;
import com.n00blife.lockit.activities.LockActivity;
import com.n00blife.lockit.database.ApplicationDatabase;
import com.n00blife.lockit.database.WhiteListedApplicationDatabase;
import com.n00blife.lockit.receiver.PackageBroadcastReceiver;
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
    private final IBinder binder = new LocalBinder();
    private ArrayList<String> allApplicationPackages;
    private ArrayList<String> whitelistedApplicationPackages;
    private Observable<Long> timerObservable;
    private Observer<Long> timerObserver;
    private PackageBroadcastReceiver packageBroadcastReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        allApplicationPackages = ApplicationDatabase.getInstance(this).getAllPackages();

        whitelistedApplicationPackages = WhiteListedApplicationDatabase.getInstance(this).getPackageListForProfile(intent.getStringExtra(Constants.EXTRA_PROFILE_NAME));

        timerObservable = Observable
                .intervalRange(0, intent.getIntExtra(Constants.EXTRA_TIMER, 1) * 60, 0, 1L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io());

        initTimerObserver();

        timerObservable.subscribe(timerObserver);

        Log.d(TAG, "Apps: " + allApplicationPackages.size());
        Log.d(TAG, "WhiteApps: " + whitelistedApplicationPackages.size());

        packageBroadcastReceiver = new PackageBroadcastReceiver();
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);

        registerReceiver(packageBroadcastReceiver, packageFilter);

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
                                    final String pkg = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                                    Intent defaultLauncherIntent = new Intent(Intent.ACTION_MAIN);
                                    defaultLauncherIntent.addCategory(Intent.CATEGORY_HOME);
                                    // If the currently open app is LockIt
                                    // TODO Show a LockActivity, but with a Auth Entry
                                    if (pkg.equals(getPackageName()))
                                        return;
                                    // If the Currently open App is the Default Launcher
                                    // Don't block it
                                    if (pkg.equals(getPackageManager().resolveActivity(defaultLauncherIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName))
                                        return;
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
                .setContentTitle("Enjoy your stay, Kid!")
                .setPriority(PRIORITY_MIN)
                .setChannelId(channelId) // I want to go to this channel
                .setSmallIcon(R.drawable.ic_001_rest_1); // Icon which'd appear to left of AppTitle

        startForeground(notificationId, notification.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(packageBroadcastReceiver);
        // What's the use of a notification, when the service behind it is about to stop
        stopForeground(true);
        stopSelf();
    }

    private class LocalBinder extends Binder {
        LockService getService() {
            return LockService.this;
        }
    }
}
