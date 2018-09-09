package com.sergeyrodin.onlinelifeviewer;

import java.util.ArrayList;
import java.util.List;

class ActorsData {
    private boolean isLoading;
    private String country;
    private String year;
    private String js;
    private List<Actor> actors;
    private boolean isError;

    ActorsData(boolean isLoading,
               boolean isError) {
        this.isLoading = isLoading;
        this.isError = isError;
        actors = new ArrayList<>();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public boolean isError() {
        return isError;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getJs() {
        return js;
    }

    public void setJs(String js) {
        this.js = js;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }
}
