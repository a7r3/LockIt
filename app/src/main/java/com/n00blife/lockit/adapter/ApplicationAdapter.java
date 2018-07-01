package com.n00blife.lockit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.n00blife.lockit.R;
import com.n00blife.lockit.model.Application;
import com.n00blife.lockit.util.ImageUtils;

import java.util.ArrayList;

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
    }

    @Override
    public int getItemCount() {
        return applicationArrayList.size();
    }

    public interface onItemClicked {
        public void onHolderClick(int position, Application application);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView applicationIcon;
        TextView applicationName;
        TextView applicationPackageName;
        TextView applicationVersion;

        public ViewHolder(View itemView) {
            super(itemView);
            applicationIcon = itemView.findViewById(R.id.application_icon);
            applicationName = itemView.findViewById(R.id.application_name);
            applicationPackageName = itemView.findViewById(R.id.application_package_name);
            applicationVersion = itemView.findViewById(R.id.application_version);
            if(layoutResId != R.layout.app_item_grid_mini) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClicked.onHolderClick(getAdapterPosition(), applicationArrayList.get(getAdapterPosition()));
                    }
                });
            }
        }

    }
}
