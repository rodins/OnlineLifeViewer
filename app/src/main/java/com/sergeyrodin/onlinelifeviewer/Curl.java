package com.sergeyrodin.onlinelifeviewer;

import android.util.Log;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by root on 07.05.16.
 */
public class Curl {
    private String curl(URL url, String referer, boolean isHtml) throws IOException{
        StringBuilder sb = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:43.0) Gecko/20100101 Firefox/43.0 SeaMonkey/2.40");
        if(!referer.equals("")) {
            connection.setRequestProperty("Referer", referer);
        }
        String charset;
        if(isHtml) {
            charset = "windows-1251";//http encoding
        }else {
            charset = "UTF-8";//js encoding
        }
        InputStream stream = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(stream, Charset.forName(charset)));
        try {
            String s;
            while((s = in.readLine()) != null){
                sb.append(s + "\n");
            }
        }finally{
            in.close();
        }
        return sb.toString();
    }

    public String getPageString(URL url) throws IOException {
        return curl(url, "", true); //encoding: windows-1251
    }

    public String getJsString(int id) throws IOException {
        return curl(NetworkUtils.buildJsonUrl(id),
                    NetworkUtils.buildRefererUrl(id),
                    true);
    }

    public String getJsLinkString(URL url) throws IOException {
        return curl(url, "", false); //encoding: utf-8
    }
}
