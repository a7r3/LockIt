package nooblife.lockit.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.leanback.widget.picker.PinPicker;

import nooblife.lockit.R;
import nooblife.lockit.services.LockService;
import nooblife.lockit.tv.TvMainActivity;
import nooblife.lockit.util.Constants;
import nooblife.lockit.util.Utils;

public class LockActivity extends Activity {

    private TextView applicationName;
    private ImageView applicationIcon;
    private TextView toggleTemporaryUnlockRequest;
    private TextView toggleEmergencyUnlockDialog;
    private String applicationPkg;
    private ApplicationInfo info;
    private View temporaryUnlockDialog;
    private View emergencyUnlockDialog;
    private PinPicker emergencyUnlockPinView;
    private boolean isEmergencyUnlockRequested = false;
    private boolean isTemporaryUnlockRequested = false;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (applicationPkg.equals(getPackageName())) {
                isTemporaryUnlockRequested = true;
                sendBroadcast(new Intent(Constants.ACTION_UNLOCK_MAINAPP));
            }
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
        applicationPkg = getIntent().getStringExtra(Constants.EXTRA_LOCKED_PKGNAME);
        applyLockedAppDetails();
    }

    private void applyLockedAppDetails() {
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
        toggleEmergencyUnlockDialog = findViewById(R.id.emergency_unlock_button);
        temporaryUnlockDialog = findViewById(R.id.temp_unlock_dialog);
        emergencyUnlockDialog = findViewById(R.id.emergency_unlock_dialog);
        emergencyUnlockPinView = findViewById(R.id.emergency_unlock_code_text);

        toggleTemporaryUnlockRequest.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) getWindow().getDecorView());
            if (!isTemporaryUnlockRequested) {
                temporaryUnlockDialog.setVisibility(View.VISIBLE);
                toggleTemporaryUnlockRequest.setText("Cancel");
                toggleEmergencyUnlockDialog.setVisibility(View.GONE);
            } else {
                temporaryUnlockDialog.setVisibility(View.GONE);
                toggleTemporaryUnlockRequest.setText("Unlock for once");
                toggleEmergencyUnlockDialog.setVisibility(View.VISIBLE);
            }
            emergencyUnlockDialog.setVisibility(View.GONE);
            isTemporaryUnlockRequested = !isTemporaryUnlockRequested;
        });

        toggleEmergencyUnlockDialog.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) getWindow().getDecorView());
            if (!isEmergencyUnlockRequested) {
                emergencyUnlockDialog.setVisibility(View.VISIBLE);
                toggleEmergencyUnlockDialog.setText("Cancel");
                toggleTemporaryUnlockRequest.setVisibility(View.GONE);
                emergencyUnlockPinView.setActivated(true);
                emergencyUnlockPinView.requestFocus();
            } else {
                emergencyUnlockDialog.setVisibility(View.GONE);
                toggleEmergencyUnlockDialog.setText("Emergency Unlock");
                toggleTemporaryUnlockRequest.setVisibility(View.VISIBLE);
            }
            temporaryUnlockDialog.setVisibility(View.GONE);
            isEmergencyUnlockRequested = !isEmergencyUnlockRequested;
        });

        emergencyUnlockPinView.setOnClickListener(v -> {
            if (Utils.isTheCodeRight(LockActivity.this, emergencyUnlockPinView.getPin())) {
                Utils.resetApp(LockActivity.this);
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(LockActivity.this, "Unlock code incorrect. Try again", Toast.LENGTH_SHORT).show();
                emergencyUnlockPinView.resetPin();
            }

        });

        applicationPkg = getIntent().getStringExtra(Constants.EXTRA_LOCKED_PKGNAME);

        if (applicationPkg.equals(getPackageName())) {
            getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        applyLockedAppDetails();
    }
}
