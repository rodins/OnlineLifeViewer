package com.sergeyrodin.onlinelifeviewer;

import com.sergeyrodin.onlinelifeviewer.utilities.Html;
import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ResultsParser {
    private ResultsData data;
    private String startLink, nextLink;

    ResultsData getData() {
        return data;
    }

    void setStartLink(String startLink) {
        this.startLink = startLink;
    }

    String getNextLink() {
        return nextLink;
    }

    void parse(BufferedReader in) throws IOException {
        data = new ResultsData();
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
        nextLink = "";
        String nl = "";
        Matcher m;
        // non-search page navigation links
        m = Pattern.compile("<a\\s+href=\"(\\S+?)\">></a>").matcher(nav);
        if(m.find()) {
            nl = m.group(1);
        }

        if(!nl.isEmpty()) {
            nextLink = nl;
        }else {
            // search page navigation links
            m = Pattern.compile("<a\\s+name=\"nextlink\".+?onclick=\".+?(\\d+).+?\">></a>").matcher(nav);
            if(m.find()) {
                nl = m.group(1);
            }

            if(!nl.isEmpty()) {
                nextLink = NetworkUtils.buildNextLink(startLink, nl); //forming next search link
            }
        }
    }

    private Result divToResult(String div) {
        Matcher m = Pattern
                .compile("<a\\s+href=\"(http://www.online-life.[a-z]+?/\\d+?-.*?html)\"\\s*?>\\n\\s*<img\\s+src=\"(.*?)\"\\s+/>(.+?)\\n?\\s*</a>")
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
