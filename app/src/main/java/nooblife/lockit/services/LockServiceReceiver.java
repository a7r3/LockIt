package nooblife.lockit.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import nooblife.lockit.util.Constants;
import nooblife.lockit.util.Utils;

public class LockServiceReceiver extends BroadcastReceiver {
    private void showToast(final Context context, final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
            Utils.startLockService(context, Constants.ACTION_RESTART_LOCKSERVICE);
    }
}
