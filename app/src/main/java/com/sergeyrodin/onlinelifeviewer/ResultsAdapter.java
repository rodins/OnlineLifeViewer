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
    private Drawable mDefaultDrawable;
    private final int WIDTH;
    private final int HEIGHT;
    private int mNewWidthTextViewPx;

    interface ListItemClickListener {
        void onListItemClick(int index);
    }

    final private ListItemClickListener mOnClickListener;

    ResultsAdapter(List<Result> results, ListItemClickListener onClickListener, int newWidthDp) {
        this.results = results;
        mOnClickListener = onClickListener;
        Resources resources = ((Context)mOnClickListener).getResources();

        float density = resources.getDisplayMetrics().density;
        int newWidthPx = (int)(newWidthDp*density);
        mNewWidthTextViewPx = newWidthPx + LIN_LAY_WIDTH_MIN_TW_WIDTH;
        WIDTH = mNewWidthTextViewPx - TW_WIDTH_MIN_IMG_WIDTH;
        HEIGHT = (int)(WIDTH * HEIGHT_DEV_WIDTH);

        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.empty);
        mDefaultBitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true);
        Log.d(TAG, "Width: " + WIDTH);
        Log.d(TAG, "Height: " + HEIGHT);
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
            mTitleView.setMaxWidth(mNewWidthTextViewPx);
        }

        private void bind(Result result) {
            mTitleView.setText(result.title);
            if(result.getBitmap() != null) {
                mImageView.setImageBitmap(result.getBitmap());
            }else if(result.image != null){
                mImageView.setImageBitmap(mDefaultBitmap);
                new ImageLoadTask(result, mImageView, WIDTH, HEIGHT).execute();
            }
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
