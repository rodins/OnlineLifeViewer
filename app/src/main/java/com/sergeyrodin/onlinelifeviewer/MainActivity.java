package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.utilities.CategoriesParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class MainActivity extends Activity {
    public static final String EXTRA_PSITEM = "com.sergeyrodin.PSITEM";
    public static final String EXTRA_PLAYLIST = "com.sergeyrodin.PLAYLIST";
    public static final String EXTRA_LINK = "com.sergeyrodin.LINK";
    public static final String EXTRA_JS = "com.sergeyrodin.JS";
    public static final String EXTRA_PAGE = "com.sergeyrodin.PAGE";
    public static final String EXTRA_TITLE = "com.sergeyrodin.TITLE";
    //public static final String STATE_PAGE = "com.sergeyrodin.STATE_PAGE";
    public static final String DOMAIN = "http://online-life.club";

    private String mTag = "saveMainData";
    //private String page; // needed for categories
    private ProgressBar progressBar;
    private LinkRetainedFragment linkRetainedFragment;
    private TextView tvCategories;
    private TextView tvLoadingError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /*if(savedInstanceState != null) {
            page = savedInstanceState.getString(STATE_PAGE);
        }*/

        progressBar = (ProgressBar)findViewById(R.id.loading_indicator);
        tvLoadingError = (TextView)findViewById(R.id.loading_error);

        tvCategories = (TextView)findViewById(R.id.tv_categories);



        FragmentManager fm = getFragmentManager();
        linkRetainedFragment = (LinkRetainedFragment)fm.findFragmentByTag(mTag);
        if(linkRetainedFragment == null){ //getting new results list
            linkRetainedFragment = new LinkRetainedFragment();
            fm.beginTransaction().add(linkRetainedFragment, mTag).commit();

            new CategoriesAsyncTask().execute();

            // Create a progress bar to display while the list loads
            /*progressBar = new ProgressBar(this);
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
            }*/
        }else{//using saved categories list
            List<Link> categories = linkRetainedFragment.getData();
            if(categories != null) {
                listToTextView(categories);
            }else {
                new CategoriesAsyncTask().execute();
            }
        }
    }

    private void listToTextView(List<Link> categories) {
        for (Link category : categories) {
            tvCategories.append(category.Title + "\n");
            for (Link subcategory : category.Links) {
                tvCategories.append("\t" + subcategory.Title + "\n");
            }
        }
    }

    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Result result = (Result)l.getAdapter().getItem(position);
        new ItemClickAsyncTask(this).execute(result);
    }*/

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

    /*@Override
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
    }*/

    private void startResultsActivity(String title, String link) {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_LINK, link);
        startActivity(intent);
    }

    /*public void setPage(String page) {
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
    }*/

    /*private void categories() {
        //Start categories activity
        if(page != null) {
            Intent intent = new Intent(this, CategoriesActivity.class);
            intent.putExtra(EXTRA_PAGE, page);
            startActivity(intent);
        }else {
            Toast.makeText(this, R.string.nothing_found, Toast.LENGTH_SHORT).show();
        }
    }*/

    /*private void refresh() {
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
    }*/

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_PAGE, page);
        super.onSaveInstanceState(outState);
    }*/

    private void showLoadingIndicator() {
        tvCategories.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showResults() {
        tvCategories.setVisibility(View.VISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showLoadingError() {
        tvCategories.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    class CategoriesAsyncTask extends AsyncTask<Void, Void, List<Link>> {

        @Override
        protected void onPreExecute() {
            showLoadingIndicator();
        }

        @Override
        protected List<Link> doInBackground(Void... voids) {
            //TODO use domain from resources
            URL url = null;
            try {
                url = new URL(DOMAIN);
                HttpURLConnection connection = null;
                BufferedReader in = null;
                try {
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:43.0) Gecko/20100101 Firefox/43.0 SeaMonkey/2.40");
                    InputStream stream = connection.getInputStream();
                    in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
                    String html = CategoriesParser.getCategoriesPart(in);
                    return CategoriesParser.parseCategories(html);
                }finally {
                    if(in != null) {
                        in.close();
                    }
                    if(connection != null) {
                        connection.disconnect();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Link> categories) {
            if(categories != null) {
                showResults();
                linkRetainedFragment.setData(categories);
                listToTextView(categories);
            }else {
                showLoadingError();
            }
        }
    }

    /*class ResultsAsyncTask extends AsyncTask<URL, Void, String> {

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
    }*/
}

