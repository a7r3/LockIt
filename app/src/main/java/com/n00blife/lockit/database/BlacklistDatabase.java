package com.n00blife.lockit.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.n00blife.lockit.dao.BlacklistDao;
import com.n00blife.lockit.model.Blacklist;

@Database(entities = Blacklist.class, version = 2, exportSchema = false)
@TypeConverters({ArrayListTypeConverter.class})
public abstract class BlacklistDatabase extends RoomDatabase {
    private static BlacklistDatabase instance;

    public static BlacklistDatabase getInstance(Context context) {
        if (instance != null) return instance;
        instance = Room.databaseBuilder(context, BlacklistDatabase.class, "apps").allowMainThreadQueries().build();
        return instance;
    }

    public abstract BlacklistDao blacklistDao();
}