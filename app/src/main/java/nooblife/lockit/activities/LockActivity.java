package nooblife.lockit.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import nooblife.lockit.R;
import nooblife.lockit.util.Constants;
import nooblife.lockit.util.Utils;

public class LockActivity extends Activity {

    private TextView applicationName;
    private ImageView applicationIcon;
    private TextView toggleTemporaryUnlockRequest;
    private String applicationPkg;
    private ApplicationInfo info;
    private View temporaryUnlockDialog;
    private boolean isTemporaryUnlockRequested = false;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.exitToLauncher(this);
        finish();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        // Resume the Timer
        Intent intent = new Intent(Constants.ACTION_RESUME_TIMERTASK);
        intent.putExtra(Constants.EXTRA_TEMPORARY_UNLOCK_REQUESTED, isTemporaryUnlockRequested);
        sendBroadcast(intent);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(Constants.ACTION_LOCKACTIVITY_STATUSREPORT);
        intent.putExtra(Constants.EXTRA_IS_LOCKACTIVITY_ONTOP, false);
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(Constants.ACTION_LOCKACTIVITY_STATUSREPORT);
        intent.putExtra(Constants.EXTRA_IS_LOCKACTIVITY_ONTOP, true);
        sendBroadcast(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        applyLockedAppDetails(intent);
    }

    private void applyLockedAppDetails(Intent intent) {
        applicationPkg = intent.getStringExtra("APP");

        try {
            info = getPackageManager().getApplicationInfo(applicationPkg, 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            applicationName.setText(info.loadLabel(getPackageManager()));
            Drawable appIcon = getPackageManager().getApplicationBanner(info);
            if (appIcon == null) {
                appIcon = getPackageManager().getApplicationIcon(info);
            }
            applicationIcon.setImageDrawable(appIcon);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        registerReceiver(receiver, new IntentFilter(Constants.ACTION_STOP_LOCKACTIVITY));

        overridePendingTransition(0, 0);

        applicationName = findViewById(R.id.text_error_content);
        applicationIcon = findViewById(R.id.application_icon);
        toggleTemporaryUnlockRequest = findViewById(R.id.temp_unlock_request_button);
        temporaryUnlockDialog = findViewById(R.id.temp_unlock_dialog);

        toggleTemporaryUnlockRequest.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) getWindow().getDecorView());
            if (!isTemporaryUnlockRequested) {
                temporaryUnlockDialog.setVisibility(View.VISIBLE);
                toggleTemporaryUnlockRequest.setText("Cancel");
            } else {
                temporaryUnlockDialog.setVisibility(View.GONE);
                toggleTemporaryUnlockRequest.setText("Unlock for once");
            }
            isTemporaryUnlockRequested = !isTemporaryUnlockRequested;
        });

        applyLockedAppDetails(getIntent());
    }
}
