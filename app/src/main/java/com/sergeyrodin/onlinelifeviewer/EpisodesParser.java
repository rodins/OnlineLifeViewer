package com.sergeyrodin.onlinelifeviewer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by root on 08.05.16.
 */
class EpisodesParser {
    public Episodes getItem(String playlist) {
        Episodes pl = new Episodes(""); // Episodes title as constructor argument (single playlist has no title)
        try {
            JSONObject ps = new JSONObject(playlist);
            JSONArray season = ps.getJSONArray("playlist");
            for(int i = 0; i < season.length(); i++) {
                JSONObject json = season.getJSONObject(i);
                VideoItem videoItem = new VideoItem(json);
                pl.add(videoItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pl;
    }
}
