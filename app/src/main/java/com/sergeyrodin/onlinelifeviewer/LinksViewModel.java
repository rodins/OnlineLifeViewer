package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;
import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

class LinksViewModel extends ViewModel {
    private LiveData<SavedItem> mSavedItem;
    private String link, js;
    private LiveData<LinkData> linkData;

    LinksViewModel(AppDatabase db, String link, String js) {
        this.link = link;
        this.js = js;
        mSavedItem = db.savedItemsDao().loadSavedItemByLink(link);
    }

    public LiveData<LinkData> getLinkData() {
        if(linkData == null) {
            LinksRepo linksRepo = new LinksRepo(link, js);
            linkData = linksRepo.getLinkData();
        }
        return linkData;
    }

    public LiveData<SavedItem> getmSavedItem() {
        return mSavedItem;
    }
}
