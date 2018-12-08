package com.n00blife.lockit.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.n00blife.lockit.dao.ApplicationDao;
import com.n00blife.lockit.model.Application;

@Database(entities = Application.class, version = 1, exportSchema = false)
public abstract class RoomApplicationDatabase extends RoomDatabase {

    private static RoomApplicationDatabase instance;

    public abstract ApplicationDao applicationDao();

    public static RoomApplicationDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    RoomApplicationDatabase.class,
                    "apps")
                    .build();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}
