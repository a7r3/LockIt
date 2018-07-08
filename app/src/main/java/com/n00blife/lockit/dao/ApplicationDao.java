package com.n00blife.lockit.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.n00blife.lockit.model.Application;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface ApplicationDao {

    @Query("SELECT * FROM apps")
    Maybe<List<Application>> getApplications();

    @Query("SELECT COUNT(*) FROM apps")
    Single<Integer> getNumberOfRows();

    @Query("SELECT app_package FROM apps")
    Maybe<List<String>> getPackages();

    @Query("SELECT * from apps WHERE app_package = :pkg")
    Maybe<Application> getApplicationByPackage(String pkg);

    @Delete
    void removeApplication(Application a);

    @Insert
    void addApplication(Application a);

}
