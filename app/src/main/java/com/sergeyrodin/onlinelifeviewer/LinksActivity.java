package com.sergeyrodin.onlinelifeviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinksActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<LinksActivity.LinksLoadingResult> {
    private static final String LINKS_EXTRA = "playlists";
    private static final String JS_EXTRA = "js";

    private Episodes mEpisodes;
    private ArrayList<Episodes> mSeasons;
    private ProgressBar pbLoadingIndicator;
    private Button btnFilm;
    private TextView tvLoadingError;
    private ListView lvEpisodes;
    private ExpandableListView elvSeasons;
    private boolean mIsCalledTwice;
    private String mInfoTitle;
    private String mInfoLink;
    private VideoItem mVideoItem;

    private AdapterView.OnItemClickListener mMessageClickedHandler;

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
            // Send link to loader
            mIsCalledTwice = false;
            showLoadingIndicator();
            Bundle linksBundle = new Bundle();
            linksBundle.putString(LINKS_EXTRA, mInfoLink);
            android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
            int LINKS_LOADER = 23;
            loaderManager.initLoader(LINKS_LOADER, linksBundle, this);
        }

        if(intent.hasExtra(MainActivity.EXTRA_JS)) { // Called from ActorsActivity
            String js = intent.getStringExtra(MainActivity.EXTRA_JS);
            mIsCalledTwice = false;
            showLoadingIndicator();
            Bundle linksBundle = new Bundle();
            linksBundle.putString(JS_EXTRA, js);
            android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
            int LINKS_LOADER = 23;
            loaderManager.initLoader(LINKS_LOADER, linksBundle, this);
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
    }

    private void onPlaylistItemClick(int position) {
        VideoItem videoItem = mEpisodes.getItems().get(position);
        ProcessVideoItem.process(LinksActivity.this, videoItem);
    }

    public void btnFilmClicked(View view) {
        ProcessVideoItem.process(LinksActivity.this, mVideoItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mInfoLink != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.playlists, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_actors) {
            ProcessVideoItem.startActorsActivity(this, mInfoTitle, mInfoLink);
            return true;
        }
        if(itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<LinksLoadingResult> onCreateLoader(int id, Bundle args) {
        return new LinksAsyncTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<LinksLoadingResult> loader,
                               LinksLoadingResult data) {
        if(!mIsCalledTwice) {
            mIsCalledTwice = true;
            mVideoItem = new VideoItemParser().getItem(data.js);
            if(mVideoItem.getComment() != null) { // film found
                setTitle(R.string.film);
                btnFilm.setText(mInfoTitle);
                showFilmData();
            }else if (data.seasonsJson != null) { // seasons json found
                mSeasons = new SeasonsParser().getItems(data.seasonsJson);
                if (mSeasons.size() == 0) { // episodes parsed
                    //Add episodes to ListView
                    setTitle(R.string.episodes);
                    mEpisodes = new EpisodesParser().getItem(data.seasonsJson);
                    EpisodesAdapter adapter = new EpisodesAdapter(LinksActivity.this, mEpisodes);
                    lvEpisodes.setAdapter(adapter);
                    showEpisodesData();
                } else { // seasons parsed
                    setTitle(R.string.seasons);
                    seasonsToAdapter(mSeasons);
                    showSeasonsData();
                }
            }else {
                // Show error here is almost impossible
                showLoadingError();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<LinksLoadingResult> loader) {

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

    private void seasonsToAdapter(List<Episodes> playlists) {
        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();
        for(Episodes episodes : playlists) {
            Map<String, String> map = new HashMap<>();
            map.put("entryText", episodes.getTitle());
            groupData.add(map);
            List<Map<String, String>> groupList = new ArrayList<>();
            for(VideoItem videoItem : episodes.getItems()) {
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

    static class LinksAsyncTaskLoader extends AsyncTaskLoader<LinksLoadingResult> {
        private Bundle args;

        LinksAsyncTaskLoader(Context context, Bundle args) {
            super(context);
            this.args = args;
        }

        @Override
        protected void onStartLoading() {
            if(args == null) {
                return;
            }
            forceLoad();
        }

        @Override
        public LinksLoadingResult loadInBackground() {
            String link = args.getString(LINKS_EXTRA);
            URL url;
            String js;
            try{
                if(link != null) {
                    url = new URL(link);
                    js = NetworkUtils.getConstantLinksJs(url); // js from constant links
                }else {
                    js = args.getString(JS_EXTRA); // js from actors activity
                }
                if(js != null) {
                    String seasonsJson = new SeasonsJsonParser().getSeasonsJson(js);
                    return new LinksLoadingResult(js, seasonsJson);
                }else {
                    return null;
                }
            }catch (IOException e) {
                System.err.println(e.toString());
                return null;
            }
        }
    }

    static class LinksLoadingResult {
        final String js;
        final String seasonsJson;
        LinksLoadingResult(String js, String seasonsJson) {
            this.js = js;
            this.seasonsJson = seasonsJson;
        }
    }
}
