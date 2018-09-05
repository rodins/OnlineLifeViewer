package com.sergeyrodin.onlinelifeviewer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.database.SavedItem;

import java.util.List;

public class SavedItemsAdapter extends RecyclerView.Adapter<SavedItemsAdapter.SavedItemsViewHolder> {
    private List<SavedItem> mSavedItems;
    private SavedItemClickListener mSavedItemClickListener;

    public SavedItemsAdapter(SavedItemClickListener listener) {
        mSavedItemClickListener = listener;
    }

    @NonNull
    @Override
    public SavedItemsAdapter.SavedItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                     int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.saved_item_entry, parent, false);
        return new SavedItemsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedItemsAdapter.SavedItemsViewHolder holder, int position) {
         holder.bind(mSavedItems.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        if(mSavedItems == null) {
            return 0;
        }
        return mSavedItems.size();
    }

    public void setSavedItems(List<SavedItem> savedItems) {
        mSavedItems = savedItems;
        notifyDataSetChanged();
    }

    public List<SavedItem> getSavedItems() {
        return mSavedItems;
    }

    public class SavedItemsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;

        public SavedItemsViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.entryText);
            itemView.setOnClickListener(this);
        }

        private void bind(String title) {
            textView.setText(title);
        }

        @Override
        public void onClick(View v) {
            mSavedItemClickListener.onSavedItemClick(getAdapterPosition());
        }
    }

    interface SavedItemClickListener {
        void onSavedItemClick(int position);
    }
}
