package com.n00blife.lockit.dao;

import com.n00blife.lockit.model.Blacklist;


import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import io.reactivex.Observable;

@Dao
public interface BlacklistDao {

    @Query("SELECT * FROM apps WHERE profileName = 'default'")
    Blacklist getBlacklist();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createBlacklist(Blacklist blacklist);

}
