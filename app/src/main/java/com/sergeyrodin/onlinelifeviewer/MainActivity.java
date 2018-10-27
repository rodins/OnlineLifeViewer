package com.sergeyrodin.onlinelifeviewer;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_PSITEM = "com.sergeyrodin.PSITEM";
    public static final String EXTRA_LINK = "com.sergeyrodin.LINK";
    public static final String EXTRA_JS = "com.sergeyrodin.JS";
    public static final String EXTRA_TITLE = "com.sergeyrodin.TITLE";
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private TextView tvLoadingError;
    private ExpandableListView mCategoriesList;
    private MenuItem refreshMenuItem;
    private List<Link> mCategories;
    private CategoriesViewModel mCategoriesViewModel;

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

        mCategoriesViewModel = ViewModelProviders.of(this).get(CategoriesViewModel.class);
        mCategoriesViewModel.getCategoriesData().observe(this, new Observer<CategoriesData>() {
            @Override
            public void onChanged(@Nullable CategoriesData categoriesData) {
                if(categoriesData != null) {
                    if(categoriesData.isLoading()) {
                        showLoadingIndicator();
                    }else if(categoriesData.isError()) {
                        showLoadingError();
                    }else if(categoriesData.getCategories() != null) {
                        showResults();
                        mCategories = categoriesData.getCategories();
                        categoriesToAdapter(mCategories);
                    }
                }
            }
        });
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
        int itemId = item.getItemId();
        if(itemId == R.id.action_refresh) {
            mCategoriesViewModel.refresh();
            return true;
        }

        if(itemId == R.id.action_saved_items) {
            Intent intent = new Intent(this, SavedItemsActivity.class);
            startActivity(intent);
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
}

