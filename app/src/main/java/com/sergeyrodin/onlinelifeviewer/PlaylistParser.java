package com.sergeyrodin.onlinelifeviewer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 08.05.16.
 */
class PlaylistParser {
    public Playlist getItem(String playlist) {
        Playlist pl = new Playlist(""); // Playlist title as constructor argument (single playlist has no title)
        try {
            JSONObject ps = new JSONObject(playlist);
            JSONArray season = ps.getJSONArray("playlist");
            for(int i = 0; i < season.length(); i++) {
                JSONObject json = season.getJSONObject(i);
                PlaylistItem playlistItem = new PlaylistItem(json);
                pl.add(playlistItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pl;
    }
}
