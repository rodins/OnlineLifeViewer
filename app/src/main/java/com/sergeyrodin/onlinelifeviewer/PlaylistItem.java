package com.sergeyrodin.onlinelifeviewer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by root on 08.05.16.
 */
public class PlaylistItem implements Serializable {
    private String _comment;
    private String _file;
    private String _download;

    PlaylistItem() {

    }

    PlaylistItem(JSONObject json) throws JSONException {
        _comment = json.getString("comment");
        _file = json.getString("file");
        _download = fixDownloadLink(json.getString("download"));
    }

    void setComment(String c) {
        _comment = c;
    }
    public void setFile(String f) {
        _file = f;
    }
    void setDownload(String d) {
        _download = fixDownloadLink(d);
    }

    String getComment() {
        return _comment;
    }
    public String getFile() {
        return _file;
    }
    String getDownload() {
        return _download;
    }

    public String toString() {
        return _comment;
    }

    private String fixDownloadLink(String link) {
        return link.split("\\?")[0];
    }
}
