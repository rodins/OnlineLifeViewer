package com.sergeyrodin.onlinelifeviewer;


import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class PlaylistsActivity extends ExpandableListActivity {
    private final String STATE_MODE = "com.sergeyrodin.MODE";
    private final String STATE_DATA = "com.sergeyrodin.DATA";
    private boolean isPlaylists = true;
    private Playlist mPlaylist;
    private ArrayList<Playlist> mPlaylists;
    private ProgressBar pbLoadingIndicator;
    private TextView tvLoadingError;
    private ListView lvPlaylist;

    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            PlaylistItem playlistItem = mPlaylist.getItems().get(position);
            ProcessPlaylistItem.process(PlaylistsActivity.this, playlistItem);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);
        setTitle(R.string.playlists);

        pbLoadingIndicator = findViewById(R.id.playlists_loading_indicator);
        tvLoadingError = findViewById(R.id.playlists_loading_error);
        lvPlaylist = findViewById(R.id.lv_playlist);
        lvPlaylist.setOnItemClickListener(mMessageClickedHandler);

        if(savedInstanceState != null) { //restore saved info
            isPlaylists = savedInstanceState.getBoolean(STATE_MODE);
            if(isPlaylists) {
                mPlaylists = (ArrayList<Playlist>)savedInstanceState.getSerializable(STATE_DATA);
                playlistsToAdapter(mPlaylists);
                showPlaylistsData();
            }else {
                setTitle(R.string.playlist);
                mPlaylist = (Playlist)savedInstanceState.getSerializable(STATE_DATA);
                PlaylistItemAdapter adapter = new PlaylistItemAdapter(PlaylistsActivity.this, mPlaylist);
                lvPlaylist.setAdapter(adapter);
                showPlaylistData();
            }
        } else {// get new info
            Intent intent = getIntent();
            String js = intent.getStringExtra(MainActivity.EXTRA_JS);
            if(js != null) {
                new PlaylistsAsyncTask().execute(js);
            }
        }
    }

    private class PlaylistsAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoadingIndicator();
        }

        @Override
        protected String doInBackground(String... params) {
            String js = params[0];
            try{
                return new ListParser().getPlaylistJson(js);
            }catch (IOException e) {
                System.err.println(e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String ps) {
            if (ps != null) { //playlist found
                mPlaylists = new PlaylistsParser().getItems(ps);
                if (mPlaylists.size() == 0) {
                    //Add playlist to ListView
                    setTitle(R.string.playlist);
                    isPlaylists = false;
                    mPlaylist = new PlaylistParser().getItem(ps);
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
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        PlaylistItem playlistItem = mPlaylists.get(groupPosition).getItems().get(childPosition);
        ProcessPlaylistItem.process(this, playlistItem);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_MODE, isPlaylists);
        if(isPlaylists) {
            outState.putSerializable(STATE_DATA, mPlaylists);
        }else {
            outState.putSerializable(STATE_DATA, mPlaylist);
        }
        super.onSaveInstanceState(outState);
    }

    private void showLoadingIndicator() {
        pbLoadingIndicator.setVisibility(View.VISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvPlaylist.setVisibility(View.INVISIBLE);
        getExpandableListView().setVisibility(View.INVISIBLE);
    }

    private void showPlaylistsData() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvPlaylist.setVisibility(View.INVISIBLE);
        getExpandableListView().setVisibility(View.VISIBLE);
    }

    private void showPlaylistData() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        lvPlaylist.setVisibility(View.VISIBLE);
        getExpandableListView().setVisibility(View.INVISIBLE);
    }

    private void showLoadingError() {
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.VISIBLE);
        lvPlaylist.setVisibility(View.INVISIBLE);
        getExpandableListView().setVisibility(View.INVISIBLE);
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
        setListAdapter(adapter);
    }
}
