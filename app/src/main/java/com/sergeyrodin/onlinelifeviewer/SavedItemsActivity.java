package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

import java.util.List;

public class SavedItemsActivity extends AppCompatActivity {
    private static final String LOG_TAG = SavedItemsActivity.class.getSimpleName();
    private RecyclerView mRvSaveItems;
    private TextView mTvNoItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        mRvSaveItems = findViewById(R.id.rv_saved_items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvSaveItems.setLayoutManager(layoutManager);

        mTvNoItems = findViewById(R.id.tv_no_saved_items);

        SavedItemsViewModel viewModel = ViewModelProviders.of(this)
                .get(SavedItemsViewModel.class);
        viewModel.getSavedItems().observe(this, new Observer<List<SavedItem>>() {
            @Override
            public void onChanged(@Nullable List<SavedItem> savedItems) {
                if(savedItems == null || savedItems.isEmpty()) {
                    Log.d(LOG_TAG, "No items saved");
                }else {
                    for(SavedItem item : savedItems) {
                        Log.d(LOG_TAG, item.getTitle());
                    }
                }
            }
        });
    }
}
