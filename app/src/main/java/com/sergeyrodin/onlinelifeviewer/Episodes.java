package com.sergeyrodin.onlinelifeviewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 08.05.16.
 */
class Episodes implements Serializable {
    private String _title;
    private List<VideoItem> _items = new ArrayList<>();
    Episodes(String t) {
        _title = t;
    }
    public void add(VideoItem item) {
        _items.add(item);
    }

    public String getTitle() {
        return _title;
    }
    List<VideoItem> getItems() {
        return _items;
    }

    public String toString() {
        return _title;
    }
}
