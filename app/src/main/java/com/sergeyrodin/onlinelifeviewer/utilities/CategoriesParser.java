package com.sergeyrodin.onlinelifeviewer.utilities;

import android.util.Log;

import com.sergeyrodin.onlinelifeviewer.Link;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sergey on 20.02.18.
 */

public class CategoriesParser {
    private static final String TAG = CategoriesParser.class.getSimpleName();

    private static String fixUrl(String href) { // I put it here because it is only needed for categories
        if(href.contains("http")) {
            return href;
        }else {
            return NetworkUtils.ONLINE_LIFE_BASE_URL + href;
        }
    }

    public static String getCategoriesPart(BufferedReader in) throws Exception {
        StringBuilder sb = new StringBuilder();
        // Get only nav div from entire page
        String s;
        while((s = in.readLine()) != null) {
            // Find beginning
            if(s.contains("<nav")) {
                sb.append(s);
                continue;
            }

            // Add line in the middle
            if(sb.length() > 0 && !s.contains("login-btn")) {
                sb.append(s);
                continue;
            }

            // Add end
            if(sb.length() > 0 && s.contains("login-btn")) {
                sb.append(s);
                return sb.toString();
            }
        }
        return null;
    }

    public static List<Link> parseCategories(String html) {
        List<Link> categories = new ArrayList<>();
        if(html != null && !html.isEmpty()) {
            Matcher mMain = Pattern.compile("<li>\\s+?<a rel=\"external\" href=\"([a-z/]+?)\">\\s+?<span class=\"menu-icon[ a-z]+?\"><svg viewbox=\"[ 0-1]+?\"><use xlink:href=\"#[a-z]+?-icon\"></use></svg></span>([ а-яА-Я]+?)</a>\\s+?</li>")
                                .matcher(html);
            LinkedList<Link> linksMain = new LinkedList<>();

            // New, Popular, Best, Trailers
            while(mMain.find()) {
                linksMain.push(new Link(mMain.group(2).trim(), fixUrl(mMain.group(1))));
            }

            // Subcategories for main
            categories.add(new Link("Главная", "", linksMain));

            // Categories
            Matcher mCategories = Pattern.compile("(?s)<li>\\s+?<a\\s+?rel=\"external\"\\s*?href=\"(.*?)\">\\s+?<span class=.*?</span>(.*?)</a>.*?</ul>")
                                         .matcher(html);

            // Subcategories
            Matcher mSubcategories = Pattern.compile("<li\\s*?><a\\s+?rel=\"external\"\\s*?href=\"(.*?)\">(.*?)</a></li>")
                                            .matcher("");

            // Most of the categories
            while(mCategories.find()) {
                List<Link> links = new ArrayList<>();
                mSubcategories.reset(mCategories.group(0));
                while(mSubcategories.find()) {
                    links.add(new Link(mSubcategories.group(2), fixUrl(mSubcategories.group(1))));
                }
                categories.add(new Link(mCategories.group(2).trim(), "", links));
            }
        }
        return categories;
    }
}
