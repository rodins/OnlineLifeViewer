package com.sergeyrodin.onlinelifeviewer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by root on 10.05.16.
 */
class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>{
    private final String TAG = ResultsAdapter.class.getSimpleName();
    private final int LIN_LAY_WIDTH_MIN_TW_WIDTH = 10;
    private final int TW_WIDTH_MIN_IMG_WIDTH = 16;
    private final double HEIGHT_DEV_WIDTH = 1.4390243902;
    private List<Result> results;
    private Bitmap mDefaultBitmap;
    private final int WIDTH;
    private final int HEIGHT;
    private int mNewWidthTextViewPx;
    private ResultsActivity mActivity;
    private int mSpanCount;

    interface ListItemClickListener {
        void onListItemClick(int index);
    }

    final private ListItemClickListener mOnClickListener;

    ResultsAdapter(List<Result> results,
                   ListItemClickListener onClickListener,
                   ResultsActivity activity,
                   int spanCount) {
        this.results = results;
        mOnClickListener = onClickListener;
        mActivity = activity;
        Resources resources = mActivity.getResources();
        int screenWidthDp = resources.getConfiguration().screenWidthDp;
        float density = resources.getDisplayMetrics().density;
        int newWidthDp = screenWidthDp/spanCount;
        int newWidthPx = (int)(newWidthDp*density);

        mSpanCount = spanCount;

        if(spanCount > 1) {
            mNewWidthTextViewPx = newWidthPx + LIN_LAY_WIDTH_MIN_TW_WIDTH;
            WIDTH = mNewWidthTextViewPx - TW_WIDTH_MIN_IMG_WIDTH;
            HEIGHT = (int)(WIDTH * HEIGHT_DEV_WIDTH);
        }else {
            mNewWidthTextViewPx = newWidthPx/2 + LIN_LAY_WIDTH_MIN_TW_WIDTH;
            WIDTH = mNewWidthTextViewPx - TW_WIDTH_MIN_IMG_WIDTH;
            HEIGHT = (int)(WIDTH * HEIGHT_DEV_WIDTH);
        }

        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.empty);
        mDefaultBitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true);
    }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mSpanCount == 1) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.linear_result_entry, parent, false);
            return new ResultViewHolder(view);
        }else {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.result_entry, parent, false);
            return new ResultViewHolder(view);
        }
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
            mTitleView.setMaxWidth(mNewWidthTextViewPx);
        }

        private void bind(Result result) {
            mTitleView.setText(result.title);
            Bitmap bitmap = mActivity.getBitmapFromMemCache(result.image);
            if(bitmap != null) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true);
                mImageView.setImageBitmap(scaledBitmap);
            }else if(result.image != null){
                mImageView.setImageBitmap(mDefaultBitmap);
                new ImageLoadTask(mActivity, mImageView, result.image, WIDTH, HEIGHT).execute();
            }
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
