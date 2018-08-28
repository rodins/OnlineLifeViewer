package com.sergeyrodin.onlinelifeviewer;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 08.05.16.
 */
class SeasonsJsonParser {
    String getSeasonsJson(String page) throws IOException {
        Matcher m = Pattern.compile("\"pl\":\"(.+?)\"").matcher(page);
        if(m.find()){
            URL url = new URL(m.group(1));
            return NetworkUtils.getResponseFromHttpUrl(url);
        }
        return null;
    }
}
