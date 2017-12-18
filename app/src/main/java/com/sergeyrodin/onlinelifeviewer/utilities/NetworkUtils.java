package com.sergeyrodin.onlinelifeviewer.utilities;

import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by sergey on 30.11.17.
 */

public class NetworkUtils {
    final static String ONLINE_LIFE_BASE_URL = "http://online-life.club";
    private final static String PARAM_DO = "do";
    private final static String PARAM_SUBACTION = "subaction";
    private final static String PARAM_MODE = "mode";
    private final static String PARAM_STORY = "story";
    private final static String PARAM_SEARCH_START = "search_start";
    private final static String search = "search";
    private final static String simple = "simple";

    public static URL buildSearchUrl(String query) {
        return buildSearchUrl(query, 0);
    }

    public static URL buildSearchUrl(String query, int page) {
        String story = "";
        try {
            story = URLEncoder.encode(query.trim(), "windows-1251");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Uri builtUri = Uri.parse(ONLINE_LIFE_BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_DO, search)
                .appendQueryParameter(PARAM_SUBACTION, search)
                .appendQueryParameter(PARAM_MODE, simple)
                .appendQueryParameter(PARAM_STORY, story)
                .build();

        // add paging to url
        if(page > 0) {
            builtUri = builtUri.buildUpon()
                               .appendQueryParameter(PARAM_SEARCH_START, page + "")
                               .build();
        }

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }
}
