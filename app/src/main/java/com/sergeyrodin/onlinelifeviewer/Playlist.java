package com.sergeyrodin.onlinelifeviewer;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 08.05.16.
 */
class Playlist implements Serializable {
    private String _title;
    private List<PlaylistItem> _items = new ArrayList<>();
    Playlist(String t) {
        _title = t;
    }
    public void add(PlaylistItem item) {
        _items.add(item);
    }

    public String getTitle() {
        return _title;
    }
    List<PlaylistItem> getItems() {
        return _items;
    }

    public String toString() {
        return _title;
    }
}
