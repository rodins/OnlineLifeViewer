package com.sergeyrodin.onlinelifeviewer;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.text.BreakIterator;

/**
 * Created by root on 07.05.16.
 */
public class Result {
    public final String title;
    public final String link;
    public final String image;

    Result(String title, String image, String link) {
        this.title = title;
        this.image = image; //+ "&w=82&h=118&zc=1";
        this.link = link;
    }

    public String toString() {
        return title;
    }
}
