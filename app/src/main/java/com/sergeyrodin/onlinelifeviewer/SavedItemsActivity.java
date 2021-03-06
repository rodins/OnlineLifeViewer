package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;
import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

import java.util.List;

// TODO: display images
public class SavedItemsActivity extends AppCompatActivity
               implements SavedItemsAdapter.SavedItemClickListener {
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

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final AppDatabase db = AppDatabase.getsInstanse(getApplicationContext());
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<SavedItem> savedItems = mSavedItemsAdapter.getSavedItems();
                        db.savedItemsDao().deleteSavedItem(savedItems.get(position));
                    }
                });
            }
        }).attachToRecyclerView(mRvSaveItems);
    }

    @Override
    public void onSavedItemClick(int position) {
        SavedItem savedItem = mSavedItemsAdapter.getSavedItems().get(position);
        startActorsActivity(savedItem.getTitle(), savedItem.getLink());
    }

    private void startActorsActivity(String title, String link) {
        Intent intent = new Intent(this, ActorsActivity.class);
        intent.putExtra(MainActivity.EXTRA_TITLE, title);
        intent.putExtra(MainActivity.EXTRA_LINK, link);
        startActivity(intent);
    }
}
