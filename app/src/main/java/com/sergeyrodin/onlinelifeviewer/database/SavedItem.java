package com.sergeyrodin.onlinelifeviewer.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "saved_items")
public class SavedItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String link;
    private String image;

    @Ignore
    public SavedItem(String title, String link, String image) {
        this.title = title;
        this.link = link;
        this.image = image;
    }

    public SavedItem(int id, String title, String link, String image) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.image = image;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
