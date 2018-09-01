package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by root on 11.05.16.
 */
public class EpisodesAdapter extends ArrayAdapter<VideoItem> {
    private View[] views;
    public EpisodesAdapter(Activity activity, Episodes episodes) {
        super(activity, R.layout.playlist_entry, episodes.getItems());
        views = new View[getCount()];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(views[position] != null) {
            return views[position];
        }else {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            View view = inflater.inflate(R.layout.playlist_entry, parent, false);

            VideoItem videoItem = getItem(position);
            TextView textView = view.findViewById(R.id.entryText);
            ImageView imageView = view.findViewById(R.id.entryImage);

            textView.setText(videoItem.getComment());
            imageView.setImageResource(R.drawable.ic_link2);
            views[position] = view;

            return view;
        }
    }
}
