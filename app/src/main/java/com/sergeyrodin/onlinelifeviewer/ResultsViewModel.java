package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class ResultsViewModel extends ViewModel {
    private List<Result> results;
    private LiveData<ResultsData> resultsData;
    private ResultsNet resultsNet;

    public ResultsViewModel() {
        results = new ArrayList<>();
        resultsNet = new ResultsNet();
    }

    public List<Result> getResults() {
        return results;
    }

    public void loadNextPage() {
        resultsNet.loadNextPage();
    }

    public LiveData<ResultsData> getResultsData(String link) {
        if(resultsData == null) {
            resultsData = resultsNet.getResultsData(link);
        }
        return resultsData;
    }
}
