package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

class ActorsRepo {
    private MutableLiveData<ActorsData> actorsData;

    ActorsRepo() {
        actorsData = new MutableLiveData<>();
    }

    public LiveData<ActorsData> getActorsData(String link) {
        getActorsDataFromNet(link);
        return actorsData;
    }

    private void getActorsDataFromNet(final String link) {
        actorsData.setValue(new ActorsData(true,false));
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ActorsData data = new ActorsParser().parse(link);
                    actorsData.postValue(data);
                } catch (Exception e) {
                    actorsData.postValue(new ActorsData(false, true));
                }
            }
        });
    }
}
