package com.sergeyrodin.onlinelifeviewer;

import java.util.ArrayList;
import java.util.List;

class ResultsData {
    private List<Result> results;
    private boolean isError;
    ResultsData() {
        results = new ArrayList<>();
        isError = false;
    }

    public void add(Result result) {
        results.add(result);
    }

    public List<Result> getResults() {
        return results;
    }

    public void setError(boolean error) {
        this.isError = error;
    }

    public boolean isError() {
        return isError;
    }
}
