package com.sergeyrodin.onlinelifeviewer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Link>> {
    public static final String EXTRA_PSITEM = "com.sergeyrodin.PSITEM";
    public static final String EXTRA_PLAYLIST = "com.sergeyrodin.PLAYLIST";
    public static final String EXTRA_LINK = "com.sergeyrodin.LINK";
    public static final String EXTRA_JS = "com.sergeyrodin.JS";
    public static final String EXTRA_PAGE = "com.sergeyrodin.PAGE";
    public static final String EXTRA_TITLE = "com.sergeyrodin.TITLE";
    public static final String DOMAIN = "http://online-life.club";//TODO: domain should be
    private static final String CATEGORIES_URL_EXTRA = "categories";
    private static final String TAG = MainActivity.class.getSimpleName();

    private int CATEGORIES_LOADER = 22;
    private ProgressBar progressBar;
    private TextView tvLoadingError;
    private ExpandableListView mCategoriesList;
    private MenuItem refreshMenuItem;
    private List<Link> mCategories;
    private boolean mIsCalledTwice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.loading_indicator);
        tvLoadingError = findViewById(R.id.loading_error);
        mCategoriesList = findViewById(R.id.categories_list);

        mCategoriesList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(mCategories != null) {
                    String parentTitle = mCategories.get(groupPosition).Title;
                    Link selectedCategory = mCategories.get(groupPosition).Links.get(childPosition);
                    startResultsActivity(parentTitle + " - " + selectedCategory.Title,
                            selectedCategory.Href);
                }
                return true;
            }
        });

        mIsCalledTwice = false;
        showLoadingIndicator();
        Bundle categoriesBundle = new Bundle();
        categoriesBundle.putString(CATEGORIES_URL_EXTRA, DOMAIN);
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(CATEGORIES_LOADER, categoriesBundle, this);
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
            showLoadingIndicator();
            Bundle categoriesBundle = new Bundle();
            categoriesBundle.putString(CATEGORIES_URL_EXTRA, DOMAIN);
            LoaderManager loaderManager = getSupportLoaderManager();
            loaderManager.restartLoader(CATEGORIES_LOADER, categoriesBundle, this);
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
        mCategoriesList.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        if(refreshMenuItem != null) {
            refreshMenuItem.setVisible(false);
        }
    }

    private void showResults() {
        mCategoriesList.setVisibility(View.VISIBLE);
        tvLoadingError.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        if(refreshMenuItem != null) {
            refreshMenuItem.setVisible(false);
        }
    }

    private void showLoadingError() {
        mCategoriesList.setVisibility(View.INVISIBLE);
        tvLoadingError.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        if(refreshMenuItem != null) {
            refreshMenuItem.setVisible(true);
        }
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
        mCategoriesList.setAdapter(adapter);
    }

    @NonNull
    @Override
    public Loader<List<Link>> onCreateLoader(int id, Bundle args) {
        return new CategoriesAsyncTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Link>> loader, List<Link> data) {
        if(!mIsCalledTwice) {
            mIsCalledTwice = true;
            if(data != null && !data.isEmpty()) {
                showResults();
                mCategories = data;
                categoriesToAdapter(data);
            }else {
                showLoadingError();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Link>> loader) {

    }

    static class CategoriesAsyncTaskLoader extends AsyncTaskLoader<List<Link>> {
        private Bundle args;

        CategoriesAsyncTaskLoader(Context context, Bundle args) {
            super(context);
            this.args = args;
        }

        @Override
        protected void onStartLoading() {
            if(args == null) {
                return;
            }
            forceLoad();
        }

        @Override
        public List<Link> loadInBackground() {
            String categoriesUrl = args.getString(CATEGORIES_URL_EXTRA);
            //TODO use domain from resources
            URL url;
            try {
                url = new URL(categoriesUrl);
                HttpURLConnection connection = null;
                BufferedReader in = null;
                try {
                    connection = (HttpURLConnection)url.openConnection();
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
    }
}

