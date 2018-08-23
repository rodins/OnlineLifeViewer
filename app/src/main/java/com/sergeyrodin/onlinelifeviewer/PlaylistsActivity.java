package com.sergeyrodin.onlinelifeviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final String PLAYLISTS_JS_EXTRA = "playlists";

    private Playlist mPlaylist;
    private ArrayList<Playlist> mPlaylists;
    private ProgressBar pbLoadingIndicator;
    private TextView tvLoadingError;
    private ListView lvPlaylist;
    private ExpandableListView elvPlaylists;
    private boolean mIsCalledTwice;
    private String mInfoTitle;
    private String mInfoLink;

    private AdapterView.OnItemClickListener mMessageClickedHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);
        setTitle(R.string.playlists);

        pbLoadingIndicator = findViewById(R.id.playlists_loading_indicator);
        tvLoadingError = findViewById(R.id.playlists_loading_error);
        lvPlaylist = findViewById(R.id.lv_playlist);
        lvPlaylist.setOnItemClickListener(mMessageClickedHandler);

        elvPlaylists = findViewById(R.id.elv_playlists);
        elvPlaylists.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                PlaylistItem playlistItem = mPlaylists.get(groupPosition).getItems().get(childPosition);
                ProcessPlaylistItem.process(PlaylistsActivity.this, playlistItem);
                return true;
            }
        });

        Intent intent = getIntent();

        if(intent.hasExtra(MainActivity.EXTRA_TITLE)) {
            mInfoTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE);
            Log.d(getClass().getSimpleName(), "InfoTitle: " + mInfoTitle);
        }

        if(intent.hasExtra(MainActivity.EXTRA_LINK)) {
            mInfoLink = intent.getStringExtra(MainActivity.EXTRA_LINK);
            Log.d(getClass().getSimpleName(), "InfoLink: " + mInfoLink);
        }

        if(intent.hasExtra(MainActivity.EXTRA_JS)) {
            String js = intent.getStringExtra(MainActivity.EXTRA_JS);
            mIsCalledTwice = false;
            showLoadingIndicator();
            Bundle playlistsBundle = new Bundle();
            playlistsBundle.putString(PLAYLISTS_JS_EXTRA, js);
            android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
            int PLAYLISTS_LOADER = 23;
            loaderManager.initLoader(PLAYLISTS_LOADER, playlistsBundle, this);
        }

        mMessageClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                onPlaylistItemClick(position);
            }
        };

        lvPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onPlaylistItemClick(position);
            }
        });
    }

    private void onPlaylistItemClick(int position) {
        PlaylistItem playlistItem = mPlaylist.getItems().get(position);
        ProcessPlaylistItem.process(PlaylistsActivity.this, playlistItem);
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new PlaylistsAsyncTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if(!mIsCalledTwice) {
            mIsCalledTwice = true;

            if (data != null) { //playlist found
                mPlaylists = new PlaylistsParser().getItems(data);
                if (mPlaylists.size() == 0) {
                    //Add playlist to ListView
                    setTitle(R.string.playlist);
                    mPlaylist = new PlaylistParser().getItem(data);
                    PlaylistItemAdapter adapter = new PlaylistItemAdapter(PlaylistsActivity.this, mPlaylist);
                    lvPlaylist.setAdapter(adapter);
                    showPlaylistData();
                } else {
                    playlistsToAdapter(mPlaylists);
                    showPlaylistsData();
                }
            }else {
                // Show error here is almost impossible
                showLoadingError();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    private void showLoadingIndicator() {
        pbLoadingIndicator.setVisibility(View.VISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvPlaylist.setVisibility(View.INVISIBLE);
        elvPlaylists.setVisibility(View.INVISIBLE);
    }

    private void showPlaylistsData() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvPlaylist.setVisibility(View.INVISIBLE);
        elvPlaylists.setVisibility(View.VISIBLE);
    }

    private void showPlaylistData() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvPlaylist.setVisibility(View.VISIBLE);
        elvPlaylists.setVisibility(View.INVISIBLE);
    }

    private void showLoadingError() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.VISIBLE);
        lvPlaylist.setVisibility(View.INVISIBLE);
        elvPlaylists.setVisibility(View.INVISIBLE);
    }

    private void playlistsToAdapter(List<Playlist> playlists) {
        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();
        for(Playlist playlist : playlists) {
            Map<String, String> map = new HashMap<>();
            map.put("entryText", playlist.getTitle());
            groupData.add(map);
            List<Map<String, String>> groupList = new ArrayList<>();
            for(PlaylistItem playlistItem : playlist.getItems()) {
                Map<String, String> childMap = new HashMap<>();
                childMap.put("entryTextSubcategories", playlistItem.getComment());
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
        elvPlaylists.setAdapter(adapter);
    }

    static class PlaylistsAsyncTaskLoader extends AsyncTaskLoader<String> {
        private Bundle args;

        PlaylistsAsyncTaskLoader(Context context, Bundle args) {
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
        public String loadInBackground() {
            String js = args.getString(PLAYLISTS_JS_EXTRA);
            try{
                return new ListParser().getPlaylistJson(js);
            }catch (IOException e) {
                System.err.println(e.toString());
                return null;
            }
        }
    }
}
