package com.sergeyrodin.onlinelifeviewer.utilities;

import android.os.Build;

/**
 * Created by sergey on 23.03.18.
 */

public class Html {
    public static String unescape(String htmlString) {
        if (Build.VERSION.SDK_INT >= 24) {
            return android.text.Html.fromHtml(htmlString , android.text.Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return android.text.Html.fromHtml(htmlString).toString();
        }
    }
}
