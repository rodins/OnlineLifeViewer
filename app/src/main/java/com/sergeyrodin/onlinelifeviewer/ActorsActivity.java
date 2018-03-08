package com.sergeyrodin.onlinelifeviewer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActorsActivity extends AppCompatActivity {
    private final static String TAG = ActorsActivity.class.getSimpleName();
    private RecyclerView mRvActors;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actors);

        setTitle(R.string.actors_title);

        mRvActors = (RecyclerView)findViewById(R.id.rv_actors);
        mLoadingIndicator = (ProgressBar)findViewById(R.id.actors_loading_indicator);
        mErrorTextView = (TextView)findViewById(R.id.actors_loading_error);


        Intent intent = getIntent();
        if(intent != null) {
            String link = intent.getStringExtra(MainActivity.EXTRA_LINK);
            //Log.d(TAG, "Link: " + link);
            try {
                URL url = new URL(link);
                new ActorsAsyncTask().execute(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showLoadingIndicator() {
        mRvActors.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
    }

    private void showData() {
        mRvActors.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        mRvActors.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    class ActorsAsyncTask extends AsyncTask<URL, Void, List<Link>> {

        @Override
        protected void onPreExecute() {
            showLoadingIndicator();
        }

        List<Link> parseAnchors(String line, boolean isDirector) {
            List<Link> links = new ArrayList<>();
            Matcher m = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(line);
            while(m.find()) {
                String title = m.group(2) + " " + (isDirector?"(" + getString(R.string.director) + ")":"");
                links.add(new Link(title, m.group(1)));
            }
            return links;
        }

        @Override
        protected List<Link> doInBackground(URL... urls) {
            try {
                URL url = urls[0];
                HttpURLConnection connection = null;
                BufferedReader in = null;
                boolean spanFound = false;
                boolean isDirector = false;
                List<Link> links = new ArrayList<>();
                try {
                    connection = (HttpURLConnection)url.openConnection();
                    //connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:43.0) Gecko/20100101 Firefox/43.0 SeaMonkey/2.40");
                    InputStream stream = connection.getInputStream();
                    in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
                    String line;
                    while((line = in.readLine()) != null){
                        //Log.d(TAG, line);
                        if(line.contains("Режиссеры") && !spanFound) {
                            spanFound = true;
                            isDirector = true;
                            continue;
                        }
                        if(line.contains("В ролях") && !spanFound) {
                            spanFound = true;
                            continue;
                        }
                        if(spanFound && !line.contains("span")) {
                            links.addAll(parseAnchors(line, isDirector));
                        }
                        if(line.contains("</span>") && spanFound) {
                            spanFound = false;
                            isDirector = false;
                        }
                    }
                    return links;
                }finally {
                    if(in != null) {
                        in.close();
                    }
                    if(connection != null) {
                        connection.disconnect();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Link> links) {
            showData();
            for(Link link : links) {
                Log.d(TAG, "Title: " + link.Title);
            }
        }
    }
}
