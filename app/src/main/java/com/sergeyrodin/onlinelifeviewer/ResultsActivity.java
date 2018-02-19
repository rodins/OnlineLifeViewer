package com.sergeyrodin.onlinelifeviewer;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends ListActivity {
    private final String STATE_PREVLINK = "com.sergeyrodin.PREVLINK";
    private final String STATE_NEXTLINK = "com.sergeyrodin.NEXTLINK";
    private final String STATE_CURRENTLINK = "com.sergeyrodin.CURRENTLINK";
    private final String STATE_PAGE = "com.sergeyrodin.PAGE";
    private String title;

    private MenuItem prev, next;
    private URL prevLink, nextLink, currentLink;
    private int page = 0;
    private RetainedFragment mSaveResults;
    private ProgressBar progressBar;
    private TextView errorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Loading indicator and error text view
        // Create a progress bar to display while the list loads
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        errorMessageTextView = new TextView(this);
        errorMessageTextView.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER));

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(errorMessageTextView);
        root.addView(progressBar);

        Intent intent = getIntent();
        title = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        if(title == null) {
            title = getString(R.string.results);
        }

        if(savedInstanceState != null) {
            try {
                String strPrevLink = savedInstanceState.getString(STATE_PREVLINK);
                String strNextLink = savedInstanceState.getString(STATE_NEXTLINK);
                String strCurrentLink = savedInstanceState.getString(STATE_CURRENTLINK);
                if(strPrevLink != null) {
                    prevLink = new URL(strPrevLink);
                }
                if(strNextLink != null) {
                    nextLink = new URL(strNextLink);
                }
                if(strCurrentLink != null) {
                    currentLink = new URL(strCurrentLink);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            page = savedInstanceState.getInt(STATE_PAGE);

            if(page > 0) {
                setTitle(title + ": " + page);
            }
        }else {
            setTitle(title);
        }

        FragmentManager fm = getFragmentManager();
        String tag = "saveResultsData";
        mSaveResults = (RetainedFragment)fm.findFragmentByTag(tag);
        if(mSaveResults == null) { //getting new results list
            mSaveResults = new RetainedFragment();
            fm.beginTransaction().add(mSaveResults, tag).commit();

            String link = intent.getStringExtra(MainActivity.EXTRA_LINK);
            if(link != null) {
                // Getting results from site, putting them to ListView and save them
                refresh(link);
            }else if(Intent.ACTION_SEARCH.equals(intent.getAction())) { //Called by SearchView
                String query = getIntent().getStringExtra(SearchManager.QUERY);
                refresh(NetworkUtils.buildSearchUrl(query));
            }
        }else { //using saved results list
            List<Result> results = mSaveResults.getData();
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
            if(pl != null) {
                prevLink = new URL(pl);
            }
            if(nl != null) {
                nextLink = new URL(nl);
            }
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
            String strPrevLink = prevLink.toString();
            outState.putString(STATE_PREVLINK, strPrevLink);
        }
        if(nextLink != null) {
            String strNextLink = nextLink.toString();
            outState.putString(STATE_NEXTLINK, strNextLink);
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
            new ResultsAsyncTask().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void refresh(URL url) {
        setListAdapter(null);
        currentLink = url;
        new ResultsAsyncTask().execute(url);
    }

    private void showErrorMessage(int id){
        errorMessageTextView.setText(id);
        progressBar.setVisibility(View.INVISIBLE);
        errorMessageTextView.setVisibility(View.VISIBLE);
    }

    private void showLoadingIndicator() {
        //progressBar.setVisibility(View.VISIBLE);
        errorMessageTextView.setVisibility(View.INVISIBLE);
    }

    public class ResultsAsyncTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            showLoadingIndicator();
        }

        protected String doInBackground(URL... params) {
            try {
                //TODO: get and parse only useful html page part
                return new Curl().getPageString(params[0]);
            }catch(IOException e){
                System.err.println(e.toString());
                return null;
            }
        }

        protected void onPostExecute(String page) {
            if(page == null) {
                showErrorMessage(R.string.network_problem);
                mSaveResults.setData(null);//save null to RetainedFragment to erase prev results
                return;
            }

            ResultsParser parser = new ResultsParser(page);
            ArrayList<Result> results =  parser.getItems();

            if(results.isEmpty()) {
                showErrorMessage(R.string.nothing_found);
                mSaveResults.setData(null);
                return;
            }

            //Updating current ListView
            //Saving data
            if(mSaveResults != null) {
                mSaveResults.setData(results);
            }
            setListAdapter(new ResultsAdapter(ResultsActivity.this, results));

            parser.navigationInfo();
            setupPagerFromAsyncTask(parser.getPrevLink(),
                                    parser.getNextLink(),
                                    parser.getPageNumber(),
                                    parser.getPrevPage(),
                                    parser.getNextPage());

        }
    }
}
