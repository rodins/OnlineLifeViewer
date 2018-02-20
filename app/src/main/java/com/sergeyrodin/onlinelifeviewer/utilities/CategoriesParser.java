package com.sergeyrodin.onlinelifeviewer.utilities;

import android.util.Log;

import com.sergeyrodin.onlinelifeviewer.Link;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sergey on 20.02.18.
 */

public class CategoriesParser {
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
            if(s.contains("<div class=\"nav\">")) {
                sb.append(s);
                continue;
            }

            // Add line in the middle
            if(sb.length() > 0 && !s.contains("</div>")) {
                sb.append(s);
                continue;
            }

            // Add end
            if(sb.length() > 0 && s.contains("</div>")) {
                sb.append(s);
                return sb.toString();
            }

        }
        return null;
    }

    public static List<Link> parseCategories(String html) {

        // Get elements from nav div into list
        List<Link> categories = new ArrayList<>();
        if(html != null && !html.isEmpty()) {
            Matcher m3 = Pattern.compile("<li class=\"pull-right nodrop\"><a href=\"(.+?)\">(.+?)</a></li>")
                    .matcher(html);
            List<Link> links3 = new ArrayList<>();
            while(m3.find()) {
                links3.add(new Link(m3.group(2), fixUrl(m3.group(1))));
            }

            // Get trailers link
            m3 = Pattern.compile("<li class=\"nodrop\" style=\"margin-left: 10px;\"><a href=\"(.+?)\" class=\"link1\">(.+?)</a>")
                    .matcher(html);
            if(m3.find()) {
                links3.add(new Link(m3.group(2), fixUrl(m3.group(1))));
            }

            categories.add(new Link("Главная", NetworkUtils.ONLINE_LIFE_BASE_URL, links3));

            Matcher m = Pattern.compile("(?s)<li class=\"drop\"><a href=\"(.*?)\".*?>(.*?)</a>.*?</ul>.*?</li>")
                    .matcher(html);

            Matcher m1 = Pattern.compile("<li><a href=\"(.*?)\">(.*?)</a></li>")
                    .matcher("");

            while(m.find()) {
                List<Link> links = new ArrayList<>();
                m1.reset(m.group(0));
                while(m1.find()) {
                    links.add(new Link(m1.group(2), fixUrl(m1.group(1))));
                }
                categories.add(new Link(m.group(2), fixUrl(m.group(1)), links));
            }
        }
        return categories;
    }
}
