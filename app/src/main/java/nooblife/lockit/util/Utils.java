package nooblife.lockit.util;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nooblife.lockit.activities.LockActivity;
import nooblife.lockit.database.BlacklistDatabase;
import nooblife.lockit.model.Application;
import nooblife.lockit.model.Blacklist;
import nooblife.lockit.services.LockService;

import static android.content.Context.UI_MODE_SERVICE;

public class Utils {
    /**
     * From https://stackoverflow.com/a/9563438
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static boolean isRunningOnTv(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static void retrieveApplicationList(final Context context, final AppRetrivalInterface appRetrivalInterface) {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        if (Utils.isRunningOnTv(context))
            mainIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        else
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager pm = context.getPackageManager();

        final ArrayList<Application> applicationArrayList = new ArrayList<>();

        Observable.fromIterable(pm.queryIntentActivities(mainIntent, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<ResolveInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        appRetrivalInterface.onProgress();
                    }

                    @Override
                    public void onNext(ResolveInfo resolveInfo) {
                        try {
                            Application a = new Application(
                                    resolveInfo.loadLabel(pm).toString(),
                                    resolveInfo.activityInfo.packageName,
                                    pm.getPackageInfo(resolveInfo.activityInfo.packageName, 0).versionName,
                                    ImageUtils.encodeBitmapToBase64(ImageUtils.drawableToBitmap(resolveInfo.loadIcon(pm)))
                            );
                            if (!a.getApplicationPackageName().equals(context.getPackageName()))
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
                        appRetrivalInterface.onComplete(applicationArrayList);
                    }
                });
    }

    public static List<Application> applyBlacklistData(Context context, List<Application> applications) {
        Blacklist blacklist = BlacklistDatabase.getInstance(context)
                .blacklistDao()
                .getBlacklist();

        if (blacklist == null)
            return applications;

        List<String> blacklistedApps = blacklist.getPackageList();

        for (Application a : applications) {
            if (blacklistedApps.contains(a.getApplicationPackageName()))
                a.setSelected(true);
            else
                a.setSelected(false);
        }

        return applications;
    }

    public static void startLockService(Context context, String action) {
        Intent lockServiceIntent = new Intent(context, LockService.class);
        lockServiceIntent.setAction(action);
        ContextCompat.startForegroundService(context, lockServiceIntent);
    }

    public static boolean isLockServiceRunning(Context context) {
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(LockService.class.getName())){
                return true;
            }
        }
        return false;
    }

    public static void setLockerActive(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Constants.PREF_LOCKER_ACTIVE, true)
                .apply();
    }

    public static void setLockerInactive(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Constants.PREF_LOCKER_ACTIVE, false)
                .apply();
    }

    public static boolean isLockerActive(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.PREF_LOCKER_ACTIVE, false);
    }

    public static void setEmergencyUnlockCode(Context context, String code) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(Constants.PREF_EMERGENCY_UNLOCK_CODE, code)
                .apply();
    }

    public static boolean isTheCodeRight(Context context, String code) {
        return code.equals(
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(Constants.PREF_EMERGENCY_UNLOCK_CODE, "0000")
        );
    }

    public static void resetApp(Context context) {
        startLockService(context, Constants.ACTION_EMERGENCY_UNLOCK_TRIGGERED);
        setEmergencyUnlockCode(context, "0000");
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().clear().apply();
    }

    public static void exitToLauncher(Context context) {
        Intent exitToLauncher = new Intent(Intent.ACTION_MAIN);
        exitToLauncher.addCategory(Intent.CATEGORY_HOME);
        exitToLauncher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(exitToLauncher);
    }

    public interface AppRetrivalInterface {
        void onProgress();

        void onComplete(List<Application> applications);
    }
}
