package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class ActorsViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private String link;

    public ActorsViewModelFactory(String link) {
        this.link = link;
    }

    // Note: This can be reused with minor modifications
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new ActorsViewModel(link);
    }
}
