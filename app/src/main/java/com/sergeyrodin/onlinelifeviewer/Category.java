package com.sergeyrodin.onlinelifeviewer;

/**
 * Created by root on 16.05.16.
 */
public class Category {
    public final String title;
    public final String link;
    public Category(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public String toString() {
        return title;
    }
}
