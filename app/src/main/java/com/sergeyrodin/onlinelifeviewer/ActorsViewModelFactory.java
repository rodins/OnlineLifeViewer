package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;

public class ActorsViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private String link;
    private AppDatabase db;

    public ActorsViewModelFactory(AppDatabase db, String link) {
        this.db = db;
        this.link = link;
    }

    // Note: This can be reused with minor modifications
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new ActorsViewModel(db, link);
    }
}
