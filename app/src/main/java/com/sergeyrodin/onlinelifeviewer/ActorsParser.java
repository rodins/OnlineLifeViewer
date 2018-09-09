package com.sergeyrodin.onlinelifeviewer;

import com.sergeyrodin.onlinelifeviewer.utilities.Html;
import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActorsParser {

    public ActorsData parse(String link) throws IOException{
        ActorsData result = new ActorsData(false, false);
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
                    result.getActors().addAll(parseAnchors(line, isDirector));
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
                    String playerLink = parseIframe(line);
                    if(playerLink != null) {
                        String jsLink = loadJsLink(playerLink);
                        result.setJs(loadJs(jsLink, playerLink));
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
            actors.add(new Actor(Html.unescape(m.group(2)),
                    isDirector,
                    m.group(1)));
        }
        return actors;
    }

    private String parseIframe(String line) {
        int linkBegin = line.indexOf("src=");
        int linkEnd = line.indexOf("'", linkBegin+6);
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

    private String parseJsLink(String line) {
        int begin = line.indexOf("src=");
        int end = line.indexOf("\"", begin+6);
        if(begin != -1 && end != -1) {
            return line.substring(begin+5, end);
        }
        return null;
    }

    private String loadJsLink(String playerLink) throws IOException{
        URL url = new URL(playerLink);
        HttpURLConnection connection = null;
        BufferedReader in = null;
        try {
            connection = (HttpURLConnection)url.openConnection();
            InputStream stream = connection.getInputStream();
            in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
            String line;
            while((line = in.readLine()) != null){
                if(line.contains("js.php")) {
                    return "http:" + parseJsLink(line);
                }
            }
            return null;
        }finally {
            if(in != null) {
                in.close();
            }
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    private String loadJs(String jsLink, String referer) throws IOException {
        URL url = new URL(jsLink);
        return NetworkUtils.getResponseFromHttpUrl(url, referer);
    }
}
