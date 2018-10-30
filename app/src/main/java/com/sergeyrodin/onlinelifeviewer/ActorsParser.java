package com.sergeyrodin.onlinelifeviewer;

import android.util.Log;

import com.sergeyrodin.onlinelifeviewer.utilities.Html;
import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ActorsParser {
    private final String LOG_TAG = getClass().getSimpleName();

    ActorsData parse(String link) throws IOException{
        ActorsData result = new ActorsData();
        URL url = new URL(link);
        HttpURLConnection connection = null;
        BufferedReader in = null;
        boolean spanFound = false;
        boolean isDirector = false;
        boolean infoDataFound = false;
        boolean countryFound = false;
        try {
            connection = (HttpURLConnection)url.openConnection();
            InputStream stream = connection.getInputStream();
            in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
            String line;
            while((line = in.readLine()) != null){
                if(line.contains("Режиссеры") && !spanFound) {
                    spanFound = true;
                    isDirector = true;
                    continue;
                }
                if(line.contains("В ролях") && !spanFound) {
                    spanFound = true;
                    continue;
                }
                if(spanFound && !line.contains("span")) {
                    result.setActors(parseAnchors(line, isDirector));
                }
                if(line.contains("</span>") && spanFound) {
                    spanFound = false;
                    isDirector = false;
                }

                if(line.contains("info_data")) {
                    if(line.contains("</span>")) {
                        if(line.contains("<li>")) {
                            result.setYear(parseYear(line));
                        }
                    }else {
                        infoDataFound = true;
                    }
                    continue;
                }

                if(line.contains("</span>") && infoDataFound) {
                    infoDataFound = false;
                }

                if(infoDataFound) {
                    if(!line.contains("<")) {
                        if(!countryFound) {
                            result.setCountry(line.trim());
                            countryFound = true;
                        }
                    }
                }

                if(line.contains("<iframe")) {
                    String iframeLink = parseIframe(line);
                    if(iframeLink != null) {
                        String playerCode = loadPlayerCode(iframeLink, link);
                        //Log.d(LOG_TAG, playerCode);
                        String playerLink = parsePlayerCode(playerCode);
                        result.setPlayerLink(playerLink);
                    }
                    return result;
                }
            }
            return result;
        }finally {
            if(in != null) {
                in.close();
            }
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<Actor> parseAnchors(String line, boolean isDirector) {
        List<Actor> actors = new ArrayList<>();
        Matcher m = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(line);
        while(m.find()) {
            actors.add(new Actor(Html.unescape(m.group(2)), isDirector, m.group(1)));
        }
        return actors;
    }

    private String parseIframe(String line) {
        int linkBegin = line.indexOf("src=");
        int linkEnd = line.indexOf("\"", linkBegin+6);
        if(linkBegin != -1 && linkEnd != -1) {
            return line.substring(linkBegin+5, linkEnd);
        }
        return null;
    }

    private String parseYear(String line) {
        int yearBegin = line.indexOf("\">");
        int yearEnd = line.indexOf("<", yearBegin);
        if(yearBegin != -1 && yearEnd != -1) {
            return line.substring(yearBegin+2, yearEnd);
        }
        return null;
    }

    private String parsePlayerCode(String code) throws UnsupportedEncodingException {
        int begin = code.indexOf("ref_url:");
        int end = code.indexOf("\"", begin+20);
        if(begin != -1 && end != -1) {
            String encodedUrl = code.substring(begin+10, end);
            return URLDecoder.decode(encodedUrl, "UTF-8");
        }
        return null;
    }

    private String loadPlayerCode(String iframeLink, String referer) throws IOException {
        URL url = new URL(iframeLink);
        return NetworkUtils.getResponseFromHttpUrl(url, referer);
    }
}
