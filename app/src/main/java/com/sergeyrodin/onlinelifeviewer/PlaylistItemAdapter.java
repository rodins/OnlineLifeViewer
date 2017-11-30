package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by root on 11.05.16.
 */
public class PlaylistItemAdapter extends ArrayAdapter<PlaylistItem> {
    private View[] views;
    public PlaylistItemAdapter(Activity activity, Playlist playlist) {
        super(activity, R.layout.playlist_entry, playlist.getItems());
        views = new View[getCount()];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(views[position] != null) {
            return views[position];
        }else {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            View view = inflater.inflate(R.layout.playlist_entry, parent, false);

            PlaylistItem playlistItem = getItem(position);
            TextView textView = (TextView)view.findViewById(R.id.entryText);
            ImageView imageView = (ImageView)view.findViewById(R.id.entryImage);

            textView.setText(playlistItem.getComment());
            imageView.setImageResource(R.drawable.film);
            views[position] = view;

            return view;
        }
    }
}
