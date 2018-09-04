package com.sergeyrodin.onlinelifeviewer;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;
import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

import java.util.List;

public class SavedItemsViewModel extends AndroidViewModel {
    private static final String LOG_TAG = SavedItemsViewModel.class.getSimpleName();

    private LiveData<List<SavedItem>> savedItems;

    public SavedItemsViewModel(@NonNull Application application) {
        super(application);

        AppDatabase db = AppDatabase.getsInstanse(getApplication());
        savedItems = db.savedItemsDao().loadSavedItems();
    }

    public LiveData<List<SavedItem>> getSavedItems() {
        return savedItems;
    }
}
