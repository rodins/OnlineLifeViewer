package com.sergeyrodin.onlinelifeviewer;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.text.BreakIterator;

/**
 * Created by root on 07.05.16.
 */
public class Result {
    public final String title;
    public final String image;
    private Bitmap bitmap;
    public int id;
    public Result(String title, int id) {
        this.title = title;
        this.id = id;
        image = null;
        bitmap = null;
    }

    Result(String title, String image, int id) {
        this.title = title;
        this.image = image + "&w=82&h=118&zc=1";
        this.id = id;
        bitmap = null;
    }

    public Result(String title, Bitmap bitmap, int id) {
        this.title = title;
        this.bitmap = bitmap;
        this.id = id;
        image = null;
    }

    void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    Bitmap getBitmap() {
        return bitmap;
    }

    public String toString() {
        return title;
    }
}
