package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sergeyrodin.onlinelifeviewer.database.AppDatabase;
import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinksActivity extends AppCompatActivity {
    private Season mSeason;
    private List<Season> mSeasons;
    private ProgressBar pbLoadingIndicator;
    private Button btnFilm;
    private TextView tvLoadingError;
    private ListView lvEpisodes;
    private ExpandableListView elvSeasons;
    private String mInfoTitle;
    private String mInfoLink;
    private VideoItem mVideoItem;

    private AdapterView.OnItemClickListener mMessageClickedHandler;
    private MenuItem mActionSave;
    private boolean mIsItemSaved = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_links);
        setTitle(R.string.links);

        // Add back button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pbLoadingIndicator = findViewById(R.id.playlists_loading_indicator);
        tvLoadingError = findViewById(R.id.playlists_loading_error);

        btnFilm = findViewById(R.id.btn_film);

        lvEpisodes = findViewById(R.id.lv_playlist);
        lvEpisodes.setOnItemClickListener(mMessageClickedHandler);

        elvSeasons = findViewById(R.id.elv_playlists);
        elvSeasons.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                VideoItem videoItem = mSeasons.get(groupPosition).getItems().get(childPosition);
                ProcessVideoItem.process(LinksActivity.this, videoItem);
                return true;
            }
        });

        Intent intent = getIntent();

        if(intent.hasExtra(MainActivity.EXTRA_TITLE)) {
            mInfoTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        }

        if(intent.hasExtra(MainActivity.EXTRA_LINK)) { // Called from ResultsActivity
            mInfoLink = intent.getStringExtra(MainActivity.EXTRA_LINK);
            setupViewModel(mInfoLink, null);
        }

        if(intent.hasExtra(MainActivity.EXTRA_JS)) { // Called from ActorsActivity
            String js = intent.getStringExtra(MainActivity.EXTRA_JS);
            // TODO: check if link is saved does not work here. Fix.
            setupViewModel(mInfoLink, js);
        }

        mMessageClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                onPlaylistItemClick(position);
            }
        };

        lvEpisodes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onPlaylistItemClick(position);
            }
        });

        FloatingActionButton fabButtonActors = findViewById(R.id.fab_actors);

        if(mInfoLink == null) {
            fabButtonActors.setVisibility(View.INVISIBLE);
        }

        fabButtonActors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProcessVideoItem.startActorsActivity(LinksActivity.this, mInfoTitle, mInfoLink);
            }
        });
    }

    private void setupViewModel(String link, String js) {
        AppDatabase db = AppDatabase.getsInstanse(this);
        LinksViewModelFactory factory = new LinksViewModelFactory(db, link, js);
        final LinksViewModel viewModel = ViewModelProviders.of(this, factory).get(LinksViewModel.class);
        viewModel.getmSavedItem().observe(this, new Observer<SavedItem>() {
            @Override
            public void onChanged(@Nullable SavedItem savedItem) {
                viewModel.getmSavedItem().removeObserver(this);
                mIsItemSaved = savedItem != null;
                if(mActionSave != null) {
                    mActionSave.setVisible(!mIsItemSaved);
                }
            }
        });
        viewModel.getLinkData().observe(this, new Observer<LinkData>() {
            @Override
            public void onChanged(@Nullable LinkData linkData) {
                if(linkData != null) {
                    if(linkData.isLoading()) {
                        showLoadingIndicator();
                    }else if(linkData.getVideoItem() != null) {
                        mVideoItem = linkData.getVideoItem();
                        setTitle(R.string.film);
                        btnFilm.setText(mInfoTitle);
                        showFilmData();
                    }else if(linkData.getSeason() != null) {
                        //Add episodes to ListView
                        setTitle(R.string.season);
                        mSeason = linkData.getSeason();
                        EpisodesAdapter adapter = new EpisodesAdapter(LinksActivity.this, mSeason);
                        lvEpisodes.setAdapter(adapter);
                        showEpisodesData();
                    }else if(linkData.getSeasons() != null) {
                        mSeasons = linkData.getSeasons();
                        setTitle(R.string.seasons);
                        seasonsToAdapter(mSeasons);
                        showSeasonsData();
                    }else if(linkData.isError()) {
                        showLoadingError();
                    }
                }
            }
        });
    }

    private void onPlaylistItemClick(int position) {
        VideoItem videoItem = mSeason.getItems().get(position);
        ProcessVideoItem.process(LinksActivity.this, videoItem);
    }

    public void btnFilmClicked(View view) {
        ProcessVideoItem.process(LinksActivity.this, mVideoItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.links_menu, menu);
        mActionSave = menu.findItem(R.id.action_save);
        mActionSave.setVisible(!mIsItemSaved);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_save) {
            saveItem();
            return true;
        }
        if(itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItem() {
        mActionSave.setVisible(false);
        Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show();
        final SavedItem savedItem = new SavedItem(mInfoTitle, mInfoLink, "");
        final AppDatabase db = AppDatabase.getsInstanse(this);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.savedItemsDao().insertSavedItem(savedItem);
            }
        });
    }

    private void showLoadingIndicator() {
        pbLoadingIndicator.setVisibility(View.VISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvEpisodes.setVisibility(View.INVISIBLE);
        elvSeasons.setVisibility(View.INVISIBLE);
        btnFilm.setVisibility(View.INVISIBLE);
    }

    private void showFilmData() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvEpisodes.setVisibility(View.INVISIBLE);
        elvSeasons.setVisibility(View.INVISIBLE);
        btnFilm.setVisibility(View.VISIBLE);
    }

    private void showSeasonsData() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvEpisodes.setVisibility(View.INVISIBLE);
        elvSeasons.setVisibility(View.VISIBLE);
        btnFilm.setVisibility(View.INVISIBLE);
    }

    private void showEpisodesData() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvEpisodes.setVisibility(View.VISIBLE);
        elvSeasons.setVisibility(View.INVISIBLE);
        btnFilm.setVisibility(View.INVISIBLE);
    }

    private void showLoadingError() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.VISIBLE);
        lvEpisodes.setVisibility(View.INVISIBLE);
        elvSeasons.setVisibility(View.INVISIBLE);
        btnFilm.setVisibility(View.INVISIBLE);
    }

    private void seasonsToAdapter(List<Season> seasons) {
        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();
        for(Season season : seasons) {
            Map<String, String> map = new HashMap<>();
            map.put("entryText", season.getTitle());
            groupData.add(map);
            List<Map<String, String>> groupList = new ArrayList<>();
            for(VideoItem videoItem : season.getItems()) {
                Map<String, String> childMap = new HashMap<>();
                childMap.put("entryTextSubcategories", videoItem.getComment());
                groupList.add(childMap);
            }
            childData.add(groupList);
        }

        String[] groupFrom = {"entryText"};
        int[] groupTo = {R.id.entryText};
        String[] childFrom = {"entryTextSubcategories"};
        int[] childTo = {R.id.entry_text_subcategories};

        ExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                R.layout.categories_entry,
                groupFrom,
                groupTo,
                childData,
                R.layout.subcategories_entry,
                childFrom,
                childTo
        );
        elvSeasons.setAdapter(adapter);
    }
}
