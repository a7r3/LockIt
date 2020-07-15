package nooblife.lockit.tv;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import nooblife.lockit.R;
import nooblife.lockit.activities.ConnectActivity;
import nooblife.lockit.activities.LockActivity;
import nooblife.lockit.adapter.ApplicationAdapter;
import nooblife.lockit.database.BlacklistDatabase;
import nooblife.lockit.model.Application;
import nooblife.lockit.model.Blacklist;
import nooblife.lockit.services.LockService;
import nooblife.lockit.util.Constants;
import nooblife.lockit.util.Utils;

public class TvMainActivity extends Activity {

    private final String TAG = getClass().getSimpleName();

    ArrayList<Application> applications = new ArrayList<>();
    RecyclerView appList;
    ApplicationAdapter adapter;
    TextView selectAll, resetOptions, startSession, connectionView, unlockView;
    private SharedPreferences sharedPreferences;
    private String serviceId;
    private BroadcastReceiver unlockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showMainUI();
        }
    };
    public static int CONNECT_ACTIVITY_RQ = 128;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONNECT_ACTIVITY_RQ) {
            String status = "complete";
            if (resultCode == RESULT_OK)
                serviceId = sharedPreferences.getString(Constants.PREF_LOCKIT_RC_SERVICE_ID, Constants.LOCKIT_DEFAULT_SERVICE_ID);
            else
                status = "failed";
            Toast.makeText(TvMainActivity.this, "Pairing " + status + "!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_main);
        appList = findViewById(R.id.apps_list);

        unlockView = findViewById(R.id.unlock_preview);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4, RecyclerView.VERTICAL, false);
        appList.setLayoutManager(layoutManager);
        adapter = new ApplicationAdapter(this, applications, R.layout.app_item);

        connectionView = findViewById(R.id.connection_view);
        connectionView.setOnClickListener(v -> {
//            if (serviceId.equals(Constants.LOCKIT_DEFAULT_SERVICE_ID)) {
            startActivityForResult(new Intent(TvMainActivity.this, ConnectActivity.class), CONNECT_ACTIVITY_RQ);
//            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        serviceId = sharedPreferences.getString(Constants.PREF_LOCKIT_RC_SERVICE_ID, Constants.LOCKIT_DEFAULT_SERVICE_ID);

        startSession = findViewById(R.id.start_session);
        startSession.setOnClickListener(v -> {
            List<Application> applications = adapter.getSelectedApplications();
            if (applications.isEmpty()) {
                Toast.makeText(TvMainActivity.this, "Select at least one application", Toast.LENGTH_LONG).show();
                return;
            }

            if (serviceId.equals(Constants.LOCKIT_DEFAULT_SERVICE_ID)) {
                Toast.makeText(TvMainActivity.this, "Set up a remote locker", Toast.LENGTH_LONG).show();
                return;
            }

            final ArrayList<String> pkgList = new ArrayList<>();

            for (Application a : applications) {
                pkgList.add(a.getApplicationPackageName());
            }

            BlacklistDatabase.getInstance(TvMainActivity.this).blacklistDao().createBlacklist(new Blacklist(pkgList));
            Utils.startLockService(TvMainActivity.this, Constants.ACTION_START_LOCKSERVICE_FROM_UI);
            finish();
        });

        resetOptions = findViewById(R.id.reset);
        resetOptions.setOnClickListener(v -> {
            Utils.applyBlacklistData(TvMainActivity.this, applications);
            adapter.notifyDataSetChanged();
        });

        selectAll = findViewById(R.id.select_all);
        selectAll.setOnClickListener(v -> {
            for (Application a : applications)
                a.setSelected(true);
            adapter.notifyDataSetChanged();
        });

        Utils.retrieveApplicationList(this, new Utils.AppRetrivalInterface() {
            @Override
            public void onProgress() {

            }

            @Override
            public void onComplete(List<Application> applications) {
                TvMainActivity.this.applications.addAll(Utils.applyBlacklistData(TvMainActivity.this, applications));
                runOnUiThread(() -> appList.setAdapter(adapter));
            }
        });

        if (Utils.isLockServiceRunning(this)) {
            registerReceiver(unlockReceiver, new IntentFilter(Constants.ACTION_UNLOCK_MAINAPP));
            showLockUI();
        } else {
            connectionView.performClick();
            showMainUI();
        }
    }

    private void showMainUI() {
        unlockView.setVisibility(View.GONE);
        selectAll.setVisibility(View.VISIBLE);
        connectionView.setVisibility(View.VISIBLE);
        startSession.setVisibility(View.VISIBLE);
        appList.setVisibility(View.VISIBLE);
    }

    private void showLockUI() {
        unlockView.setVisibility(View.VISIBLE);
        selectAll.setVisibility(View.GONE);
        connectionView.setVisibility(View.GONE);
        startSession.setVisibility(View.GONE);
        appList.setVisibility(View.GONE);
        Intent intent = new Intent(this, LockActivity.class);
        intent.putExtra(Constants.EXTRA_LOCKED_PKGNAME, getPackageName());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(unlockReceiver);
    }
}