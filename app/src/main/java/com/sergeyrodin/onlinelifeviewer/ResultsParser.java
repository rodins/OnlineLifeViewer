package com.sergeyrodin.onlinelifeviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 07.05.16.
 */
class ResultsParser {
    private String page;
    private int pageNumber = 0;
    private String prevLink, nextLink;
    private int prevPage  = 0, nextPage = 0;

    ResultsParser(String page){
        this.page = page;
    }

    private boolean isDuplicate(List<Result> results, int id) {
        for(Result r : results) {
            if(r.id == id) {
                return true;
            }
        }
        return false;
    }

    ArrayList<Result> getItems() {
        //Find html-page links, turn them into titles and id's
        ArrayList<Result> results = new ArrayList<>();
        //TODO: make regexp domain suffix independent
        Matcher m = Pattern
                .compile("<a\\s+href=\"http://www.online-life.club/(\\d+?)-.*?html\"\\s*?>\\n\\s*<img\\s+src=\"(.*?)\"\\s+/>(.+?)\\n?\\s*</a>")
                .matcher(page);
        while(m.find()){
            int id = Integer.parseInt(m.group(1));
            String image = m.group(2);
            image = image.substring(0, image.indexOf("&"));
            String title = Html.unescape(m.group(3));
            /*if(!isDuplicate(results, id)) {
                results.add(new Result(title, image, id));
            }*/
            results.add(new Result(title, image, id));
        }
        return results;
    }

    void navigationInfo() {
        Matcher m = Pattern
                .compile("<div class=\"navigation\" align=\"center\" >.*?</div>")
                .matcher(page);
        if(m.find()){
            String nav = m.group();
            //find current page
            m = Pattern.compile("<span>(.+?)</span>").matcher(nav);
            while(m.find()) {
                if(m.group(1).length() < 5) {
                    pageNumber = Integer.parseInt(m.group(1));
                }
            }

            // non-search page navigation links
            m = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(nav);
            while(m.find()) {
                if(m.group(2).length() == 5) {
                    prevLink = m.group(1);
                }
                if(m.group(2).length() == 6) {
                    nextLink = m.group(1);
                }
            }

            // search page navigation links
            m = Pattern.compile("<a.+?onclick=\".+?(\\d+).+?\">(.+?)</a>").matcher(nav);
            while(m.find()) {
                if(m.group(2).length() == 5) {
                    prevPage = Integer.parseInt(m.group(1));
                }
                if(m.group(2).length() == 6) {
                    nextPage = Integer.parseInt(m.group(1));
                }
            }
        }
    }

    int getPageNumber() {
        return pageNumber;
    }

    String getPrevLink() {
        return prevLink;
    }

    String getNextLink() {
        return nextLink;
    }

    int getNextPage() { return nextPage; }

    int getPrevPage() { return prevPage; }
}
