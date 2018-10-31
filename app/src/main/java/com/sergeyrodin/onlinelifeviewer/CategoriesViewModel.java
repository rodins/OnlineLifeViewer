package com.sergeyrodin.onlinelifeviewer;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {
    private LiveData<List<Link>> categoriesData;
    private CategoriesRepo categoriesRepo;

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        String categoriesUrl = application.getString(R.string.onlinelife_domain);
        categoriesRepo = new CategoriesRepo(categoriesUrl);
    }

    LiveData<List<Link>> getCategoriesData() {
        if(categoriesData == null) {
            categoriesData = categoriesRepo.getCategoriesData();
        }
        return categoriesData;
    }

    LiveData<State> getState() {
        return categoriesRepo.getState();
    }

    void refresh() {
        categoriesRepo.refresh();
    }
}
