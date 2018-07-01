package com.n00blife.lockit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.n00blife.lockit.R;
import com.n00blife.lockit.database.WhiteListedApplicationDatabase;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.model.Profile;
import com.n00blife.lockit.util.ImageUtils;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Profile> profileArrayList;
    private OnItemClickedListener onItemClickedListener;
    private OnListModifiedListener onListModifiedListener;

    public ProfileAdapter(Context context, ArrayList<Profile> profileArrayList) {
        this.context = context;
        this.profileArrayList = profileArrayList;
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.profile_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.profileName.setText(profileArrayList.get(position).getProfileName());
        ArrayList<Application> applications = new ArrayList<>();
        try {
            for (String pkg : profileArrayList.get(holder.getAdapterPosition()).getPackageList()) {
                Application a = new Application();
                a.setApplicationIcon(ImageUtils.drawableToBitmap(context.getPackageManager().getApplicationIcon(pkg)));
                applications.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(onListModifiedListener != null)
            onListModifiedListener.onListModified(profileArrayList.size());
        holder.appsList.setAdapter(new ApplicationAdapter(context, applications, R.layout.app_item_grid_mini));
        holder.appsList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.appsList.setLayoutFrozen(true);
    }

    @Override
    public int getItemCount() {
        return profileArrayList.size();
    }

    public void setOnListModifiedListener(OnListModifiedListener onEmptyStateListener) {
        this.onListModifiedListener = onEmptyStateListener;
    }

    public interface OnItemClickedListener {
        public void onItemClick(int position, Profile profile);
    }

    public interface OnListModifiedListener {
        public void onListModified(int currentSize);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView profileName;
        RecyclerView appsList;
        ImageView deleteProfile;

        ViewHolder(View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.profile_name);
            appsList = itemView.findViewById(R.id.profile_apps_list);
            deleteProfile = itemView.findViewById(R.id.profile_delete_action);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickedListener.onItemClick(getAdapterPosition(), profileArrayList.get(getAdapterPosition()));
                }
            });
            deleteProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WhiteListedApplicationDatabase
                            .getInstance(context)
                            .deleteProfile(profileArrayList.get(getAdapterPosition()).getProfileName());
                    profileArrayList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    if(onListModifiedListener != null)
                        onListModifiedListener.onListModified(profileArrayList.size());
                }
            });
        }

    }
}
