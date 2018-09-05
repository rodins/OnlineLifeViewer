package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;
import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

class LinksViewModel extends ViewModel {
    private LiveData<SavedItem> mSavedItem;

    public LinksViewModel(AppDatabase db, String link) {
        mSavedItem = db.savedItemsDao().loadSavedItemByLink(link);
    }

    public LiveData<SavedItem> getmSavedItem() {
        return mSavedItem;
    }
}
