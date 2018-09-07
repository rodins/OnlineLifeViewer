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

public class CategoriesRepo {
    private MutableLiveData<CategoriesData> categoriesData;
    private final String CATEGORIES_URL;

    CategoriesRepo(String categoriesUrl){
        CATEGORIES_URL = categoriesUrl;
    }

    LiveData<CategoriesData> getCategoriesData() {
        //TODO: store categories in database
        if(categoriesData == null) {
            categoriesData = new MutableLiveData<>();
            categoriesData.setValue(new CategoriesData(true, null, false));
            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                @Override
                public void run() {
                    getCategoriesFromNet();
                }
            });

        }
        return categoriesData;
    }

    private void getCategoriesFromNet() {
        URL url;
        try {
            url = new URL(CATEGORIES_URL);
            HttpURLConnection connection = null;
            BufferedReader in = null;
            try {
                connection = (HttpURLConnection)url.openConnection();
                InputStream stream = connection.getInputStream();
                in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
                String html = CategoriesParser.getCategoriesPart(in);
                List<Link> categories = CategoriesParser.parseCategories(html);
                categoriesData.postValue(new CategoriesData(false, categories, false));
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
            categoriesData.postValue(new CategoriesData(false, null, true));
        }
    }
}
