package com.sergeyrodin.onlinelifeviewer;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ResultsActivity extends ListActivity {
    private final String STATE_PREVLINK = "com.sergeyrodin.PREVLINK";
    private final String STATE_NEXTLINK = "com.sergeyrodin.NEXTLINK";
    private final String STATE_CURRENTLINK = "com.sergeyrodin.CURRENTLINK";
    private final String STATE_PAGE = "com.sergeyrodin.PAGE";
    private String title;

    private MenuItem prev, next;
    private URL prevLink, nextLink, currentLink;
    private int page = 0;
    private String tag = "saveResultsData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        title = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        if(title == null) {
            title = getString(R.string.results);
        }

        setTitle(title);

        if(savedInstanceState != null) {
            try {
                prevLink = new URL(savedInstanceState.getString(STATE_PREVLINK));
                nextLink = new URL(savedInstanceState.getString(STATE_NEXTLINK));
                currentLink = new URL(savedInstanceState.getString(STATE_CURRENTLINK));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            page = savedInstanceState.getInt(STATE_PAGE);

            if(page > 0) {
                setTitle(title + ": " + page);
            }
        }

        FragmentManager fm = getFragmentManager();
        RetainedFragment saveResults = (RetainedFragment)fm.findFragmentByTag(tag);
        if(saveResults == null) { //getting new results list
            // Create a progress bar to display while the list loads
            ProgressBar progressBar = new ProgressBar(this);
            progressBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            progressBar.setIndeterminate(true);
            getListView().setEmptyView(progressBar);

            // Must add the progress bar to the root of the layout
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            root.addView(progressBar);

            String link = intent.getStringExtra(MainActivity.EXTRA_LINK);
            if(link != null) {
                // Getting results from site, putting them to ListView and save them
                refresh(link);
            }else if(Intent.ACTION_SEARCH.equals(intent.getAction())) { //Called by SearchView
                String query = getIntent().getStringExtra(SearchManager.QUERY);
                refresh(NetworkUtils.buildSearchUrl(query));
            }
        }else { //using saved results list
            ArrayList<Result> results = saveResults.getData();
            if(results != null) {
                setListAdapter(new ResultsAdapter(this, results));
            }else {
                //if RetainedFragment is outdated refresh data
                if(currentLink != null) {
                    refresh(currentLink);
                }
            }
        }
    }

    private URL getSearchLink(int page) {
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        return NetworkUtils.buildSearchUrl(query, page);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Result result = (Result)l.getAdapter().getItem(position);
        new ItemClickAsyncTask(this).execute(result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.results_menu, menu);

        prev = menu.findItem(R.id.action_prev);
        next = menu.findItem(R.id.action_next);
        if(prevLink == null) {
            prev.setVisible(false);
        }
        if(nextLink == null) {
            next.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void setupPagerFromAsyncTask(String pl, String nl, int p, int prevPage, int nextPage) {
        page = p;
        prevLink = null;
        nextLink = null;
        try {
            prevLink = new URL(pl);
            nextLink = new URL(nl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(page > 0) {
            setTitle(title + ": " + page);
        }
        if(prev != null && prevLink != null) {
            prev.setVisible(true);
        }else {
            if(prevPage == 0) {
                prev.setVisible(false);
            }else { // search page pager
                prev.setVisible(true);
                prevLink = getSearchLink(prevPage); // forming prev search link
            }
        }
        if(next != null && nextLink != null) {
            next.setVisible(true);
        }else {
            if(nextPage == 0) {
                next.setVisible(false);
            }else {
                next.setVisible(true);
                nextLink = getSearchLink(nextPage); //forming next search link
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_prev:
                refresh(prevLink);
                return true;
            case R.id.action_next:
                refresh(nextLink);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(prevLink != null) {
            outState.putString(STATE_PREVLINK, prevLink.toString());
        }
        if(nextLink != null) {
            outState.putString(STATE_NEXTLINK, nextLink.toString());
        }
        if(currentLink != null) {
            outState.putString(STATE_CURRENTLINK, currentLink.toString());
        }
        outState.putInt(STATE_PAGE, page);
        super.onSaveInstanceState(outState);
    }

    private void refresh(String link) {
        setListAdapter(null);
        try {
            currentLink = new URL(link);
            URL url = new URL(link);
            new ResultsAsyncTask(this, tag).execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void refresh(URL url) {
        setListAdapter(null);
        currentLink = url;
        new ResultsAsyncTask(this, tag).execute(url);
    }
}
