package com.sergeyrodin.onlinelifeviewer;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class ResultsActivity extends AppCompatActivity implements ResultsAdapter.ListItemClickListener {
    private final String STATE_TITLE = "com.sergeyrodin.TITLE";
    private final String STATE_LINK = "com.sergeyrodin.LINK";
    private final String LOG_TAG = getClass().getSimpleName();

    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageTextView;
    private RecyclerView mResultsView;
    private SwipeRefreshLayout mSwipeRefresh;
    private String mTitle, mLink;
    private int mSpanCount;
    private ResultsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Add back button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mResultsView = findViewById(R.id.list);

        mSwipeRefresh = findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        Configuration configuration = getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp;
        int RESULT_WIDTH = 190;
        mSpanCount = screenWidthDp / RESULT_WIDTH;

        if(mSpanCount <= 2) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mResultsView.setLayoutManager(layoutManager);
        }else {
            GridLayoutManager layoutManager = new GridLayoutManager(this, mSpanCount);
            mResultsView.setLayoutManager(layoutManager);
        }

        mResultsView.setHasFixedSize(false);

        mLoadingIndicator = findViewById(R.id.results_loading_indicator);
        mErrorMessageTextView = findViewById(R.id.results_loading_error);
        mErrorMessageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewModel != null) {
                    viewModel.retry();
                }
            }
        });

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

        createViewModel();

        setTitle(mTitle);
    }

    private void createViewModel() {
        viewModel = ViewModelProviders.of(this).get(ResultsViewModel.class);
        viewModel.getLiveData(mLink).observe(this, new Observer<PagedList<Result>>() {
            @Override
            public void onChanged(@Nullable PagedList<Result> results) {
                ResultsAdapter adapter = new ResultsAdapter(ResultsActivity.this,
                                                                  ResultsActivity.this,
                                                                          mSpanCount);
                adapter.submitList(results);
                mResultsView.setAdapter(adapter);
            }
        });

        viewModel.getState().observe(this, new Observer<State>() {
            @Override
            public void onChanged(@Nullable State state) {
                if(state != null) {
                    switch(state) {
                        case LOADING_INIT:
                            showLoadingIndicatorInit();
                            break;
                        case LOADING_AFTER:
                            showLoadingIndicatorAfter();
                            break;
                        case DONE:
                            showData();
                            break;
                        case ERROR_INIT:
                            showErrorMessageInit();
                            break;
                        case ERROR_AFTER:
                            showErrorMessageAfter();
                            break;
                    }
                }
            }
        });
    }

    private void refresh() {
        viewModel.refresh(mLink);
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

        if(itemId == R.id.action_refresh) {
            mSwipeRefresh.setRefreshing(true);
            refresh();
            return true;
        }
        if(itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(Result result) {
        ProcessVideoItem.startActorsActivity(this, result.title, result.link);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_TITLE, mTitle);
        outState.putString(STATE_LINK, mLink);
        super.onSaveInstanceState(outState);
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

    private void showErrorMessageInit(){
        if(mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
        mErrorMessageTextView.setText(R.string.network_problem);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mResultsView.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessageAfter() {
        mErrorMessageTextView.setText(R.string.network_problem);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mResultsView.setVisibility(View.VISIBLE);
        moveUpResultsViewBottom();
        moveErrorMessageToBottom();
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    private void showLoadingIndicatorInit() {
        if(mSwipeRefresh.isRefreshing()) {
            mResultsView.setVisibility(View.VISIBLE);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
        }else {
            mResultsView.setVisibility(View.INVISIBLE);
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void showLoadingIndicatorAfter() {
        mResultsView.setVisibility(View.VISIBLE);
        moveUpResultsViewBottom();
        moveLoadingIndicatorToBottom();
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void showData() {
        if(mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mResultsView.setPadding(0, 0, 0, 0);
        mResultsView.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }
}
