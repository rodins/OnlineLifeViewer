package com.sergeyrodin.onlinelifeviewer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by root on 08.05.16.
 */
public class VideoItem implements Serializable {
    private String _comment;
    private String _file;
    private String _download;
    private String _fileSize;
    private String _downloadSize;

    VideoItem() {

    }

    VideoItem(JSONObject json) throws JSONException {
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

    public String toString() {
        return _comment;
    }

    private String fixDownloadLink(String link) {
        return link.split("\\?")[0];
    }
}
