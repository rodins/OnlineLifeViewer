package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoriesActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.action_categories);

        String page = getIntent().getStringExtra(MainActivity.EXTRA_PAGE);

        if(page != null) {
            final ArrayList<Category> categories = getCategories(page);
            ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this,
                    R.layout.playlist_entry, categories) {
                View[] views = new View[categories.size()];
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if(views[position] != null) {
                        return views[position];
                    }else {
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View view = inflater.inflate(R.layout.playlist_entry, parent, false);
                        Category category = getItem(position);
                        TextView textView = (TextView)view.findViewById(R.id.entryText);
                        ImageView imageView = (ImageView)view.findViewById(R.id.entryImage);
                        textView.setText(category.title);
                        imageView.setImageResource(R.drawable.movies_folder);
                        views[position] = view;
                        return view;
                    }
                }
            };

            setListAdapter(adapter);
        }else {
            Toast.makeText(this, R.string.nothing_found, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDuplicate(ArrayList<Category> categories, String title) {
        for(Category c : categories) {
            if(c.title.equals(title)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Category> getCategories(String page) {
        ArrayList<Category> categories = new ArrayList<>();

        //Parse page for categories
        Matcher m = Pattern
                .compile("<li><a\\s+href=\"/(.+?)/\">(.+?)</a></li>")
                .matcher(page);

        while(m.find()){
            String title = m.group(2);
            String link = MainActivity.DOMAIN + "/" + m.group(1) + "/";
            if(isDuplicate(categories, title)) {
                categories.add(new Category(title + " (сериалы)", link));
            }else {
                categories.add(new Category(title, link));
            }
        }
        return categories;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Category category = (Category)getListAdapter().getItem(position);
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(MainActivity.EXTRA_TITLE, category.title);
        intent.putExtra(MainActivity.EXTRA_LINK, category.link);
        startActivity(intent);
    }
}
