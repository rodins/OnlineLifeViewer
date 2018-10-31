package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;
import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

public class ActorsViewModel extends ViewModel {
    private String link;
    private AppDatabase db;
    private LiveData<ActorsData> actorsData;
    private ActorsDataSource actorsDataSource;

    ActorsViewModel(AppDatabase db, String link) {
        this.link = link;
        this.db = db;
        actorsDataSource = new ActorsDataSource();
    }

    LiveData<ActorsData> getActorsData() {
        if(actorsData == null) {
            actorsData = actorsDataSource.getActorsData(link);
        }
        return actorsData;
    }

    void retry() {
        actorsDataSource.getActorsDataFromNet(link);
    }

    LiveData<State> getState() {
        return actorsDataSource.getState();
    }

    LiveData<SavedItem> getSavedItem() {
        return db.savedItemsDao().loadSavedItemByLink(link);
    }
}
