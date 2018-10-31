package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;
import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

public class ActorsViewModel extends ViewModel {
    private String link;
    private AppDatabase db;
    private LiveData<ActorsData> actorsData;
    private ActorsRepo actorsRepo;

    ActorsViewModel(AppDatabase db, String link) {
        this.link = link;
        this.db = db;
        actorsRepo = new ActorsRepo();
    }

    LiveData<ActorsData> getActorsData() {
        if(actorsData == null) {
            actorsData = actorsRepo.getActorsData(link);
        }
        return actorsData;
    }

    void retry() {
        actorsRepo.getActorsDataFromNet(link);
    }

    LiveData<State> getState() {
        return actorsRepo.getState();
    }

    LiveData<SavedItem> getSavedItem() {
        return db.savedItemsDao().loadSavedItemByLink(link);
    }
}
