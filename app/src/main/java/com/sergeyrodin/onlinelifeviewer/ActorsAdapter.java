package com.sergeyrodin.onlinelifeviewer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sergey on 09.03.18.
 */

public class ActorsAdapter extends RecyclerView.Adapter<ActorsAdapter.ActorViewHolder> {
    private static final String TAG = ActorsAdapter.class.getSimpleName();
    private List<Link> mActors;

    interface ListItemClickListener {
        void onListItemClick(int index);
    }

    final private ListItemClickListener mListItemClickListener;

    public ActorsAdapter(List<Link> actors, ListItemClickListener listener) {
        mActors = actors;
        mListItemClickListener = listener;
    }

    @NonNull
    @Override
    public ActorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.categories_entry, parent, false);
        return new ActorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActorViewHolder holder, int position) {
        holder.bind(mActors.get(position).Title);
    }

    @Override
    public int getItemCount() {
        return mActors.size();
    }

    public class ActorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTitleView;

        public ActorViewHolder(View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.entryText);
            itemView.setOnClickListener(this);
        }

        private void bind(String title) {
            mTitleView.setText(title);
        }

        @Override
        public void onClick(View v) {
            mListItemClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
