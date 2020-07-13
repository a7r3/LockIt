package nooblife.lockit.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nooblife.lockit.R;
import nooblife.lockit.activities.LockActivity;
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
    private Disposable timerDisposable;
    private SharedPreferences sharedPreferences;
    private boolean isLockActivityRunning = false;
    // If a user unlocks a foreverLockedApp, the lock mechanism will be bypassed
    // until the user moves out of that foreverLockedApp
    private boolean isInTemporaryUnlockMode = false;
    private String packageInTemporaryUnlock = "";
    private BroadcastReceiver lockActivityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch(intent.getAction()) {
                    case Constants.ACTION_LOCKACTIVITY_STATUSREPORT:
                        isLockActivityRunning = intent.getBooleanExtra(Constants.EXTRA_IS_LOCKACTIVITY_ONTOP, true);
                        break;
                    case Constants.ACTION_RESUME_TIMERTASK:
                        if (intent.getBooleanExtra(Constants.EXTRA_TEMPORARY_UNLOCK_REQUESTED, false)) {
                            isInTemporaryUnlockMode = true;
                            startLock();
                        } else if (BlacklistDatabase.getInstance(context).blacklistDao().isServiceActive())
                            startOrResumeTimer();
                        break;
                }
            }
        }
    };
    // Flip this to 'true' if you want to test this in emulator
    // Broadcast intents as mentioned in the below receiver to Lock/Unlock from ADB AM Broadcasts.
    // SHOULD BE FALSE FOR ACTUAL TESTS
    private final boolean receiveDebugBroadcasts = true;
    private BroadcastReceiver debugCommandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch(intent.getAction()) {
                    case Constants.DEBUG_LOCKSERVICE_START:
                        startLock();
                        break;
                    case Constants.DEBUG_LOCKSERVICE_STOP:
                        stopLock();
                        break;
                }
            }
        }
    };

    public static List<String> foreverLockedApps = Arrays.asList(
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.android.tv.settings",
            "nooblife.lockit"
    );

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {

            if (intent.getAction().equalsIgnoreCase(getPackageName() + Constants.ACTION_RESTART_LOCKSERVICE)) {
                Log.d(TAG, "onStartCommand: called on BOOT_COMPLETED broadcast");
                Log.d(TAG, "onStartCommand: Checking if service was running on last boot");
                boolean hasToBeRestarted = BlacklistDatabase.getInstance(this).blacklistDao().isServiceActive();
                if (!hasToBeRestarted) {
                    Log.d(TAG, "onStartCommand: Destroying Service: since it wasn't running on last boot");
                    LockService.this.onDestroy();
                    return START_STICKY;
                }
                BlacklistDatabase.getInstance(this).blacklistDao().setServiceActive(false);
            }

            Log.d(TAG, "onStartCommand: Initializing LockIt Server");
            LockItServer.initialize(this)
                    .onLock(this::startLock)
                    .onUnlock(this::stopLock)
                    .start();

            // Lock the Device now
            coldStartLock();
        }

        return START_STICKY;
    }

    // Initial preparations before locking/unlocking normally
    // Only called when LockService is just started
    private void coldStartLock() {
        if (BlacklistDatabase.getInstance(this).blacklistDao().isServiceActive()) {
            Log.e(TAG, "startLock: Service is already running");
            return;
        }
        Utils.exitToLauncher(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_RESUME_TIMERTASK);
        filter.addAction(Constants.ACTION_LOCKACTIVITY_STATUSREPORT);
        registerReceiver(lockActivityBroadcastReceiver, filter);

        if (receiveDebugBroadcasts) {
            IntentFilter debugCommandFilter = new IntentFilter();
            debugCommandFilter.addAction(Constants.DEBUG_LOCKSERVICE_START);
            debugCommandFilter.addAction(Constants.DEBUG_LOCKSERVICE_STOP);
            registerReceiver(debugCommandReceiver, debugCommandFilter);
        }
        startLock();
    }

    // Just start the lock without the initial setup (for Temporary Unlocks ONLY)
    private void startLock() {
        BlacklistDatabase.getInstance(this).blacklistDao().setServiceActive(true);
        String message = isInTemporaryUnlockMode
                ? "Device will be locked again once you leave the current App"
                : "Device is Locked";
        showToast(message);
        loadInstalledAppsAndStartTimer();
    }

    private void loadInstalledAppsAndStartTimer() {
        Log.d(TAG, "startLockOps: Started Lock Service");

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
                            .interval(0, 100L, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io());

                    startOrResumeTimer();

                }).subscribe();

    }

    private void startOrResumeTimer() {
        pauseTimer();
        timerDisposable = timerObservable.subscribe(
                next -> checkCurrentlyRunningAppAndLock(),
                Throwable::printStackTrace
        );
    }

    private void checkCurrentlyRunningAppAndLock() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        UsageEvents usageEvents = usm.queryEvents(now - 30 * 1000, now + (10 * 1000));
        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
        }

        // TODO Replace MOVE_TO_FOREGROUND by ACTIVITY_RESUME when bumping targetSdk to 29
        if (TextUtils.isEmpty(event.getPackageName()) || event.getEventType() != UsageEvents.Event.MOVE_TO_FOREGROUND) {
            return;
        }

        final String pkg = event.getPackageName();
        Intent defaultLauncherIntent = new Intent(Intent.ACTION_MAIN);
        defaultLauncherIntent.addCategory(Intent.CATEGORY_HOME);
        // If the currently open app is Lockit->LockActivity
        // Don't block it (we'd end up blocking LockActivity with another LockActivity blocking another LockActivity...)
        if (pkg.equals(getPackageName()) && isLockActivityRunning) {
            return;
        }

        // Temporary Unlock Core Logic
        if (isInTemporaryUnlockMode) {
            if (packageInTemporaryUnlock.isEmpty()) {
                // Getting the temporarily unlocked package
                packageInTemporaryUnlock = pkg;
                return;
            }
            if (!packageInTemporaryUnlock.equals(pkg)) {
                // Moved out of ForeverLockedApp? Back to normal!
                isInTemporaryUnlockMode = false;
                packageInTemporaryUnlock = "";
            } else {
                // Still inside the temporarily unlocked app
                // Don't lock
                return;
            }
        }

        // If the Currently open App is the Default Launcher
        // Don't block it
        if (pkg.equals(getPackageManager().resolveActivity(defaultLauncherIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName))
            return;

        boolean isToBeBlocked = false;

        if (foreverLockedApps.contains(pkg)) {
            isToBeBlocked = true;
        } else if (BlacklistDatabase.getInstance(LockService.this).blacklistDao().isServiceActive()) {
            if (blackList.contains(pkg) && allApplicationPackages.contains(pkg)) {
                isToBeBlocked = true;
            }
        }

        if (isToBeBlocked) {
            Intent intent = new Intent(LockService.this, LockActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.EXTRA_LOCKED_APP_PACKAGE_NAME, pkg);
            pauseTimer();
            LockService.this.startActivity(intent);
        }
    }

    // Timer will be paused (no foreground activity checking) when LockActivity is open
    private void pauseTimer() {
        if (timerDisposable != null && !timerDisposable.isDisposed())
            timerDisposable.dispose();
    }

    // timer will run only for foreverLockedApps (not for user-specified apps)
    private void stopLock() {
        if (!BlacklistDatabase.getInstance(this).blacklistDao().isServiceActive()) {
            Log.e(TAG, "stopLock: Service is already stopped");
            return;
        }
        showToast("Device is Unlocked");
        BlacklistDatabase.getInstance(this).blacklistDao().setServiceActive(false);
        // unregisterReceiver(lockActivityBroadcastReceiver);
        sendBroadcast(new Intent(Constants.ACTION_STOP_LOCKACTIVITY));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(lockActivityBroadcastReceiver);
        // What's the use of a notification, when the service behind it is about to stop
        LockItServer.initialize(this).stop();
        stopForeground(true);
        stopSelf();
    }

    private class LocalBinder extends Binder {
        LockService getService() {
            return LockService.this;
        }
    }

    private void showToast(final String text) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(LockService.this, text, Toast.LENGTH_LONG).show()
        );
    }
}
