package com.n00blife.lockit.model;

import java.util.ArrayList;

public class Profile {

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
