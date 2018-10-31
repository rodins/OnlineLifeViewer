package com.sergeyrodin.onlinelifeviewer;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.PagedList;

public class ResultsViewModel extends ViewModel {
    private MutableLiveData<PagedList<Result>> liveData;
    private ResultsDataSource dataSource;
    private MutableLiveData<ResultsDataSource> dataSourceLiveData;

    public ResultsViewModel() {
        // This live data needed for transformations. To set state from new data source after retry
        dataSourceLiveData = new MutableLiveData<>();
    }

    LiveData<PagedList<Result>> getLiveData(String link) {
        if(liveData == null) {
            liveData = new MutableLiveData<>();
            getPagedList(link);
        }
        return liveData;
    }

    LiveData<State> getState() {
        return Transformations.switchMap(dataSourceLiveData, new Function<ResultsDataSource, LiveData<State>>() {
            @Override
            public LiveData<State> apply(ResultsDataSource input) {
                return input.state;
            }
        });
    }

    void retry() {
        dataSource.retry();
    }

    void refresh(String link) {
        dataSource.invalidate();
        getPagedList(link);
    }

    private void getPagedList(String link) {
        dataSource = new ResultsDataSource(link);
        dataSourceLiveData.setValue(dataSource);
        // Think about how to get page size.
        PagedList.Config config = new PagedList.Config.Builder().setPageSize(5)
                                                                .setEnablePlaceholders(false)
                                                                .build();
        PagedList<Result> results = new PagedList.Builder<>(dataSource, config)
                                                 .setFetchExecutor(AppExecutors.getInstance().networkIO())
                                                 .setNotifyExecutor(AppExecutors.getInstance().mainThread())
                                                 .build();
        liveData.setValue(results);
    }
}
