package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;
import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

import java.util.List;

public class SavedItemsActivity extends AppCompatActivity
               implements SavedItemsAdapter.SavedItemClickListener {
    private static final String LOG_TAG = SavedItemsActivity.class.getSimpleName();
    private RecyclerView mRvSaveItems;
    private TextView mTvNoItems;
    private SavedItemsAdapter mSavedItemsAdapter;
    private boolean mIsShowActorsOnClick;

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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsShowActorsOnClick = sharedPreferences.getBoolean(getString(R.string.pref_actors_key),
                getResources().getBoolean(R.bool.pref_actors_default_value));
    }

    @Override
    public void onSavedItemClick(int position) {
        Log.d(LOG_TAG, mSavedItemsAdapter.getSavedItems().get(position).getTitle());
        SavedItem savedItem = mSavedItemsAdapter.getSavedItems().get(position);
        // TODO: this will not always work correct on trailers in LinksActivity
        // I have to pass additional isTrailer boolean to LinksActivity and store it
        // in database. And here I should always open ActorsActivity on trailers.
        // TODO: trailer is currently impossible to save as it does not open in LinksActivity.
        // Should probably also implement saving in ActorsActivity
        if(mIsShowActorsOnClick) { // Use actors links
            ProcessVideoItem.startActorsActivity(this,
                    savedItem.getTitle(),
                    savedItem.getLink());
        }else { // Use constant links
            // Find links in LinksActivity
            Intent intent = new Intent(this, LinksActivity.class);
            intent.putExtra(MainActivity.EXTRA_TITLE, savedItem.getTitle());
            intent.putExtra(MainActivity.EXTRA_LINK, savedItem.getLink());
            startActivity(intent);
        }
    }
}
