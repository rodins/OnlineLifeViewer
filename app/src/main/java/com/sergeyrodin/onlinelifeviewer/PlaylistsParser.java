package com.sergeyrodin.onlinelifeviewer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by root on 08.05.16.
 */
class PlaylistsParser {
    ArrayList<Playlist> getItems(String page) {
        //Test json parser
        ArrayList<Playlist> playlists = new ArrayList<>();
        try {
            JSONObject ps = new JSONObject(page);
            JSONArray seasons = ps.getJSONArray("playlist");
            for(int i = 0; i < seasons.length(); i++) {
                JSONObject season = seasons.getJSONObject(i);
                String comment = season.getString("comment");
                JSONArray playItems = season.optJSONArray("playlist");
                if(playItems != null) {
                    Playlist pl = new Playlist(comment); // Playlist title as constructor argument
                    for(int j = 0; j < playItems.length(); j++) {
                        JSONObject playItem = playItems.getJSONObject(j);
                        PlaylistItem playlistItem = new PlaylistItem(playItem);
                        pl.add(playlistItem);
                    }
                    playlists.add(pl);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return playlists;
    }
}
