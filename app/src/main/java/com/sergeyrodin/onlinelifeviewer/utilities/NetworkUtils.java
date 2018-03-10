package com.sergeyrodin.onlinelifeviewer.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Created by sergey on 30.11.17.
 */

public class NetworkUtils {
    final static String ONLINE_LIFE_BASE_URL = "http://online-life.club";
    final static String TAG = NetworkUtils.class.getSimpleName();

    private final static String PARAM_DO = "do";
    private final static String PARAM_SUBACTION = "subaction";
    private final static String PARAM_MODE = "mode";
    private final static String PARAM_STORY = "story";
    private final static String PARAM_SEARCH_START = "search_start";
    private final static String search = "search";
    private final static String simple = "simple";

    private final static String JSON_BASE_URL = "http://dterod.com";
    private final static String PATH_JSON = "js.php";
    private final static String PARAM_ID = "id";
    private final static String PATH_PLAYER = "player.php";
    private final static String PARAM_NEWS_ID = "newsid";

    private final static String PARAM_IMAGE_WIDTH = "w";
    private final static String PARAM_IMAGE_HEIGHT = "h";
    private final static String PARAM_IMAGE_ZC = "zc";
    private final static String ZC = "1";

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
                .build();

        //Only need this to be able to use story encoded in windows-1251 as appendQueryParameter only uses UTF-8
        builtUri = Uri.parse(builtUri.toString() + "&" + PARAM_STORY + "=" + story);

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

    public static URL buildJsonUrl(int id) throws MalformedURLException {
        Uri builtUri = Uri.parse(JSON_BASE_URL).buildUpon()
                .appendPath(PATH_JSON)
                .appendQueryParameter(PARAM_ID, Integer.toString(id))
                .build();
        return new URL(builtUri.toString());
    }

    public static String buildRefererUrl(int id) {
        Uri builtUri = Uri.parse(JSON_BASE_URL).buildUpon()
                .appendPath(PATH_PLAYER)
                .appendQueryParameter(PARAM_NEWS_ID, Integer.toString(id))
                .build();
        return builtUri.toString();
    }

    public static URL buildImageUrl(String image, String width, String height) throws MalformedURLException {
        Uri builtUri = Uri.parse(image).buildUpon()
                .appendQueryParameter(PARAM_IMAGE_WIDTH, width)
                .appendQueryParameter(PARAM_IMAGE_HEIGHT, height)
                .appendQueryParameter(PARAM_IMAGE_ZC, ZC)
                .build();
        return new URL(builtUri.toString());
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException{
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
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

    public static String getLinkSize(URL url) {
        try {
            HttpURLConnection connection = null;
            BufferedReader in = null;
            try {
                connection = (HttpURLConnection)url.openConnection();
                return connection.getHeaderField("content-length");
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
