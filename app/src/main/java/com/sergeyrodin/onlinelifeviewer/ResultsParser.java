package com.sergeyrodin.onlinelifeviewer;

import android.util.Log;

import com.sergeyrodin.onlinelifeviewer.utilities.Html;
import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ResultsParser {
    private List<Result> data;
    private String startLink, nextLink;

    List<Result> getData() {
        return data;
    }

    void setStartLink(String startLink) {
        this.startLink = startLink;
    }

    String getNextLink() {
        return nextLink;
    }

    void init() {
        nextLink = null;
        data = new ArrayList<>();
    }

    void parse(BufferedReader in) throws IOException {
        String line;
        String div = "";
        boolean div_found = false;
        boolean div_mobile_found = false;
        boolean div_nav_found = false;
        while((line = in.readLine()) != null){
            if(line.contains("class=\"custom-poster\"") && !div_found) {
                div_found = true;
            }
            if(line.contains("</a>") && div_found) {
                div_found = false;
                div += line;
                Result result = divToResult(div);
                if(result != null) {
                    data.add(result);
                }
                div = "";
            }
            if(div_found) {
                div += line + "\n";
            }

            if(line.contains("class=\"slider-item\"")) {
                div_mobile_found = true;
                div = "";
                continue;
            }

            if(line.contains("</a>") && div_mobile_found) {
                div_mobile_found = false;
                Result result = divToMobileResult(div);
                if(result != null) {
                    data.add(result);
                }
            }

            if(div_mobile_found) {
                if(!line.contains("<div") && !line.contains("</div>")) {
                    div += line;
                }
            }

            if(line.contains("class=\"navigation\"")) {
                div_nav_found = true;
                div = "";
                continue;
            }

            if(line.contains("</div>") && div_nav_found) {
                div += line.trim();
                parseNavigation(div);
                return;
            }

            if(div_nav_found) {
                div += line.trim();
            }
        }
    }

    private void parseNavigation(String nav) {
        String nl = null;
        Matcher m;
        // non-search page navigation links
        m = Pattern.compile("<a\\s+href=\"(\\S+?)\">></a>").matcher(nav);
        if(m.find()) {
            nl = m.group(1);
        }

        if(nl != null && !nl.isEmpty()) {
            nextLink = nl;
        }else {
            // search page navigation links
            m = Pattern.compile("<a\\s+name=\"nextlink\".+?onclick=\".+?(\\d+).+?\">></a>").matcher(nav);
            if(m.find()) {
                nl = m.group(1);
            }

            if(nl != null && !nl.isEmpty()) {
                nextLink = NetworkUtils.buildNextLink(startLink, nl); //forming next search link
            }
        }
    }

    private Result divToResult(String div) {
        Matcher m = Pattern
                .compile("<a\\s+href=\"(.+?)\"\\s*?>\\n\\s*<img\\s+src=\"(.*?)\"\\s+/>(.+?)\\n?\\s*</a>")
                .matcher(div);
        if(m.find()) {
            String link = m.group(1);
            String image = m.group(2);
            image = image.substring(0, image.indexOf("&"));
            String title = Html.unescape(m.group(3));
            return new Result(title, image, link);
        }
        return null;
    }

    private Result divToMobileResult(String div) {
        Matcher m = Pattern
                .compile("<a\\s+href=\"(.*?)\".*?src=\"(.*?)\".*?\">(.+?)</span>")
                .matcher(div);
        if(m.find()) {
            String link = m.group(1);
            String image = m.group(2);
            image = image.substring(0, image.indexOf("&"));
            String title = Html.unescape(m.group(3));
            return new Result(title, image, link);
        }
        return null;
    }
}
