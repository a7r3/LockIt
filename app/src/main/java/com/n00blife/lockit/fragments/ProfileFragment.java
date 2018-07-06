package com.n00blife.lockit.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.n00blife.lockit.R;
import com.n00blife.lockit.activities.ProfileCreationActivity;
import com.n00blife.lockit.adapter.ProfileAdapter;
import com.n00blife.lockit.database.WhiteListedApplicationDatabase;
import com.n00blife.lockit.model.Profile;
import com.n00blife.lockit.services.LockService;
import com.n00blife.lockit.util.Constants;
import com.n00blife.lockit.util.ItemSwipeListener;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;

public class ProfileFragment extends Fragment {

    private PackageManager pm;
    private FloatingActionButton createProfileButton;
    private RecyclerView profileListView;
    private ArrayList<Profile> profiles = new ArrayList<>();
    private ProfileAdapter adapter;
    private LinearLayout noProfileContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_profile, container, false);

        noProfileContainer = rootView.findViewById(R.id.no_profile_container);
        noProfileContainer.setVisibility(View.GONE);

        profileListView = rootView.findViewById(R.id.profile_recyclerview);
        profileListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        profileListView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter = new ProfileAdapter(getActivity(), profiles);

        pm = getActivity().getPackageManager();

        adapter.setOnItemClickedListener(new ProfileAdapter.OnItemClickedListener() {
            @Override
            public void onItemClick(int position, final Profile profile) {
                View numberPickerView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_activate_profile, null, false);
                final NumberPicker numberPicker = numberPickerView.findViewById(R.id.timer_picker);
                numberPicker.setMaxValue(30);
                numberPicker.setMinValue(1);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setView(numberPickerView)
                        .setPositiveButton("Activate", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent lockServiceIntent = new Intent(getActivity(), LockService.class);
                                lockServiceIntent.putExtra(Constants.EXTRA_TIMER, numberPicker.getValue());
                                lockServiceIntent.putExtra(Constants.EXTRA_PROFILE_NAME, profile.getProfileName());
                                ContextCompat.startForegroundService(getActivity(), lockServiceIntent);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setTitle("Activate '" + profile.getProfileName() + "'");

                builder.create().show();
            }
        });

        profileListView.setAdapter(adapter);

        createProfileButton = rootView.findViewById(R.id.create_profile_button);

        createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ProfileCreationActivity.class));
            }
        });

        if (profiles.isEmpty()) {
            WhiteListedApplicationDatabase whiteListedApplicationDatabase = WhiteListedApplicationDatabase.getInstance(getActivity());

            Observable.fromIterable(whiteListedApplicationDatabase.getProfiles())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Profile>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Profile profile) {
                            profiles.add(profile);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            if (profiles.size() == 0) {
                                noProfileContainer.setVisibility(View.VISIBLE);
                                profileListView.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
        }

        ItemSwipeListener listener = new ItemSwipeListener(getContext()) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction == LEFT) {
                    WhiteListedApplicationDatabase
                            .getInstance(getContext())
                            .deleteProfile(profiles.get(viewHolder.getAdapterPosition()).getProfileName());
                    profiles.remove(viewHolder.getAdapterPosition());
                    adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    if (adapter.profileArrayList.size() == 0) {
                        noProfileContainer.setVisibility(View.VISIBLE);
                        profileListView.setVisibility(View.GONE);
                    } else {
                        noProfileContainer.setVisibility(View.GONE);
                        profileListView.setVisibility(View.VISIBLE);
                    }
                } else if(direction == RIGHT) {
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            }
        };

        ItemTouchHelper helper = new ItemTouchHelper(listener);
        helper.attachToRecyclerView(profileListView);

        return rootView;
    }
}
