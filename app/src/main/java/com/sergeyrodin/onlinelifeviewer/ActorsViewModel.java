package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

public class ActorsViewModel extends ViewModel {
    private String link;
    private LiveData<ActorsData> actorsData;
    private ActorsRepo actorsRepo;

    ActorsViewModel(String link) {
        this.link = link;
        actorsRepo = new ActorsRepo();
    }

    public LiveData<ActorsData> getActorsData() {
        if(actorsData == null) {
            actorsData = actorsRepo.getActorsData(link);
        }
        return actorsData;
    }
}
