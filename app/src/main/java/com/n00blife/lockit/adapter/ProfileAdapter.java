package com.n00blife.lockit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.n00blife.lockit.R;
import com.n00blife.lockit.model.Profile;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Profile> profileArrayList;
    private OnItemClickedListener onItemClickedListener;

    public ProfileAdapter(Context context, ArrayList<Profile> profileArrayList, OnItemClickedListener onItemClickedListener) {
        this.context = context;
        this.profileArrayList = profileArrayList;
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
        holder.addClickListener();
    }

    @Override
    public int getItemCount() {
        return profileArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView profileName;

        ViewHolder(View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.profile_name);
        }

        public void addClickListener() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickedListener.onItemClick(getAdapterPosition(), profileArrayList.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface OnItemClickedListener {
        public void onItemClick(int position, Profile profile);
    }
}
