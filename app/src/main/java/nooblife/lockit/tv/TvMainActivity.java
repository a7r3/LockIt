package nooblife.lockit.tv;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import nooblife.lockit.R;
import nooblife.lockit.adapter.ApplicationAdapter;
import nooblife.lockit.database.BlacklistDatabase;
import nooblife.lockit.model.Application;
import nooblife.lockit.model.Blacklist;
import nooblife.lockit.util.Constants;
import nooblife.lockit.util.Utils;

public class TvMainActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    ArrayList<Application> applications = new ArrayList<>();
    RecyclerView appList;
    ApplicationAdapter adapter;
    TextView selectAll, resetOptions, startSession, connectionView;
    private SharedPreferences sharedPreferences;
    private String authenticationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_main);
        appList = findViewById(R.id.apps_list);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4, RecyclerView.VERTICAL, false);
        appList.setLayoutManager(layoutManager);
        adapter = new ApplicationAdapter(this, applications, R.layout.app_item);

        connectionView = findViewById(R.id.connection_view);
        connectionView.setOnClickListener(v -> startAdvertising());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        authenticationCode = sharedPreferences.getString(Constants.PREF_LOCKIT_RC_SERVICE_ID, "");

        startSession = findViewById(R.id.start_session);
        startSession.setOnClickListener(v -> {
            List<Application> applications = adapter.getSelectedApplications();
            if (applications.isEmpty()) {
                Toast.makeText(TvMainActivity.this, "Select at least one application", Toast.LENGTH_LONG).show();
                return;
            }
//
//            if (authenticationCode.isEmpty()) {
//                Toast.makeText(TvMainActivity.this, "Set up a remote locker", Toast.LENGTH_LONG).show();
//                return;
//            }

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
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Application a : applications)
                    a.setSelected(true);
                adapter.notifyDataSetChanged();
            }
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

    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            Toast.makeText(TvMainActivity.this, s + payload.toString(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    private ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo info) {
            authenticationCode = info.getAuthenticationToken();
            new AlertDialog.Builder(TvMainActivity.this, R.style.Theme_AppCompat_Dialog_Alert)
                    .setTitle("Accept connection to " + info.getEndpointName())
                    .setMessage("Confirm the code matches on both devices: " + info.getAuthenticationToken())
                    .setPositiveButton(
                            "Accept",
                            (DialogInterface dialog, int which) -> {
                                    // The user confirmed, so we can accept the connection.
                                    Nearby.getConnectionsClient(TvMainActivity.this)
                                            .acceptConnection(s, payloadCallback);
                            })
                    .setNegativeButton(
                            android.R.string.cancel,
                            (DialogInterface dialog, int which) ->
                                    // The user canceled, so we should reject the connection.
                                    Nearby.getConnectionsClient(TvMainActivity.this).rejectConnection(s))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    Toast.makeText(TvMainActivity.this, "Connection Successful", Toast.LENGTH_SHORT).show();
                    sharedPreferences.edit().putString(Constants.PREF_LOCKIT_RC_SERVICE_ID, authenticationCode).apply();
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    Toast.makeText(TvMainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            Toast.makeText(TvMainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    private void startAdvertising() {
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        Nearby.getConnectionsClient(this)
                .startAdvertising(
                        "remotelocker",
                        Constants.LOCKIT_DISCOVERY_SERVICE_ID,
                        connectionLifecycleCallback,
                        advertisingOptions
                ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: ADVERTISING NOW");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d(TAG, "onFailure: ADVERTISING FAILURE");
            }
        });
    }

}