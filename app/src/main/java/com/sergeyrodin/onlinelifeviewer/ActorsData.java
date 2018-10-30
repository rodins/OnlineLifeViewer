package com.sergeyrodin.onlinelifeviewer;

import java.util.ArrayList;
import java.util.List;

class ActorsData {
    private String country;
    private String year;
    private String playerLink;
    private List<Actor> actors;

    public List<Actor> getActors() {
        return actors;
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
