package com.n00blife.lockit.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

public class Application {

    private String applicationName;
    private String applicationPackageName;
    private String applicationVersion;
    private String applicationIconEncoded;

    public Application(String applicationName, String applicationPackageName, String applicationVersion, String applicationIconEncoded) {
        this.applicationName = applicationName;
        this.applicationPackageName = applicationPackageName;
        this.applicationVersion = applicationVersion;
        this.applicationIconEncoded = applicationIconEncoded;
    }

    public Application() {

    }

    public String getApplicationIconEncoded() {
        return applicationIconEncoded;
    }

    public void setApplicationIconEncoded(String applicationIconEncoded) {
        this.applicationIconEncoded = applicationIconEncoded;
    }

    public boolean isAfter(Application toBeInserted) {
        return getApplicationName().compareTo(toBeInserted.getApplicationName()) > 0;
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
