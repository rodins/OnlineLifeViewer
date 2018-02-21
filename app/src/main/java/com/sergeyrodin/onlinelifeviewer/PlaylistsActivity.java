package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlists);
        setTitle(R.string.playlists);

        pbLoadingIndicator = (ProgressBar)findViewById(R.id.playlists_loading_indicator);
        tvLoadingError = (TextView)findViewById(R.id.playlists_loading_error);
        lvPlaylist = (ListView)findViewById(R.id.lv_playlist);

        if(savedInstanceState != null) { //restore saved info
            isPlaylists = savedInstanceState.getBoolean(STATE_MODE);
            if(isPlaylists) {
                mPlaylists = (ArrayList<Playlist>)savedInstanceState.getSerializable(STATE_DATA);
                /*PlaylistsAdapter adapter = new PlaylistsAdapter(PlaylistsActivity.this, mPlaylists);
                setListAdapter(adapter);*/
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
                // Create a progress bar to display while the list loads
                /*ProgressBar progressBar = new ProgressBar(this);
                progressBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                progressBar.setIndeterminate(true);
                getListView().setEmptyView(progressBar);

                // Must add the progress bar to the root of the layout
                ViewGroup root = (ViewGroup)findViewById(android.R.id.content);
                root.addView(progressBar);*/

                new PlaylistsAsyncTask().execute(js);
            }/*else {
                mPlaylist = (Playlist)intent.getSerializableExtra(MainActivity.EXTRA_PLAYLIST);
                if(mPlaylist != null) {
                    isPlaylists = false;
                    setTitle(R.string.playlist);
                    PlaylistItemAdapter adapter = new PlaylistItemAdapter(this, mPlaylist);
                    setListAdapter(adapter);
                }
            }*/
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
                    //add playlists to ExpandableListView
                    /*PlaylistsAdapter adapter = new PlaylistsAdapter(PlaylistsActivity.this, mPlaylists);
                    setListAdapter(adapter);*/
                    playlistsToAdapter(mPlaylists);
                    showPlaylistsData();
                }
            }else {
                showLoadingError();
                /*ProgressBar pb = (ProgressBar)getListView().getEmptyView();
                if(pb != null) {
                    pb.setVisibility(View.INVISIBLE);
                }
                Toast.makeText(PlaylistsActivity.this, R.string.nothing_found, Toast.LENGTH_SHORT).show();*/
            }
        }
    }

    /*private class PlaylistsAdapter extends ArrayAdapter<Playlist> {
        private View[] views;

        PlaylistsAdapter(Activity activity, ArrayList<Playlist> playlists){
            super(activity, R.layout.playlist_entry, playlists);
            views = new View[playlists.size()];
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if(views[position] != null) {
                return views[position];
            }else {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View view = inflater.inflate(R.layout.playlist_entry, parent, false);

                Playlist playlist = getItem(position);
                TextView textView = (TextView)view.findViewById(R.id.entryText);
                ImageView imageView = (ImageView)view.findViewById(R.id.entryImage);

                if (playlist != null) {
                    textView.setText(playlist.getTitle());
                }
                imageView.setImageResource(R.drawable.movies_folder);
                views[position] = view;

                return view;
            }
        }
    }*/

    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Object obj = l.getAdapter().getItem(position);
        if(obj instanceof Playlist){
            Playlist playlist = (Playlist)obj;
            Intent intent = new Intent(this, PlaylistsActivity.class);
            intent.putExtra(MainActivity.EXTRA_PLAYLIST, playlist);
            startActivity(intent);
        }else if(obj instanceof PlaylistItem) {
            PlaylistItem playlistItem = (PlaylistItem)obj;
            ProcessPlaylistItem.process(this, playlistItem);
        }
    }*/

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
