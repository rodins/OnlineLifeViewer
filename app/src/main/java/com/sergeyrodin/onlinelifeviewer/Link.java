package com.sergeyrodin.onlinelifeviewer;

import java.util.List;

/**
 * Created by sergey on 20.02.18.
 */

public class Link {
    public final String Title;
    public final String Href;
    public final List<Link> Links;
    public final String Id;

    public Link(String title, String href) {
        Title = title;
        Href = href;
        Links = null;
        Id = null;
    }

    public Link(String title, String href, List<Link> links) {
        Title = title;
        Href = href;
        Links = links;
        Id = null;
    }

    public Link(String title, String href, String id) {
        Title = title;
        Href = href;
        Links = null;
        Id = id;
    }

    @Override
    public String toString() {
        return Title;
    }
}
