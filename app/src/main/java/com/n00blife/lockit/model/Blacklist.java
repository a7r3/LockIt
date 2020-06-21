package com.n00blife.lockit.model;

import com.n00blife.lockit.database.ArrayListTypeConverter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "apps")
public class Blacklist {

    @PrimaryKey
    @NonNull
    String profileName = "default";

    ArrayList<String> packageList;

    public Blacklist(ArrayList<String> packageList) {
        this.packageList = packageList;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public ArrayList<String> getPackageList() {
        return packageList;
    }

    public void setPackageList(ArrayList<String> packageList) {
        this.packageList = packageList;
    }
}
