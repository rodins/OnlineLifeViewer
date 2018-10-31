package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.sergeyrodin.onlinelifeviewer.utilities.CategoriesParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class CategoriesDataSource {
    private MutableLiveData<List<Link>> categoriesData;
    private MutableLiveData<State> state;
    private final String CATEGORIES_URL;

    CategoriesDataSource(String categoriesUrl){
        CATEGORIES_URL = categoriesUrl;
        state = new MutableLiveData<>();
    }

    LiveData<List<Link>> getCategoriesData() {
        //TODO: store categories in database
        if(categoriesData == null) {
            categoriesData = new MutableLiveData<>();
            getCategoriesFromNet();
        }
        return categoriesData;
    }

    LiveData<State> getState() {
        return state;
    }

    void retry() {
        getCategoriesFromNet();
    }

    private void getCategoriesFromNet() {
        // Show loading indicator
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                URL url;
                try {
                    state.postValue(State.LOADING_INIT);
                    url = new URL(CATEGORIES_URL);
                    HttpURLConnection connection = null;
                    BufferedReader in = null;
                    try {
                        connection = (HttpURLConnection)url.openConnection();
                        InputStream stream = connection.getInputStream();
                        in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
                        String html = CategoriesParser.getCategoriesPart(in);
                        List<Link> categories = CategoriesParser.parseCategories(html);
                        categoriesData.postValue(categories);
                        state.postValue(State.DONE);
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
                    state.postValue(State.ERROR_INIT);
                }
            }
        });
    }
}
