package com.sergeyrodin.onlinelifeviewer;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {
    private LiveData<List<Link>> categoriesData;
    private CategoriesDataSource categoriesDataSource;

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        String categoriesUrl = application.getString(R.string.onlinelife_domain);
        categoriesDataSource = new CategoriesDataSource(categoriesUrl);
    }

    LiveData<List<Link>> getCategoriesData() {
        if(categoriesData == null) {
            categoriesData = categoriesDataSource.getCategoriesData();
        }
        return categoriesData;
    }

    LiveData<State> getState() {
        return categoriesDataSource.getState();
    }

    void retry() {
        categoriesDataSource.retry();
    }
}
