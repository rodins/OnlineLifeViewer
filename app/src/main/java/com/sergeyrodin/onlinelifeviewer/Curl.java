package com.sergeyrodin.onlinelifeviewer;

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
    private String curl(String addr, String referer, boolean isHtml) throws IOException{
        StringBuilder sb = new StringBuilder();

        URL url = new URL(addr);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:43.0) Gecko/20100101 Firefox/43.0 SeaMonkey/2.40");
        if(referer != "") {
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

    public String getPageString(String addr) throws IOException {
        return curl(addr, "", true); //encoding: windows-1251
    }

    public String getJsString(int id) throws IOException {
        String addr = "http://dterod.com/js.php?id=" + id;
        String referer = "http://dterod.com/player.php?newsid=" + id;
        return curl(addr, referer, true);
    }

    public String getJsLinkString(String addr) throws IOException {
        return curl(addr, "", false); //encoding: utf-8
    }
}
