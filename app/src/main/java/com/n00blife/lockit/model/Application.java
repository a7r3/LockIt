package com.n00blife.lockit.model;

import android.graphics.Bitmap;

public class Application {

    private String applicationName;
    private String applicationPackageName;
    private String applicationVersion;
    private Bitmap applicationIcon;
    private int positionInApplicationList = -1;

    public Application(String applicationName, String applicationPackageName, String applicationVersion, Bitmap applicationIcon) {
        this.applicationName = applicationName;
        this.applicationPackageName = applicationPackageName;
        this.applicationVersion = applicationVersion;
        this.applicationIcon = applicationIcon;
    }

    public Application() {

    }

    public int getPositionInApplicationList() {
        return positionInApplicationList;
    }

    public void setPositionInApplicationList(int positionInApplicationList) {
        this.positionInApplicationList = positionInApplicationList;
    }

    public boolean isAfter(Application toBeInserted) {
        return getApplicationName().compareTo(toBeInserted.getApplicationName()) > 0;
    }

    public Bitmap getApplicationIcon() {
        return applicationIcon;
    }

    public void setApplicationIcon(Bitmap applicationIcon) {
        this.applicationIcon = applicationIcon;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationPackageName() {
        return applicationPackageName;
    }

    public void setApplicationPackageName(String applicationPackageName) {
        this.applicationPackageName = applicationPackageName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

}
