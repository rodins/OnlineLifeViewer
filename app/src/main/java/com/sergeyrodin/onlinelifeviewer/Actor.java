package com.sergeyrodin.onlinelifeviewer;

public class Actor {
    public final String title;
    public final boolean isDirector;
    public final String href;
    Actor(String title, boolean isDirector, String href) {
        this.title = title;
        this.isDirector = isDirector;
        this.href = href;
    }
}
