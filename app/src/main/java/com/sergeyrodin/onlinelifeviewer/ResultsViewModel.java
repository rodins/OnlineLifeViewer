package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.PagedList;

public class ResultsViewModel extends ViewModel {
    private MutableLiveData<PagedList<Result>> liveData;
    private LiveData<State> state;

    LiveData<PagedList<Result>> getLiveData(String link) {
        if(liveData == null) {
            getPagedList(link);
        }
        return liveData;
    }

    LiveData<State> getState() {
        return state;
    }

    private void getPagedList(String link) {
        ResultsDataSource dataSource = new ResultsDataSource(link);
        state = dataSource.state;
        // Think about how to get page size.
        PagedList.Config config = new PagedList.Config.Builder().setPageSize(5)
                                                                .setEnablePlaceholders(false)
                                                                .build();
        PagedList<Result> results = new PagedList.Builder<>(dataSource, config)
                                                 .setFetchExecutor(AppExecutors.getInstance().networkIO())
                                                 .setNotifyExecutor(AppExecutors.getInstance().mainThread())
                                                 .build();
        liveData = new MutableLiveData<>();
        liveData.setValue(results);
    }
}
