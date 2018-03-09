package com.sergeyrodin.onlinelifeviewer;

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

    public ActorsAdapter(List<Link> actors) {
        mActors = actors;
    }

    @Override
    public ActorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.categories_entry, parent, false);
        return new ActorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ActorViewHolder holder, int position) {
        holder.bind(mActors.get(position).Title);
    }

    @Override
    public int getItemCount() {
        return mActors.size();
    }

    public class ActorViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleView;

        public ActorViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView)itemView.findViewById(R.id.entryText);
        }

        private void bind(String title) {
            mTitleView.setText(title);
        }
    }
}
