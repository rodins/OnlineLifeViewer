package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.app.FragmentManager;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class PlaylistsActivity extends ListActivity {
    private final String STATE_MODE = "com.sergeyrodin.MODE";
    private final String STATE_DATA = "com.sergeyrodin.DATA";
    private boolean isPlaylists = true;
    private Playlist mPlaylist;
    private ArrayList<Playlist> mPlaylists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.playlists);

        if(savedInstanceState != null) { //restore saved info
            isPlaylists = savedInstanceState.getBoolean(STATE_MODE);
            if(isPlaylists) {
                mPlaylists = (ArrayList<Playlist>)savedInstanceState.getSerializable(STATE_DATA);
                PlaylistsAdapter adapter = new PlaylistsAdapter(PlaylistsActivity.this, mPlaylists);
                setListAdapter(adapter);
            }else {
                setTitle(R.string.playlist);
                mPlaylist = (Playlist)savedInstanceState.getSerializable(STATE_DATA);
                PlaylistItemAdapter adapter = new PlaylistItemAdapter(PlaylistsActivity.this, mPlaylist);
                setListAdapter(adapter);
            }
        } else {// get new info
            Intent intent = getIntent();
            String js = intent.getStringExtra(MainActivity.EXTRA_JS);
            if(js != null) {
                // Create a progress bar to display while the list loads
                ProgressBar progressBar = new ProgressBar(this);
                progressBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                progressBar.setIndeterminate(true);
                getListView().setEmptyView(progressBar);

                // Must add the progress bar to the root of the layout
                ViewGroup root = (ViewGroup)findViewById(android.R.id.content);
                root.addView(progressBar);

                new PlaylistsAsyncTask().execute(js);
            }else {
                mPlaylist = (Playlist)intent.getSerializableExtra(MainActivity.EXTRA_PLAYLIST);
                if(mPlaylist != null) {
                    isPlaylists = false;
                    setTitle(R.string.playlist);
                    PlaylistItemAdapter adapter = new PlaylistItemAdapter(this, mPlaylist);
                    setListAdapter(adapter);
                }
            }
        }
    }

    private class PlaylistsAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String js = params[0];
            try{
                return new ListParser().getLink(js);
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
                    setListAdapter(adapter);
                } else {
                    //add playlists to ListView
                    PlaylistsAdapter adapter = new PlaylistsAdapter(PlaylistsActivity.this, mPlaylists);
                    setListAdapter(adapter);
                }
            }else {
                ProgressBar pb = (ProgressBar)getListView().getEmptyView();
                if(pb != null) {
                    pb.setVisibility(View.INVISIBLE);
                }
                Toast.makeText(PlaylistsActivity.this, R.string.nothing_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PlaylistsAdapter extends ArrayAdapter<Playlist> {
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
    }

    @Override
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
}
