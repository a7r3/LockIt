package nooblife.lockit.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.github.druk.rx2dnssd.BonjourService;
import com.github.druk.rx2dnssd.Rx2Dnssd;
import com.github.druk.rx2dnssd.Rx2DnssdBindable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nooblife.lockit.R;
import nooblife.lockit.activities.LockActivity;
import nooblife.lockit.activities.PostLockdownActivity;
import nooblife.lockit.database.BlacklistDatabase;
import nooblife.lockit.util.Constants;
import nooblife.lockit.util.LockItServer;
import nooblife.lockit.util.Utils;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class LockService extends Service {

    private final String TAG = getClass().getSimpleName();
    private final IBinder binder = new LocalBinder();
    private ArrayList<String> allApplicationPackages = new ArrayList<>();
    private List<String> blackList;
    private Observable<Long> timerObservable;
    private Observer<Long> timerObserver;
    private Disposable timerDisposable;
    private SharedPreferences sharedPreferences;

    private void showToast(final String text) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(LockService.this, text, Toast.LENGTH_LONG).show()
        );
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {

            if (intent.getAction().equalsIgnoreCase(Constants.ACTION_RESTART_LOCKSERVICE)) {
                Log.d(TAG, "onStartCommand: called on BOOT_COMPLETED broadcast");
                Log.d(TAG, "onStartCommand: Checking if service was running on last boot");
                boolean hasToBeRestarted = BlacklistDatabase.getInstance(this).blacklistDao().isServiceActiveOnLastBoot();
                if (!hasToBeRestarted) {
                    Log.d(TAG, "onStartCommand: Destroying Service: since it wasn't running on last boot");
                    LockService.this.onDestroy();
                    return START_NOT_STICKY;
                }
            }

            Log.d(TAG, "onStartCommand: Initializing LockIt Server");
            LockItServer.initialize(this, new LockItServer.ServerEventListener() {
                @Override
                public void onLock() {
                    startLock();
                }

                @Override
                public void onUnlock() {
                    stopLock();
                }

                @Override
                public void onPair() {

                }
            }).start();

            // Lock the Device now
            startLock();
        }

        return START_NOT_STICKY;
    }


    private void startLockOps() {
        Log.d(TAG, "startLockOps: Started Lock Service");

        Utils.exitToLauncher(this);

        Observable.fromIterable(getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(applicationInfo -> allApplicationPackages.add(applicationInfo.packageName))
                .doOnComplete(() -> {
                    blackList = BlacklistDatabase
                            .getInstance(LockService.this)
                            .blacklistDao()
                            .getBlacklist()
                            .getPackageList();

                    timerObservable = Observable
                            .interval(0, 1L, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io());

                    initTimerObserver();

                    timerObservable.subscribe(timerObserver);
                })
                .subscribe();

    }

    public void initTimerObserver() {

        timerObserver = new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                timerDisposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                checkCurrentlyRunningAppAndLock();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                // complete? never
                new Handler(getMainLooper()).post(() -> {
                    startActivity(new Intent(LockService.this, PostLockdownActivity.class));
                    Log.d(TAG, "LockService Complete");
                });
            }
        };
    }

    private void checkCurrentlyRunningAppAndLock() {
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
                            // if (pkg.equals(getPackageName()))
                            // TODO Show a LockActivity, but with a Auth Entry
                            //    return;
                            // If the Currently open App is the Default Launcher
                            // Don't block it
                            if (pkg.equals(getPackageManager().resolveActivity(defaultLauncherIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName))
                                return;

                            if (blackList.contains(pkg) && allApplicationPackages.contains(pkg)) {
                                Intent intent = new Intent(LockService.this, LockActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra(Constants.EXTRA_LOCKED_APP_PACKAGE_NAME, pkg);
                                LockService.this.startActivity(intent);
                            }
                        }
                    }
                });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        publishForegroundServiceNotification();
    }

    public void publishForegroundServiceNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "lockservice";
        int notificationId = 69;

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

    private void stopLock() {
        showToast("Device is Unlocked");
        BlacklistDatabase.getInstance(this).blacklistDao().setServiceActive(false);
        sendBroadcast(new Intent(Constants.ACTION_STOP_LOCKACTIVITY));
        timerDisposable.dispose();
    }

    private void startLock() {
        showToast("Device is Locked");
        BlacklistDatabase.getInstance(this).blacklistDao().setServiceActive(true);
        startLockOps();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
