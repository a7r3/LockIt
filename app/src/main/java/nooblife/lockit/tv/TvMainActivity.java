package nooblife.lockit.tv;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import nooblife.lockit.R;
import nooblife.lockit.adapter.ApplicationAdapter;
import nooblife.lockit.database.BlacklistDatabase;
import nooblife.lockit.model.Application;
import nooblife.lockit.model.Blacklist;
import nooblife.lockit.util.Constants;
import nooblife.lockit.util.LockItServer;
import nooblife.lockit.util.Utils;

public class TvMainActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    ArrayList<Application> applications = new ArrayList<>();
    RecyclerView appList;
    ApplicationAdapter adapter;
    TextView selectAll, resetOptions, startSession, connectionView;
    private SharedPreferences sharedPreferences;
    private String serviceId;
    private LockItServer lockItServer;
    private BottomSheetDialog bsd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_main);
        appList = findViewById(R.id.apps_list);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4, RecyclerView.VERTICAL, false);
        appList.setLayoutManager(layoutManager);
        adapter = new ApplicationAdapter(this, applications, R.layout.app_item);

        bsd = new BottomSheetDialog(TvMainActivity.this, R.style.Theme_Design_BottomSheetDialog) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.dialog_connect);
                View cancel = findViewById(R.id.connect_cancel);
                cancel.setOnClickListener(v1 -> {
                    lockItServer.stop();
                    cancel();
                });
            }

            @Override
            protected void onStop() {
                lockItServer.stop();
            }
        };
        bsd.setCancelable(true);

        lockItServer = LockItServer.initialize(TvMainActivity.this, new LockItServer.ServerEventListener() {
            @Override
            public void onLock() {

            }

            @Override
            public void onUnlock() {

            }

            @Override
            public void onPair() {
                serviceId = sharedPreferences.getString(Constants.PREF_LOCKIT_RC_SERVICE_ID, Constants.LOCKIT_DEFAULT_SERVICE_ID);
                bsd.cancel();
//                Toast.makeText(TvMainActivity.this, "Phone paired successfully", Toast.LENGTH_SHORT).show();
            }
        });
        lockItServer.setInPairingMode();

        connectionView = findViewById(R.id.connection_view);
        connectionView.setOnClickListener(v -> {
            if (serviceId.equals(Constants.LOCKIT_DEFAULT_SERVICE_ID)) {
                lockItServer.start();
                bsd.show();
            }
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

            // Well, gotta do that
            pkgList.add(getPackageName());

            BlacklistDatabase.getInstance(TvMainActivity.this).blacklistDao().createBlacklist(new Blacklist(pkgList));
            Utils.startLockService(TvMainActivity.this);
            finish();
        });

        resetOptions = findViewById(R.id.reset);
        resetOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.applyBlacklistData(TvMainActivity.this, applications);
                adapter.notifyDataSetChanged();
            }
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
                appList.setAdapter(adapter);
            }
        });
    }

}