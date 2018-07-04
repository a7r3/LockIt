package com.n00blife.lockit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;

import com.n00blife.lockit.database.ApplicationDatabase;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.util.ImageUtils;

public class PackageBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;

        ApplicationDatabase database = ApplicationDatabase.getInstance(context);

        switch (intent.getAction()) {
            case Intent.ACTION_PACKAGE_ADDED:
                int packageUid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                if (packageUid == -1) break;
                String[] packages = context.getPackageManager().getPackagesForUid(packageUid);
                if (packages == null) break;
                String installedPackage = packages[0];
                try {
                    ApplicationInfo installedAppInfo = context.getPackageManager().getApplicationInfo(installedPackage, 0);
                    String appLabel = installedAppInfo.loadLabel(context.getPackageManager()).toString();
                    String appIcon64 = ImageUtils.encodeBitmapToBase64(ImageUtils.drawableToBitmap(installedAppInfo.loadIcon(context.getPackageManager())));
                    String appVersion = context.getPackageManager().getPackageInfo(installedPackage, 0).versionName;
                    database.addApplication(new Application(
                            appLabel,
                            installedPackage,
                            appVersion,
                            appIcon64
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Intent.ACTION_PACKAGE_CHANGED:
            case Intent.ACTION_PACKAGE_REMOVED:
        }
    }
}
