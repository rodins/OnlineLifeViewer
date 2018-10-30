package com.sergeyrodin.onlinelifeviewer.utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

/**
 * Created by sergey on 30.11.17.
 */

public class NetworkUtils {
    final static String ONLINE_LIFE_BASE_URL = "http://onlinelife.club";

    private final static String PARAM_DO = "do";
    private final static String PARAM_SUBACTION = "subaction";
    private final static String PARAM_MODE = "mode";
    private final static String PARAM_STORY = "story";
    private final static String PARAM_SEARCH_START = "search_start";
    private final static String search = "search";
    private final static String simple = "simple";

    private final static String PARAM_IMAGE_WIDTH = "w";
    private final static String PARAM_IMAGE_HEIGHT = "h";
    private final static String PARAM_IMAGE_ZC = "zc";
    private final static String ZC = "1";

    public static String buildSearchUrl(String query) {
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
                .build();

        //Only need this to be able to use story encoded in windows-1251 as appendQueryParameter only uses UTF-8
        builtUri = Uri.parse(builtUri.toString() + "&" + PARAM_STORY + "=" + story);

        return builtUri.toString();
    }

    public static String buildNextLink(String startUrl, String page) {
        Uri builtUri = Uri.parse(startUrl);
        return builtUri.buildUpon()
                       .appendQueryParameter(PARAM_SEARCH_START, page)
                       .build()
                       .toString();
    }

    public static String buildImageStringUrl(String image, int w, int h) {
        String width = Integer.toString(w);
        String height = Integer.toString(h);
        Uri builtUri = Uri.parse(image).buildUpon()
                .appendQueryParameter(PARAM_IMAGE_WIDTH, width)
                .appendQueryParameter(PARAM_IMAGE_HEIGHT, height)
                .appendQueryParameter(PARAM_IMAGE_ZC, ZC)
                .build();
        return builtUri.toString();
    }

    public static String getResponseFromHttpUrl(URL url, String referer) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Referer", referer);
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in, "windows-1251");
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if(hasInput) {
                return scanner.next();
            }else {
                return null;
            }
        }finally {
            urlConnection.disconnect();
        }
    }
}
