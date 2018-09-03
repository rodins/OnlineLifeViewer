package com.sergeyrodin.onlinelifeviewer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

public class SavedItemsActivity extends AppCompatActivity {
    private RecyclerView mRvSaveItems;
    private TextView mTvNoItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_items);

        mRvSaveItems = findViewById(R.id.rv_saved_items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvSaveItems.setLayoutManager(layoutManager);

        mTvNoItems = findViewById(R.id.tv_no_saved_items);
    }
}
