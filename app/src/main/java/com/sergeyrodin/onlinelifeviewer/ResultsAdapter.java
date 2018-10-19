package com.sergeyrodin.onlinelifeviewer;

import android.arch.paging.PagedListAdapter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.util.List;

/**
 * Created by root on 10.05.16.
 */
class ResultsAdapter extends PagedListAdapter<Result, ResultsAdapter.ResultViewHolder> {
    private final String TAG = ResultsAdapter.class.getSimpleName();

    private Drawable mDefaultDrawable;
    private final int WIDTH;
    private final int HEIGHT;
    private int mNewWidthTextViewPx;
    private ResultsActivity mActivity;
    private int mSpanCount;

    interface ListItemClickListener {
        void onListItemClick(Result result);
    }

    final private ListItemClickListener mOnClickListener;

    ResultsAdapter(ListItemClickListener onClickListener,
                   ResultsActivity activity,
                   int spanCount) {
        super(DIFF_CALLBACK);
        mOnClickListener = onClickListener;
        mActivity = activity;
        Resources resources = mActivity.getResources();
        int screenWidthDp = resources.getConfiguration().screenWidthDp;
        float density = resources.getDisplayMetrics().density;
        int newWidthDp = screenWidthDp / spanCount;
        int newWidthPx = (int) (newWidthDp * density);

        mSpanCount = spanCount;

        int LIN_LAY_WIDTH_MIN_TW_WIDTH = 10;
        int TW_WIDTH_MIN_IMG_WIDTH = 16;
        double HEIGHT_DEV_WIDTH = 1.4390243902;
        if (spanCount > 2) {
            mNewWidthTextViewPx = newWidthPx + LIN_LAY_WIDTH_MIN_TW_WIDTH;
            WIDTH = mNewWidthTextViewPx - TW_WIDTH_MIN_IMG_WIDTH;
            HEIGHT = (int) (WIDTH * HEIGHT_DEV_WIDTH);
        } else {
            mNewWidthTextViewPx = newWidthPx / 2 + LIN_LAY_WIDTH_MIN_TW_WIDTH;
            WIDTH = mNewWidthTextViewPx - TW_WIDTH_MIN_IMG_WIDTH;
            HEIGHT = (int) (WIDTH * HEIGHT_DEV_WIDTH);
        }

        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.empty);
        bitmap = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, true);
        mDefaultDrawable = new BitmapDrawable(resources, bitmap);
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mSpanCount <= 2) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.linear_result_entry, parent, false);
            return new ResultViewHolder(view);
        } else {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.result_entry, parent, false);
            return new ResultViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        Result result = getItem(position);
        if(result != null) {
            holder.bind(result);
        }
    }

    class ResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTitleView;
        ImageView mImageView;

        ResultViewHolder(View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.resultEntryTitle);
            mImageView = itemView.findViewById(R.id.resultEntryImage);
            itemView.setOnClickListener(this);
            if (mSpanCount > 2) {
                mTitleView.setMaxWidth(mNewWidthTextViewPx);
            }
        }

        private void bind(Result result) {
            mTitleView.setText(result.title);
            int DEFAULT_WIDTH = 164;
            int DEFAULT_HEIGHT = 236;
            GlideApp.with(mActivity)
                    .load(NetworkUtils.buildImageStringUrl(result.image, DEFAULT_WIDTH, DEFAULT_HEIGHT))
                    .override(WIDTH, HEIGHT)
                    .placeholder(mDefaultDrawable)
                    .into(mImageView);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mOnClickListener.onListItemClick(getItem(position));
        }
    }

    private static final DiffUtil.ItemCallback<Result> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Result>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Result oldResult, @NonNull Result newResult) {
                    return oldResult.title.equals(newResult.title);
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Result oldResult, @NonNull Result newResult) {
                    // Incorrectly returning false here will result in too many animations.
                    return oldResult.link.equals(newResult.link);
                }
            };
}
