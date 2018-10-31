package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

class ActorsRepo {
    private MutableLiveData<ActorsData> actorsData;
    private MutableLiveData<State> state;

    ActorsRepo() {
        actorsData = new MutableLiveData<>();
        state = new MutableLiveData<>();
    }

    public LiveData<ActorsData> getActorsData(String link) {
        getActorsDataFromNet(link);
        return actorsData;
    }

    LiveData<State> getState() {
        return state;
    }

    void getActorsDataFromNet(final String link) {
        state.setValue(State.LOADING_INIT);
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ActorsData data = new ActorsParser().parse(link);
                    if(data.getActors().isEmpty()) {
                        state.postValue(State.EMPTY);
                    }else {
                        actorsData.postValue(data);
                        state.postValue(State.DONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    state.postValue(State.ERROR_INIT);
                }
            }
        });
    }
}
