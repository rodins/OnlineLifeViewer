package com.sergeyrodin.onlinelifeviewer;

import java.util.ArrayList;
import java.util.List;

class ResultsData {
    private List<Result> results;
    private boolean isError, isNextLink;
    ResultsData() {
        results = new ArrayList<>();
        isError = false;
        isNextLink = false;
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

    public void setNextLink(boolean isNextLink) {
        this.isNextLink = isNextLink;
    }

    public boolean isNextLink() {
        return isNextLink;
    }
}
