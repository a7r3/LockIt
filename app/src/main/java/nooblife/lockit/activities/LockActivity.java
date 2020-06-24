package nooblife.lockit.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import nooblife.lockit.R;
import nooblife.lockit.util.Constants;
import nooblife.lockit.util.Utils;

public class LockActivity extends AppCompatActivity {

    private TextView applicationName;
    private ImageView applicationIcon;
    private TextView exitButton;
    private String applicationPkg;
    private ApplicationInfo info;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.exitToLauncher(this);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        registerReceiver(receiver, new IntentFilter(Constants.ACTION_STOP_LOCKACTIVITY));

        overridePendingTransition(0, 0);

        applicationPkg = getIntent().getStringExtra("APP");

        applicationName = findViewById(R.id.text_error_content);
        applicationIcon = findViewById(R.id.application_icon);
        exitButton = findViewById(R.id.exit_button);

        try {
            info = getPackageManager().getApplicationInfo(applicationPkg, 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            applicationName.setText(info.loadLabel(getPackageManager()));
            applicationIcon.setImageDrawable(getPackageManager().getApplicationIcon(info));
        }

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LockActivity.this.onBackPressed();
            }
        });

    }
}
