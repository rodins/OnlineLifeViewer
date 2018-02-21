package com.sergeyrodin.onlinelifeviewer;

import android.app.ExpandableListActivity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.utilities.CategoriesParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends ExpandableListActivity {
    public static final String EXTRA_PSITEM = "com.sergeyrodin.PSITEM";
    public static final String EXTRA_PLAYLIST = "com.sergeyrodin.PLAYLIST";
    public static final String EXTRA_LINK = "com.sergeyrodin.LINK";
    public static final String EXTRA_JS = "com.sergeyrodin.JS";
    public static final String EXTRA_PAGE = "com.sergeyrodin.PAGE";
    public static final String EXTRA_TITLE = "com.sergeyrodin.TITLE";
    public static final String DOMAIN = "http://online-life.club";

    private ProgressBar progressBar;
    private LinkRetainedFragment linkRetainedFragment;
    private TextView tvLoadingError;
    private MenuItem refreshMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        progressBar = (ProgressBar)findViewById(R.id.loading_indicator);
        tvLoadingError = (TextView)findViewById(R.id.loading_error);

        FragmentManager fm = getFragmentManager();
        String tag = "saveMainData";
        linkRetainedFragment = (LinkRetainedFragment)fm.findFragmentByTag(tag);
        if(linkRetainedFragment == null){ //getting new results list
            linkRetainedFragment = new LinkRetainedFragment();
            fm.beginTransaction().add(linkRetainedFragment, tag).commit();

            new CategoriesAsyncTask().execute();
        }else{//using saved categories list
            List<Link> categories = linkRetainedFragment.getData();
            if(categories != null) {
                categoriesToAdapter(categories);
            }else {
                new CategoriesAsyncTask().execute();
            }
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        List<Link> categories = linkRetainedFragment.getData();
        Link selectedCategory = categories.get(groupPosition).Links.get(childPosition);
        startResultsActivity(selectedCategory.Title, selectedCategory.Href);
        return true;
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

        refreshMenuItem = menu.findItem(R.id.action_refresh);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh) {
            new CategoriesAsyncTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startResultsActivity(String title, String link) {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_LINK, link);
        startActivity(intent);
    }

    private void showLoadingIndicator() {
        getExpandableListView().setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        if(refreshMenuItem != null) {
            refreshMenuItem.setVisible(false);
        }
    }

    private void showResults() {
        getExpandableListView().setVisibility(View.VISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        refreshMenuItem.setVisible(false);
    }

    private void showLoadingError() {
        getExpandableListView().setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        refreshMenuItem.setVisible(true);
    }

    private void categoriesToAdapter(List<Link> categories) {
        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();
        for(Link category : categories) {
            Map<String, String> map = new HashMap<>();
            map.put("entryText", category.Title);
            groupData.add(map);
            List<Map<String, String>> groupList = new ArrayList<>();
            for(Link subcategory : category.Links) {
                Map<String, String> childMap = new HashMap<>();
                childMap.put("entryTextSubcategories", subcategory.Title);
                groupList.add(childMap);
            }
            childData.add(groupList);
        }

        String[] groupFrom = {"entryText"};
        int[] groupTo = {R.id.entryText};
        String[] childFrom = {"entryTextSubcategories"};
        int[] childTo = {R.id.entry_text_subcategories};

        ExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                R.layout.categories_entry,
                groupFrom,
                groupTo,
                childData,
                R.layout.subcategories_entry,
                childFrom,
                childTo
                );
        setListAdapter(adapter);
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
                //listToTextView(categories);
                categoriesToAdapter(categories);
            }else {
                showLoadingError();
            }
        }
    }
}

