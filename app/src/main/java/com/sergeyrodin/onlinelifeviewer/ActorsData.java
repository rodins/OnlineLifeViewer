package com.sergeyrodin.onlinelifeviewer;

import java.util.ArrayList;
import java.util.List;

class ActorsData {
    private boolean isLoading;
    private String country;
    private String year;
    private String playerLink;
    private List<Actor> actors;
    private boolean isError;

    ActorsData(boolean isLoading,
               boolean isError) {
        this.isLoading = isLoading;
        this.isError = isError;
        actors = new ArrayList<>();
    }

    boolean isLoading() {
        return isLoading;
    }

    public List<Actor> getActors() {
        return actors;
    }

    boolean isError() {
        return isError;
    }

    String getCountry() {
        return country;
    }

    void setCountry(String country) {
        this.country = country;
    }

    String getYear() {
        return year;
    }

    void setYear(String year) {
        this.year = year;
    }

    String getPlayerLink() {
        return playerLink;
    }

    void setPlayerLink(String playerLink) {
        this.playerLink = playerLink;
    }

    void setActors(List<Actor> actors) {
        this.actors = actors;
    }
}
