package com.n00blife.lockit.database;

import android.content.Context;

import com.n00blife.lockit.dao.ProfileDao;
import com.n00blife.lockit.model.Profile;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = Profile.class, version = 2, exportSchema = false)
@TypeConverters({ArrayListTypeConverter.class})
public abstract class ProfileDatabase extends RoomDatabase {
    private static ProfileDatabase instance;

    public abstract ProfileDao profileDao();

    public static ProfileDatabase getInstance(Context context) {
        if (instance != null) return instance;
        instance = Room.databaseBuilder(context, ProfileDatabase.class, "profiles").build();
        return instance;
    }
}