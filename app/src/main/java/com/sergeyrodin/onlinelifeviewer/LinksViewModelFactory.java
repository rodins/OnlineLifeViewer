package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;

public class LinksViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private AppDatabase mDb;
    private String mLink;

    public LinksViewModelFactory(AppDatabase db, String link) {
        mDb = db;
        mLink = link;
    }

    // Note: This can be reused with minor modifications
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new LinksViewModel(mDb, mLink);
    }
}
