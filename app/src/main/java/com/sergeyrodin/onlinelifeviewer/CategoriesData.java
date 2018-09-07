package com.sergeyrodin.onlinelifeviewer;

import java.util.List;

public class CategoriesData {
    private boolean isLoading;
    private List<Link> categories;
    private boolean isError;

    CategoriesData(boolean isLoading, List<Link> categories, boolean isError) {
        this.isLoading = isLoading;
        this.categories = categories;
        this.isError = isError;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public List<Link> getCategories() {
        return categories;
    }

    public boolean isError() {
        return isError;
    }
}
