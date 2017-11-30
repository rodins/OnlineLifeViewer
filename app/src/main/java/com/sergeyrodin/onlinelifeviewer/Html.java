package com.sergeyrodin.onlinelifeviewer;

/**
 * Created by root on 17.05.16.
 */
public class Html {
    public static String unescape(String text) {
        text = text.replace("&lt", ">");
        text = text.replace("&gt", "<");
        text = text.replace("&amp", "&");
        text = text.replace("&quot", "'");
        text = text.replace("&#39;", "'");
        text = text.replace("&#249;", "ù");
        return text;
    }
}
