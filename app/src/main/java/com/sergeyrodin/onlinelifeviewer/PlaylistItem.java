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
    private String _fileSize;
    private String _downloadSize;
    private String _infoTitle;
    private String _infoLink;

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
    void setFileSize(String size) {
        _fileSize = size;
    }
    void setDownloadSize(String size) {
        _downloadSize = size;
    }
    void setInfoTitle(String title) {
        _infoTitle = title;
    }
    void setInfoLink(String link) {
        _infoLink = link;
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
    String getFileSize() {
        return _fileSize;
    }
    String getDownloadSize() {
        return _downloadSize;
    }
    String getInfoTitle() {
        return _infoTitle;
    }
    String getInfoLink() {
        return _infoLink;
    }

    public String toString() {
        return _comment;
    }

    private String fixDownloadLink(String link) {
        return link.split("\\?")[0];
    }
}
