package com.sergeyrodin.onlinelifeviewer;

import com.sergeyrodin.onlinelifeviewer.utilities.Html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 08.05.16.
 */
public class PlaylistItemParser {
    public PlaylistItem getItem(String item) {
        //TODO: switch to json
        Matcher m = Pattern.compile("\"(file|download|comment)\":\"(.+?)\"").matcher(item);
        PlaylistItem plItem = new PlaylistItem();
        while(m.find()) {
            String key = m.group(1);
            String value = m.group(2);

            switch(key) {
                case "comment":
                    plItem.setComment(Html.unescape(value));
                    break;
                case "file":
                    plItem.setFile(value);
                    break;
                case "download":
                    plItem.setDownload(value);
                    break;
            }
        }
        return plItem;
    }
}
