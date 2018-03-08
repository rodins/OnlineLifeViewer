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

    private List<Link> mActors = new ArrayList<>();
    private String mPlayerLink;

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

    class ActorsAsyncTask extends AsyncTask<URL, Link, String> {

        @Override
        protected void onPreExecute() {
            showLoadingIndicator();
        }

        void parseAnchors(String line, boolean isDirector) {
            Matcher m = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(line);
            while(m.find()) {
                String title = m.group(2) + " " + (isDirector?"(" + getString(R.string.director) + ")":"");
                publishProgress(new Link(Html.unescape(title), m.group(1)));
            }
        }

        String parseIframe(String line) {
            int linkBegin = line.indexOf("src=");
            int linkEnd = line.indexOf("'", linkBegin+6);
            if(linkBegin != -1 && linkEnd != -1) {
                return line.substring(linkBegin+5, linkEnd);
            }
            return null;
        }

        @Override
        protected String doInBackground(URL... urls) {
            try {
                URL url = urls[0];
                HttpURLConnection connection = null;
                BufferedReader in = null;
                boolean spanFound = false;
                boolean isDirector = false;
                try {
                    connection = (HttpURLConnection)url.openConnection();
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
                            parseAnchors(line, isDirector);
                        }
                        if(line.contains("</span>") && spanFound) {
                            spanFound = false;
                            isDirector = false;
                        }
                        if(line.contains("<iframe")) {
                            return parseIframe(line);
                        }
                    }
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
        protected void onProgressUpdate(Link... values) {
            mActors.add(values[0]);
        }

        @Override
        protected void onPostExecute(String playerLink) {
            mPlayerLink = playerLink;
            if(playerLink != null) {
                //showData();
                try {
                    URL url = new URL(playerLink);
                    new PlayerLinkAsyncTask().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }else {
                showError();
            }

            for(Link link : mActors) {
                Log.d(TAG, "Title: " + link.Title);
            }
        }
    }

    class PlayerLinkAsyncTask extends AsyncTask<URL, Void, String> {

        private String parseJsLink(String line) {
            int begin = line.indexOf("src=");
            int end = line.indexOf("\"", begin+6);
            if(begin != -1 && end != -1) {
                return line.substring(begin+5, end);
            }
            return null;
        }

        @Override
        protected String doInBackground(URL... urls) {
            try {
                URL url = urls[0];
                HttpURLConnection connection = null;
                BufferedReader in = null;
                try {
                    connection = (HttpURLConnection)url.openConnection();
                    InputStream stream = connection.getInputStream();
                    in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
                    String line;
                    while((line = in.readLine()) != null){
                        if(line.contains("js.php")) {
                            return "http:" + parseJsLink(line);
                        }
                    }
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
        protected void onPostExecute(String jsLink) {
            Log.d(TAG, "Js: " + jsLink);
            showData();
        }
    }
}
