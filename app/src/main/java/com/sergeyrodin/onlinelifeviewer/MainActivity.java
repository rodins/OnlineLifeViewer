package com.sergeyrodin.onlinelifeviewer;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.FrameLayout.LayoutParams;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    public static final String EXTRA_PSITEM = "com.sergeyrodin.PSITEM";
    public static final String EXTRA_PLAYLIST = "com.sergeyrodin.PLAYLIST";
    public static final String EXTRA_LINK = "com.sergeyrodin.LINK";
    public static final String EXTRA_JS = "com.sergeyrodin.JS";
    public static final String EXTRA_PAGE = "com.sergeyrodin.PAGE";
    public static final String EXTRA_TITLE = "com.sergeyrodin.TITLE";
    public static final String STATE_PAGE = "com.sergeyrodin.STATE_PAGE";
    public static final String DOMAIN = "http://online-life.club";

    private String mTag = "saveMainData";
    private String page; // needed for categories
    private ProgressBar progressBar;
    private RetainedFragment mSaveResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.main);

        if(savedInstanceState != null) {
            page = savedInstanceState.getString(STATE_PAGE);
        }

        FragmentManager fm = getFragmentManager();
        mSaveResults = (RetainedFragment)fm.findFragmentByTag(mTag);
        if(mSaveResults == null){ //getting new results list
            mSaveResults = new RetainedFragment();
            fm.beginTransaction().add(mSaveResults, mTag).commit();
            // Create a progress bar to display while the list loads
            progressBar = new ProgressBar(this);
            progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                                                         LayoutParams.WRAP_CONTENT,
                                                         Gravity.CENTER));
            progressBar.setIndeterminate(true);
            getListView().setEmptyView(progressBar);

            // Must add the progress bar to the root of the layout
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            root.addView(progressBar);

            try {
                URL url = new URL(DOMAIN);
                new ResultsAsyncTask().execute(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }else{//using saved results list
            List<Result> results = mSaveResults.getData();
            if(results != null) {
                setListAdapter(new ResultsAdapter(this, results));
            }else {
                refresh();
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Result result = (Result)l.getAdapter().getItem(position);
        new ItemClickAsyncTask(this).execute(result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//Mainly search stuff here
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_lastnews:
                lastnews();
                return true;
            case R.id.action_kinonew:
                kinonew();
                return true;
            case R.id.action_kinoserial:
                kinoserial();
                return true;
            case R.id.action_kinomult:
                kinomult();
                return true;
            case R.id.action_kinomultserial:
                kinomultserial();
                return true;
            case R.id.action_kinotv:
                kinotv();
                return true;
            case R.id.action_categories:
                categories();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startResultsActivity(String title, String link) {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_LINK, link);
        startActivity(intent);
    }

    public void setPage(String page) {
        this.page = page;
    }

    private void lastnews() {
        startResultsActivity(getString(R.string.action_lastnews), DOMAIN + "/lastnews/");
    }

    private void kinonew() {
        startResultsActivity(getString(R.string.action_kinonew), DOMAIN + "/kino-new/");
    }

    private void kinoserial() {
        startResultsActivity(getString(R.string.action_kinoserial), DOMAIN + "/kino-serial/");
    }

    private void kinomult() {
        startResultsActivity(getString(R.string.action_kinomult), DOMAIN + "/kino-mult/");
    }

    private void kinomultserial() {
        startResultsActivity(getString(R.string.action_kinomultserial), DOMAIN + "/kino-multserial/");
    }

    private void kinotv() {
        startResultsActivity(getString(R.string.action_kinotv), DOMAIN + "/kino-tv/");
    }

    private void categories() {
        //Start categories activity
        if(page != null) {
            Intent intent = new Intent(this, CategoriesActivity.class);
            intent.putExtra(EXTRA_PAGE, page);
            startActivity(intent);
        }else {
            Toast.makeText(this, R.string.nothing_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void refresh() {
        setListAdapter(null);
        ProgressBar progressBar = (ProgressBar)getListView().getEmptyView();
        if(progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        try {
            URL url = new URL(DOMAIN);
            new ResultsAsyncTask().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_PAGE, page);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ResultsAdapter adapter = (ResultsAdapter)getListAdapter();
        if(mSaveResults != null & adapter != null) {
            mSaveResults.setData(adapter.getResults());
        }
    }

    class ResultsAsyncTask extends AsyncTask<URL, Void, String> {

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
                setPage(page);

            if(page == null) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, R.string.network_problem, Toast.LENGTH_SHORT).show();
                return;
            }

            ResultsParser parser = new ResultsParser(page);
            ArrayList<Result> results =  parser.getItems();

            if(results.isEmpty()) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, R.string.nothing_found, Toast.LENGTH_SHORT).show();
                return;
            }

            //Updating current ListView
            setListAdapter(new ResultsAdapter(MainActivity.this, results));
        }
    }
}

