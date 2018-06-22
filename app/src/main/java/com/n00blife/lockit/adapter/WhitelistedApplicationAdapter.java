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

public class WhitelistedApplicationAdapter extends RecyclerView.Adapter<WhitelistedApplicationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Application> applications;
    private OnItemClickListener onItemClickListener;

    public WhitelistedApplicationAdapter(Context context, ArrayList<Application> applications, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.applications = applications;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.app_item_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.applicationName.setText(applications.get(position).getApplicationName());
        holder.applicationIcon.setImageDrawable(applications.get(position).getApplicationIcon());
        holder.addClick(holder, applications.get(position), position);
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView applicationIcon;
        public TextView applicationName;

        public ViewHolder(View itemView) {
            super(itemView);
            applicationIcon = itemView.findViewById(R.id.application_icon);
            applicationName = itemView.findViewById(R.id.application_name);
        }

        public void addClick(final ViewHolder viewHolder, final Application application, final int position) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    onItemClickListener.onItemClick(application, position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(Application application, int position);
    }
}
