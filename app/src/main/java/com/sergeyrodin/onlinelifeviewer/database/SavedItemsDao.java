package com.sergeyrodin.onlinelifeviewer.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SavedItemsDao {

    @Query("SELECT * FROM saved_items")
    LiveData<List<SavedItem>> loadSavedItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSavedItem(SavedItem savedItem);

    @Delete
    void deleteSavedItem(SavedItem savedItem);

    @Query("SELECT * FROM saved_items WHERE link =:link")
    LiveData<SavedItem> loadSavedItemByLink(String link);

}
