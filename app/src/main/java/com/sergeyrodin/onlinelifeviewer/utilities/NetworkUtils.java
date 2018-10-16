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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final static String JSON_BASE_URL = "http://play.cidwo.com";
    private final static String PATH_JSON = "js.php";
    private final static String PARAM_ID = "id";
    private final static String PATH_PLAYER = "player.php";
    private final static String PARAM_NEWS_ID = "newsid";

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

    private static String getLinkId(String link) {
        Matcher m = Pattern
                .compile("/(\\d+?)-")
                .matcher(link);
        if(m.find()){
            return m.group(1);
        }
        return  "";
    }

    private static URL buildJsonUrl(String id) throws MalformedURLException {
        Uri builtUri = Uri.parse(JSON_BASE_URL).buildUpon()
                .appendPath(PATH_JSON)
                .appendQueryParameter(PARAM_ID, id)
                .build();
        return new URL(builtUri.toString());
    }

    private static String buildRefererUrl(String id) {
        Uri builtUri = Uri.parse(JSON_BASE_URL).buildUpon()
                .appendPath(PATH_PLAYER)
                .appendQueryParameter(PARAM_NEWS_ID, id)
                .build();
        return builtUri.toString();
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

    public static String getConstantLinksJs(URL url) throws IOException {
        String id = getLinkId(url.toString());
        return getResponseFromHttpUrl(buildJsonUrl(id), buildRefererUrl(id));
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
            try {
                connection = (HttpURLConnection)url.openConnection();
                return connection.getHeaderField("content-length");
            }finally {
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
