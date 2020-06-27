package nooblife.lockit.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import nooblife.lockit.R;
import nooblife.lockit.adapter.ApplicationAdapter;
import nooblife.lockit.database.BlacklistDatabase;
import nooblife.lockit.model.Application;
import nooblife.lockit.model.Blacklist;
import nooblife.lockit.services.LockService;
import nooblife.lockit.util.Utils;

public class ProfileFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    ArrayList<Application> applications = new ArrayList<>();
    RecyclerView appList;
    ApplicationAdapter adapter;
    FloatingActionButton startSession;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_profile, container, false);

        appList = view.findViewById(R.id.apps_list);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        appList.setLayoutManager(layoutManager);
        adapter = new ApplicationAdapter(getContext(), applications, R.layout.app_item);

        startSession = view.findViewById(R.id.start_session);
        startSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Application> applications = adapter.getSelectedApplications();
                if (applications.isEmpty()) {
                    Toast.makeText(getContext(), "Select at least one application", Toast.LENGTH_LONG).show();
                    return;
                }

                final ArrayList<String> pkgList = new ArrayList<>();

                for (Application a : applications) {
                    pkgList.add(a.getApplicationPackageName());
                }

                BlacklistDatabase.getInstance(getActivity()).blacklistDao().createBlacklist(new Blacklist(pkgList));
                Toast.makeText(getContext(), "Start!", Toast.LENGTH_LONG).show();
                Intent lockServiceIntent = new Intent(getContext(), LockService.class);
                lockServiceIntent.setAction("lockit_tv");
                ContextCompat.startForegroundService(getContext(), lockServiceIntent);
                getActivity().finish();
            }
        });

        Utils.retrieveApplicationList(getContext(), new Utils.AppRetrivalInterface() {
            @Override
            public void onProgress() {

            }

            @Override
            public void onComplete(List<Application> applications) {
                ProfileFragment.this.applications.addAll(applications);
                appList.setAdapter(adapter);
            }
        });

        return view;
    }
}
