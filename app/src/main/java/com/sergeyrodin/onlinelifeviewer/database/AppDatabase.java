package com.sergeyrodin.onlinelifeviewer.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = SavedItem.class, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "onlinelife";
    private static AppDatabase sInstanse;

    public static AppDatabase getsInstanse(Context context) {
        if(sInstanse == null) {
            synchronized (LOCK) {
                sInstanse = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();
            }
        }
        return sInstanse;
    }

    public abstract SavedItemsDao savedItemsDao();
}
