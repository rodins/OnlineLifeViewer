package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;
import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

import java.util.ArrayList;
import java.util.List;

public class ActorsActivity extends AppCompatActivity implements ActorsAdapter.ListItemClickListener {

    private RecyclerView mRvActors;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorTextView;

    private List<Link> mActors = new ArrayList<>();
    private String mPlayerLink;
    private String mTitle, mResultTitle, mResultLink;
    private FloatingActionButton mFabButtonLinks;

    private MenuItem mActionSave, mActionOpen;
    private boolean mIsItemSaved = true;
    private ActorsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actors);

        // Add back button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvActors = findViewById(R.id.rv_actors);
        mRvActors.setLayoutManager(layoutManager);
        mLoadingIndicator = findViewById(R.id.actors_loading_indicator);
        mErrorTextView = findViewById(R.id.actors_loading_error);
        mErrorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewModel != null) {
                    viewModel.retry();
                }
            }
        });

        Intent intent = getIntent();
        if(intent.hasExtra(MainActivity.EXTRA_TITLE)) {
            mResultTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE);
            setTitle(mResultTitle);
        }else {
            setTitle(R.string.actors_title);
        }

        if(intent.hasExtra(MainActivity.EXTRA_LINK)) {
            mResultLink = intent.getStringExtra(MainActivity.EXTRA_LINK);
            AppDatabase db = AppDatabase.getsInstanse(this);
            ActorsViewModelFactory factory = new ActorsViewModelFactory(db, mResultLink);
            viewModel = ViewModelProviders.of(this, factory).get(ActorsViewModel.class);
            viewModel.getActorsData().observe(this, new Observer<ActorsData>() {
                @Override
                public void onChanged(@Nullable ActorsData actorsData) {
                    if(actorsData != null) {
                        for(Actor actor: actorsData.getActors()) {
                            String title = actor.title + " " + (actor.isDirector?"(" + getString(R.string.director) + ")":"");
                            mActors.add(new Link(title, actor.href));
                        }
                        ActorsAdapter adapter = new ActorsAdapter(mActors,
                                ActorsActivity.this);
                        mRvActors.setAdapter(adapter);

                        if(actorsData.getCountry() != null && actorsData.getYear() != null) {
                            mTitle = mResultTitle + " - " + actorsData.getCountry() + " - " +
                                    actorsData.getYear();
                            setTitle(mTitle);
                        }

                        if(actorsData.getPlayerLink() != null) {
                            mPlayerLink = actorsData.getPlayerLink();
                        }

                        mFabButtonLinks.setVisibility(View.VISIBLE);
                        mActionOpen.setVisible(true);
                    }
                }
            });

            viewModel.getState().observe(this, new Observer<State>() {
                @Override
                public void onChanged(@Nullable State state) {
                    if(state != null) {
                        switch(state) {
                            case LOADING_INIT:
                                showLoadingIndicator();
                                break;
                            case DONE:
                                showData();
                                break;
                            case EMPTY:
                                showEmpty();
                                break;
                            case ERROR_INIT:
                                showError();
                                break;
                        }
                    }
                }
            });

            viewModel.getSavedItem().observe(this, new Observer<SavedItem>() {
                @Override
                public void onChanged(@Nullable SavedItem savedItem) {
                    viewModel.getSavedItem().removeObserver(this);
                    mIsItemSaved = savedItem != null;
                    if(mActionSave != null) {
                        mActionSave.setVisible(!mIsItemSaved);
                    }
                }
            });
        }

        mFabButtonLinks = findViewById(R.id.fab_links);

        mFabButtonLinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionOpenClicked();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actors_menu, menu);

        mActionOpen = menu.findItem(R.id.action_open);

        mActionSave = menu.findItem(R.id.action_save);
        mActionSave.setVisible(!mIsItemSaved);

        return super.onCreateOptionsMenu(menu);
    }

    private void actionOpenClicked() {
        if(mPlayerLink != null) {
            Uri uri = Uri.parse(mPlayerLink);
            Intent viewMediaIntent = new Intent();
            viewMediaIntent.setAction(Intent.ACTION_VIEW);
            viewMediaIntent.setData(uri);
            viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if(viewMediaIntent.resolveActivity(getPackageManager()) != null){
                startActivity(viewMediaIntent);
            }else {
                Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_save) {
            saveItem();
            return true;
        }

        if(itemId == R.id.action_open) {
            actionOpenClicked();
            return true;
        }

        if(itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLoadingIndicator() {
        mRvActors.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
    }

    private void showData() {
        mRvActors.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        mRvActors.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.setText(R.string.network_problem);
    }

    private void showEmpty() {
        mRvActors.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.setText(R.string.no_actors);
    }

    @Override
    public void onListItemClick(int index) {
        Link link = mActors.get(index);
        startResultsActivity(link.Title, link.Href);
    }

    private void startResultsActivity(String title, String link) {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(MainActivity.EXTRA_TITLE, title);
        intent.putExtra(MainActivity.EXTRA_LINK, link);
        startActivity(intent);
    }

    private void saveItem() {
        mActionSave.setVisible(false);
        Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show();
        final SavedItem savedItem = new SavedItem(mResultTitle, mResultLink, "");
        final AppDatabase db = AppDatabase.getsInstanse(this);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.savedItemsDao().insertSavedItem(savedItem);
            }
        });
    }
}
