package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>{
    //private View[] views;
    private List<Result> results;
    private final String TAG = ResultsAdapter.class.getSimpleName();

    ResultsAdapter(List<Result> results) {
        //super(activity, R.layout.result_entry, results);
        this.results = results;
        //views = new View[getCount()];
    }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.result_entry, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        Result result = results.get(position);
        holder.bind(result);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    /*@NonNull
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
    }*/

    /*public List<Result> getResults() {
        return results;
    }*/

    class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleView;
        ImageView mImageView;
        ResultViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView)itemView.findViewById(R.id.resultEntryTitle);
            mImageView = (ImageView)itemView.findViewById(R.id.resultEntryImage);
        }

        private void bind(Result result) {
            mTitleView.setText(result.title);
            if(result.image != null) {
                new ImageLoadTask(result, mImageView).execute();
            }
        }
    }
}
