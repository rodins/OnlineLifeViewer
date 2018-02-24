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
    private List<Result> results;

    interface ListItemClickListener {
        void onListItemClick(int index);
    }

    final private ListItemClickListener mOnClickListener;

    ResultsAdapter(List<Result> results, ListItemClickListener onClickListener) {
        this.results = results;
        mOnClickListener = onClickListener;
    }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.result_entry, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder holder, int position) {
        Result result = results.get(position);
        holder.bind(result);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    class ResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTitleView;
        ImageView mImageView;
        ResultViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView)itemView.findViewById(R.id.resultEntryTitle);
            mImageView = (ImageView)itemView.findViewById(R.id.resultEntryImage);
            itemView.setOnClickListener(this);
        }

        private void bind(Result result) {
            mTitleView.setText(result.title);
            if(result.getBitmap() != null) {
                mImageView.setImageBitmap(result.getBitmap());
            }else if(result.image != null){
                mImageView.setImageResource(R.drawable.empty);
                new ImageLoadTask(result, mImageView).execute();
            }
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
