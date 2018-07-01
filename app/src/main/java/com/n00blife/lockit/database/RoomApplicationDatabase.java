package com.n00blife.lockit.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
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