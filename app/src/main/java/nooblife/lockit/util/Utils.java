package nooblife.lockit.util;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
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
                .observeOn(AndroidSchedulers.mainThread())
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

    public static void startLockService(Context context) {
        // This method would be either called by Remote Locker OR In-App UI
        // So, the user explicitly calls LockService
        BlacklistDatabase.getInstance(context).blacklistDao().setServiceActive(true);
        startLockService(context, "");
    }

    public static void startLockService(Context context, String action) {
        // This method is ONLY to be called by BOOT_COMPLETED broadcast receiver
        // Service is not to be set as active here since it's not explicit
        // Though, we'll check if the service was running on last boot, and restart it again
        //   if that was the case
        Intent lockServiceIntent = new Intent(context, LockService.class);
        lockServiceIntent.setAction(context.getPackageName() + action);
        ContextCompat.startForegroundService(context, lockServiceIntent);
    }

    public interface AppRetrivalInterface {
        void onProgress();

        void onComplete(List<Application> applications);
    }

    public static void exitToLauncher(Context context) {
        Intent exitToLauncher = new Intent(Intent.ACTION_MAIN);
        exitToLauncher.addCategory(Intent.CATEGORY_HOME);
        exitToLauncher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(exitToLauncher);
    }
}