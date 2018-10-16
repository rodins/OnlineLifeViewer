package com.sergeyrodin.onlinelifeviewer;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sergeyrodin.onlinelifeviewer.utilities.Html;
import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsActivity extends AppCompatActivity implements ResultsAdapter.ListItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener{
    private final String STATE_TITLE = "com.sergeyrodin.TITLE";
    private final String STATE_LINK = "com.sergeyrodin.LINK";

    private ResultsViewModel mViewModel;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageTextView;
    private RecyclerView mResultsView;
    private List<Result> mResults;
    private boolean mIsPage = false;
    private boolean isLoadStarted = false;
    private String mTitle, mLink;
    private boolean mIsShowActorsOnClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Add back button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mResultsView = findViewById(R.id.rv_results);

        Configuration configuration = getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp;
        int RESULT_WIDTH = 190;
        int mSpanCount = screenWidthDp / RESULT_WIDTH;

        if(mSpanCount <= 2) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mResultsView.setLayoutManager(layoutManager);
        }else {
            GridLayoutManager layoutManager = new GridLayoutManager(this, mSpanCount);
            mResultsView.setLayoutManager(layoutManager);
        }

        mResultsView.setHasFixedSize(false);

        mResultsView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(!recyclerView.canScrollVertically(1)) {
                    if(!isLoadStarted) {
                        isLoadStarted = true;
                        mIsPage = true;
                        showLoadingIndicator();
                        mViewModel.loadNextPage();
                    }
                }
            }
        });

        mLoadingIndicator = findViewById(R.id.results_loading_indicator);
        mErrorMessageTextView = findViewById(R.id.results_loading_error);

        if(savedInstanceState != null) {
            mTitle = savedInstanceState.getString(STATE_TITLE);
            mLink = savedInstanceState.getString(STATE_LINK);
        }else {
            mTitle = getString(R.string.results);
        }

        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(MainActivity.EXTRA_TITLE)) {
                mTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE);
            }

            if(intent.hasExtra(MainActivity.EXTRA_LINK)) {
                mLink = intent.getStringExtra(MainActivity.EXTRA_LINK);
            }else if(Intent.ACTION_SEARCH.equals(intent.getAction())) { //Called by SearchView
                String query = getIntent().getStringExtra(SearchManager.QUERY);
                mTitle = query;
                mLink = NetworkUtils.buildSearchUrl(query);
            }
        }

        createViewModel(mLink);

        mResultsView.setAdapter(new ResultsAdapter(mResults,
                               this,
                               this,
                                mSpanCount));

        setTitle(mTitle);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadClickModeFromPreferences(sharedPreferences);

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void createViewModel(String link) {
        showLoadingIndicator();
        mViewModel = ViewModelProviders.of(this).get(ResultsViewModel.class);
        mResults = mViewModel.getResults();
        mViewModel.getResultsData(link).observe(this, new Observer<ResultsData>() {
            @Override
            public void onChanged(@Nullable ResultsData resultsData) {
                isLoadStarted = false;
                if(resultsData != null && !resultsData.isError()) {
                    if(!resultsData.getResults().isEmpty()) {
                        mResults.addAll(resultsData.getResults());
                        ResultsAdapter adapter = (ResultsAdapter) mResultsView.getAdapter();
                        adapter.notifyDataSetChanged();
                        showData();
                    }else {
                        showErrorMessage(R.string.nothing_found);
                    }
                }else {
                    showErrorMessage(R.string.network_problem);
                }
            }
        });
    }

    private void loadClickModeFromPreferences(SharedPreferences sharedPreferences) {
        mIsShowActorsOnClick = sharedPreferences.getBoolean(getString(R.string.pref_actors_key),
                getResources().getBoolean(R.bool.pref_actors_default_value));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.results_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if(itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int position) {
        Result result = mResults.get(position);
        if(mIsShowActorsOnClick || mTitle.contains(getString(R.string.trailers))) { // Use actors links
            ProcessVideoItem.startActorsActivity(this, result.title, result.link);
        }else { // Use constant links
            // Find links in LinksActivity
            Intent intent = new Intent(ResultsActivity.this, LinksActivity.class);
            intent.putExtra(MainActivity.EXTRA_TITLE, result.title);
            intent.putExtra(MainActivity.EXTRA_LINK, result.link);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_TITLE, mTitle);
        outState.putString(STATE_LINK, mLink);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister ResultsActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void moveUpResultsViewBottom() {
        // Increase bottom padding to show spinner
        //TODO: use int padding = getResources().getDimensionPixelOffset(R.dimen.padding);
        int paddingDp = 50;
        float density = this.getResources().getDisplayMetrics().density;
        int paddingPixel = (int)(paddingDp * density);
        mResultsView.setPadding(0, 0, 0, paddingPixel);
    }

    private void moveLoadingIndicatorToBottom() {
        // Place spinner to the bottom
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
        mLoadingIndicator.setLayoutParams(params);
    }

    private void moveErrorMessageToBottom() {
        // Place textView to the bottom
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
        mErrorMessageTextView.setLayoutParams(params);
    }

    private void showErrorMessage(int id){
        mErrorMessageTextView.setText(id);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if(mIsPage) {
            mResultsView.setVisibility(View.VISIBLE);
            moveUpResultsViewBottom();
            moveErrorMessageToBottom();
        }else {
            mResultsView.setVisibility(View.INVISIBLE);
        }
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    private void showLoadingIndicator() {
        if(mIsPage) {
            mResultsView.setVisibility(View.VISIBLE);
            moveUpResultsViewBottom();
            moveLoadingIndicatorToBottom();
        }else {
            mResultsView.setVisibility(View.INVISIBLE);
        }
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void showData() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mResultsView.setPadding(0, 0, 0, 0);
        mResultsView.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_actors_key))) {
            loadClickModeFromPreferences(sharedPreferences);
        }
    }
}
