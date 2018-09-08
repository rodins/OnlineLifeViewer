package com.sergeyrodin.onlinelifeviewer;

import java.util.List;

public class LinkData {
    private boolean isLoading;
    private List<Season> seasons;
    private Season season;
    private VideoItem videoItem;
    private boolean isError;

    LinkData(boolean isLoading,
             List<Season> seasons,
             Season season,
             VideoItem videoItem,
             boolean isError) {
        this.isLoading = isLoading;
        this.seasons = seasons;
        this.season = season;
        this.videoItem = videoItem;
        this.isError = isError;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public List<Season> getSeasons() {
        return seasons;
    }

    public Season getSeason() {
        return season;
    }

    public VideoItem getVideoItem() {
        return videoItem;
    }

    public boolean isError() {
        return isError;
    }
}
