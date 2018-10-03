package com.sergeyrodin.onlinelifeviewer;

import java.util.List;

public class LinkData {
    private List<Season> seasons;
    private Season season;
    private VideoItem videoItem;
    private boolean isError;

    LinkData() {
        seasons = null;
        season = null;
        videoItem = null;
        isError = false;
    }

    public void setSeasons(List<Season> seasons) {
        this.seasons = seasons;
    }

    public List<Season> getSeasons() {
        return seasons;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public Season getSeason() {
        return season;
    }

    public void setVideoItem(VideoItem videoItem) {
        this.videoItem = videoItem;
    }

    public VideoItem getVideoItem() {
        return videoItem;
    }

    public void setError() {
        isError = true;
    }

    public boolean isError() {
        return isError;
    }
}
