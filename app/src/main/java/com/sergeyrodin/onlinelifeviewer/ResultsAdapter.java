package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by root on 10.05.16.
 */
class ResultsAdapter extends ArrayAdapter<Result>{
    private View[] views;
    private List<Result> results;

    ResultsAdapter(Activity activity, List<Result> results) {
        super(activity, R.layout.result_entry, results);
        this.results = results;
        views = new View[getCount()];
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(views[position] != null) {
            return views[position];
        }else{
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.result_entry, parent, false);
            Result result = getItem(position);
            TextView textView = (TextView)view.findViewById(R.id.resultEntryTitle);
            ImageView imageView = (ImageView)view.findViewById(R.id.resultEntryImage);
            textView.setText(result.title);
            if(result.image != null) {
                new ImageLoadTask(result, imageView).execute();
            }
            if(result.getBitmap() != null) {
                imageView.setImageBitmap(result.getBitmap());
            }
            views[position] = view;
            return view;
        }
    }

    public List<Result> getResults() {
        return results;
    }
}
