package com.sergeyrodin.onlinelifeviewer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ActorsActivity extends AppCompatActivity {
    private final static String TAG = ActorsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actors);

        setTitle(R.string.actors_title);

        Intent intent = getIntent();
        if(intent != null) {
            String link = intent.getStringExtra(MainActivity.EXTRA_LINK);
            Log.d(TAG, "Link: " + link);
        }
    }
}
