package com.n00blife.lockit.dao;

import com.n00blife.lockit.model.Profile;


import java.util.ArrayList;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ProfileDao {

    @Query("SELECT * FROM whitelist_profiles WHERE profileName = :profileName")
    Profile getProfile(String profileName);

    @Query("SELECT * FROM whitelist_profiles")
    List<Profile> getAllProfiles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createProfile(Profile profile);

    @Delete
    void deleteProfile(Profile profile);
}
