package com.n00blife.lockit.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.n00blife.lockit.R;
import com.n00blife.lockit.activities.ProfileCreationActivity;
import com.n00blife.lockit.adapter.ProfileAdapter;
import com.n00blife.lockit.database.ProfileDatabase;
import com.n00blife.lockit.model.Profile;
import com.n00blife.lockit.services.LockService;
import com.n00blife.lockit.util.Constants;
import com.n00blife.lockit.util.ItemSwipeListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

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
                                lockServiceIntent.setAction("lockit");
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
            ProfileDatabase whiteListedApplicationDatabase = ProfileDatabase.getInstance(getActivity());


            whiteListedApplicationDatabase.profileDao().getAllProfiles()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<Profile>>() {
                        @Override
                        public void accept(List<Profile> profiles) throws Exception {
                            ProfileFragment.this.profiles.clear();
                            ProfileFragment.this.profiles.addAll(profiles);
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
                    ProfileDatabase
                            .getInstance(getContext())
                            .profileDao()
                            .deleteProfile(profiles.get(viewHolder.getAdapterPosition()));
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
