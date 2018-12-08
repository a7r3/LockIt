package com.n00blife.lockit.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "apps")
public class Application {

    @ColumnInfo(name = "app_name")
    private String applicationName;
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "app_package")
    private String applicationPackageName;
    @ColumnInfo(name = "app_version")
    private String applicationVersion;
    @ColumnInfo(name = "app_icon64")
    private String applicationIconEncoded;

    @Ignore
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
