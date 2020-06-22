package com.n00blife.lockit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.n00blife.lockit.R;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Application> applicationArrayList;
    private onItemClicked onItemClicked;
    private int layoutResId;

    public ApplicationAdapter(Context context, ArrayList<Application> applicationArrayList, int layoutResId) {
        this.context = context;
        this.applicationArrayList = applicationArrayList;
        this.layoutResId = layoutResId;
    }

    public void setOnItemClicked(onItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

    public List<Application> getSelectedApplications() {
        ArrayList<Application> applications = new ArrayList<>();
        for (Application a : applicationArrayList) {
            if (a.isSelected())
                applications.add(a);
        }
        return applications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(layoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Application List in ProfileCreationActivity
        if (holder.applicationPackageName != null) {
            holder.applicationPackageName.setText(applicationArrayList.get(position).getApplicationPackageName());
            holder.applicationVersion.setText(applicationArrayList.get(position).getApplicationVersion());
        }
        // Application in WhiteListRecyclerView
        if (holder.applicationName != null)
            holder.applicationName.setText(applicationArrayList.get(position).getApplicationName());
        // Application list in Profile Layout
        holder.applicationIcon.setImageBitmap(ImageUtils.decodeBase64ToBitmap(applicationArrayList.get(position).getApplicationIconEncoded()));
        holder.isSelectedForLock.setChecked(applicationArrayList.get(position).isSelected());
    }

    @Override
    public int getItemCount() {
        return applicationArrayList.size();
    }

    public void getSelectedApps() {

    }

    public interface onItemClicked {
        void onHolderClick(int position, Application application);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView applicationIcon;
        TextView applicationName;
        TextView applicationPackageName;
        TextView applicationVersion;
        CheckBox isSelectedForLock;

        public ViewHolder(final View itemView) {
            super(itemView);
            applicationIcon = itemView.findViewById(R.id.application_icon);
            applicationName = itemView.findViewById(R.id.application_name);
            applicationPackageName = itemView.findViewById(R.id.application_package_name);
            applicationVersion = itemView.findViewById(R.id.application_version);
            isSelectedForLock = itemView.findViewById(R.id.checkbox);

            if (layoutResId != R.layout.app_item_grid_mini) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isSelectedForLock.toggle();
                        applicationArrayList.get(getAdapterPosition()).setSelected(isSelectedForLock.isChecked());
                    }
                });
            }
        }

    }
}
