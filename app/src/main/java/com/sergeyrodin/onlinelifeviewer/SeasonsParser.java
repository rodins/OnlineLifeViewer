package com.sergeyrodin.onlinelifeviewer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by root on 08.05.16.
 */
class SeasonsParser {
    ArrayList<Episodes> getItems(String page) {
        //Test json parser
        ArrayList<Episodes> playlists = new ArrayList<>();
        try {
            JSONObject ps = new JSONObject(page);
            JSONArray seasons = ps.getJSONArray("playlist");
            for(int i = 0; i < seasons.length(); i++) {
                JSONObject season = seasons.getJSONObject(i);
                String comment = season.getString("comment");
                JSONArray playItems = season.optJSONArray("playlist");
                if(playItems != null) {
                    Episodes pl = new Episodes(comment); // Episodes title as constructor argument
                    for(int j = 0; j < playItems.length(); j++) {
                        JSONObject playItem = playItems.getJSONObject(j);
                        VideoItem videoItem = new VideoItem(playItem);
                        pl.add(videoItem);
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
