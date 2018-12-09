package com.n00blife.lockit.model;

import com.n00blife.lockit.database.ArrayListTypeConverter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "whitelist_profiles")
public class Profile {

    @PrimaryKey
    @NonNull
    String profileName;

    ArrayList<String> packageList;

    public Profile(String profileName, ArrayList<String> packageList) {
        this.profileName = profileName;
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
