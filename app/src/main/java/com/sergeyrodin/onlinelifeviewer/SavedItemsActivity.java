package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

import java.util.List;

public class SavedItemsActivity extends AppCompatActivity
               implements SavedItemsAdapter.SavedItemClickListener {
    private static final String LOG_TAG = SavedItemsActivity.class.getSimpleName();
    private RecyclerView mRvSaveItems;
    private TextView mTvNoItems;
    private SavedItemsAdapter mSavedItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        mRvSaveItems = findViewById(R.id.rv_saved_items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvSaveItems.setLayoutManager(layoutManager);
        mSavedItemsAdapter = new SavedItemsAdapter(this);
        mRvSaveItems.setAdapter(mSavedItemsAdapter);

        mTvNoItems = findViewById(R.id.tv_no_saved_items);

        SavedItemsViewModel viewModel = ViewModelProviders.of(this)
                .get(SavedItemsViewModel.class);
        viewModel.getSavedItems().observe(this, new Observer<List<SavedItem>>() {
            @Override
            public void onChanged(@Nullable List<SavedItem> savedItems) {
                if(savedItems == null || savedItems.isEmpty()) {
                    mRvSaveItems.setVisibility(View.INVISIBLE);
                    mTvNoItems.setVisibility(View.VISIBLE);
                }else {
                    mTvNoItems.setVisibility(View.INVISIBLE);
                    mRvSaveItems.setVisibility(View.VISIBLE);
                    mSavedItemsAdapter.setSavedItems(savedItems);
                }
            }
        });
    }

    @Override
    public void onSavedItemClick(int position) {
        Log.d(LOG_TAG, mSavedItemsAdapter.getSavedItems().get(position).getTitle());
    }
}
