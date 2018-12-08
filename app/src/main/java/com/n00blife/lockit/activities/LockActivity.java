package com.n00blife.lockit.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.n00blife.lockit.R;

public class LockActivity extends AppCompatActivity {

    private TextView applicationName;
    private ImageView applicationIcon;
    private TextView exitButton;
    private String applicationPkg;
    private ApplicationInfo info;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent exitToLauncher = new Intent(Intent.ACTION_MAIN);
        exitToLauncher.addCategory(Intent.CATEGORY_HOME);
        exitToLauncher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(exitToLauncher);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

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
