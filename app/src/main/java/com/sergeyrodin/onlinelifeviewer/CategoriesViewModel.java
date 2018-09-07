package com.sergeyrodin.onlinelifeviewer;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

public class CategoriesViewModel extends AndroidViewModel {
    private LiveData<CategoriesData> categoriesData;
    private CategoriesRepo categoriesRepo;

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        String categoriesUrl = application.getString(R.string.onlinelife_domain);
        categoriesRepo = new CategoriesRepo(categoriesUrl);
    }

    public LiveData<CategoriesData> getCategoriesData() {
        if(categoriesData == null) {
            categoriesData = categoriesRepo.getCategoriesData();
        }
        return categoriesData;
    }

    public void refresh() {
        categoriesRepo.refresh();
    }
}
