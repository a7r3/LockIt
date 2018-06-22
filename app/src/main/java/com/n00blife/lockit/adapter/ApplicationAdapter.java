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

import java.util.ArrayList;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Application> applicationArrayList;
    private OnItemClicked onHolderClicked;

    public ApplicationAdapter(Context context, ArrayList<Application> applicationArrayList, OnItemClicked onHolderClicked) {
        this.context = context;
        this.applicationArrayList = applicationArrayList;
        this.onHolderClicked = onHolderClicked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.app_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.applicationIcon.setImageDrawable(applicationArrayList.get(position).getApplicationIcon());
        holder.applicationName.setText(applicationArrayList.get(position).getApplicationName());
        holder.applicationPackageName.setText(applicationArrayList.get(position).getApplicationPackageName());
        holder.dostuff(holder, applicationArrayList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return applicationArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView applicationIcon;
        public TextView applicationName;
        public TextView applicationPackageName;

        public ViewHolder(View itemView) {
            super(itemView);
            applicationIcon = itemView.findViewById(R.id.application_icon);
            applicationName = itemView.findViewById(R.id.application_name);
            applicationPackageName = itemView.findViewById(R.id.application_package_name);
        }

        public void dostuff(final ViewHolder holder, final Application application, final int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    application.setPositionInApplicationList(holder.getAdapterPosition());
                    onHolderClicked.onHolderClick(holder, application);
                }
            });
        }

    }

    public interface OnItemClicked {
        public void onHolderClick(ViewHolder viewHolder, Application application);
    }
}
