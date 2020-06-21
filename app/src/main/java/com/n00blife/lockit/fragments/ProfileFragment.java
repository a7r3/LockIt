package com.n00blife.lockit.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.n00blife.lockit.R;
import com.n00blife.lockit.adapter.ApplicationAdapter;
import com.n00blife.lockit.database.BlacklistDatabase;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.model.Blacklist;
import com.n00blife.lockit.services.LockService;
import com.n00blife.lockit.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    ArrayList<Application> applications = new ArrayList<>();
    RecyclerView appList;
    ApplicationAdapter adapter;
    FloatingActionButton startSession;
    private final String TAG = getClass().getSimpleName();
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_profile, container, false);

        appList = view.findViewById(R.id.apps_list);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        appList.setLayoutManager(layoutManager);
        adapter = new ApplicationAdapter(getContext(), applications, R.layout.app_item);

        FirebaseMessaging.getInstance().subscribeToTopic("locker").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: Subbed!");
            }
        });
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

                BlacklistDatabase.getInstance(getActivity()).profileDao().createBlacklist(new Blacklist(pkgList));
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
