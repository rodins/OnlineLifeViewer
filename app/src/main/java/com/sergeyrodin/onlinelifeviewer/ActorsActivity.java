package com.sergeyrodin.onlinelifeviewer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.*;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActorsActivity extends AppCompatActivity implements ActorsAdapter.ListItemClickListener {
    private final static String TAG = ActorsActivity.class.getSimpleName();
    private final String SAVE_JS = "com.sergeyrodin.JS";
    private final String SAVE_TITLE = "com.sergeyrodin.TITLE";
    private RecyclerView mRvActors;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorTextView;

    private List<Link> mActors = new ArrayList<>();
    private String mJs;
    private MenuItem mActionOpen;
    private LinkRetainedFragment mSaveActors;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actors);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvActors = (RecyclerView)findViewById(R.id.rv_actors);
        mRvActors.setLayoutManager(layoutManager);
        mLoadingIndicator = (ProgressBar)findViewById(R.id.actors_loading_indicator);
        mErrorTextView = (TextView)findViewById(R.id.actors_loading_error);

        mSaveActors = LinkRetainedFragment.findOrCreateRetainedFragment(getFragmentManager());

        if(savedInstanceState != null) {
            mJs = savedInstanceState.getString(SAVE_JS);
            mTitle = savedInstanceState.getString(SAVE_TITLE);
        }

        if(mSaveActors.Data != null && mSaveActors.Data.size() != 0) {
            mActors = mSaveActors.Data;
            mRvActors.setAdapter(new ActorsAdapter(mActors, this));
        }else {
            Intent intent = getIntent();
            if(intent != null) {
                mTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE);
                String link = intent.getStringExtra(MainActivity.EXTRA_LINK);
                try {
                    URL url = new URL(link);
                    new ActorsAsyncTask().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        if(mTitle != null) {
            setTitle(mTitle);
        }else {
            setTitle(R.string.actors_title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actors_menu, menu);
        mActionOpen = menu.findItem(R.id.action_open);
        if(mJs != null) {
            mActionOpen.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_open) {

            if(mJs != null) {
                PlaylistItem psItem = new PlaylistItemParser().getItem(mJs);
                if(psItem.getComment() != null) {
                    // Trailer title
                    if(psItem.getComment().length() == 1) {
                        psItem.setComment(mTitle);
                    }
                    //Start process item dialog: select play or download item
                    ProcessPlaylistItem.process(this, psItem);
                }else {
                    // Process activity_playlists in PlaylistsActivity
                    Intent intent = new Intent(this, PlaylistsActivity.class);
                    intent.putExtra(MainActivity.EXTRA_JS, mJs);
                    startActivity(intent);
                }
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onListItemClick(int index) {
        Link link = mActors.get(index);
        startResultsActivity(link.Title, link.Href);
    }

    private void startResultsActivity(String title, String link) {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(MainActivity.EXTRA_TITLE, title);
        intent.putExtra(MainActivity.EXTRA_LINK, link);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mJs != null) {
            outState.putString(SAVE_JS, mJs);
        }
        if(mTitle != null) {
            outState.putString(SAVE_TITLE, mTitle);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        mSaveActors.Data = mActors;
        super.onPause();
    }

    class ActorsAsyncTask extends AsyncTask<URL, Link, Map<String, String>> {
        private ActorsAdapter mAdapter;

        @Override
        protected void onPreExecute() {
            showLoadingIndicator();
            mAdapter = new ActorsAdapter(mActors, ActorsActivity.this);
            mRvActors.setAdapter(mAdapter);
        }

        void parseAnchors(String line, boolean isDirector) {
            Matcher m = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(line);
            while(m.find()) {
                String title = m.group(2) + " " + (isDirector?"(" + getString(R.string.director) + ")":"");
                publishProgress(new Link(android.text.Html.fromHtml(title).toString(), m.group(1)));
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

        String parseYear(String line) {
            int yearBegin = line.indexOf("\">");
            int yearEnd = line.indexOf("<", yearBegin);
            if(yearBegin != -1 && yearEnd != -1) {
                return line.substring(yearBegin+2, yearEnd);
            }
            return null;
        }

        @Override
        protected Map<String, String> doInBackground(URL... urls) {
            try {
                Map<String, String> result = new HashMap<>();
                URL url = urls[0];
                HttpURLConnection connection = null;
                BufferedReader in = null;
                boolean spanFound = false;
                boolean isDirector = false;
                boolean infoDataFound = false;
                boolean countryFound = false;
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

                        if(line.contains("info_data")) {

                            if(line.contains("</span>")) {
                                if(line.contains("<li>")) {
                                    String year = parseYear(line);
                                    result.put("year", year);
                                }
                            }else {
                                infoDataFound = true;
                            }
                            continue;
                        }

                        if(line.contains("</span>") && infoDataFound) {
                            infoDataFound = false;
                        }

                        if(infoDataFound) {
                            if(!line.contains("<")) {
                                if(!countryFound) {
                                    result.put("country", line.trim());
                                    countryFound = true;
                                }
                            }
                        }

                        if(line.contains("<iframe")) {
                            result.put("playerLink", parseIframe(line));
                            return result;
                        }
                    }
                    return result;
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
            mAdapter.notifyItemInserted(mActors.size()-1);
            if(mActors.size() == 1) {
                showData();
            }
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            if(result != null) {
                mTitle += (" - " + result.get("country") + " - " + result.get("year"));
                setTitle(mTitle);
                try {
                    URL url = new URL(result.get("playerLink"));
                    new PlayerLinkAsyncTask().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }else {
                showError();
            }
        }
    }

    class PlayerLinkAsyncTask extends AsyncTask<URL, Void, String> {

        private URL referer;

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
                referer = url;
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
            if(jsLink != null) {
                try {
                    URL url = new URL(jsLink);
                    new JsAsyncTask().execute(url, referer);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }else {
                showError();
            }
        }
    }

    class JsAsyncTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            try {
                return NetworkUtils.getResponseFromHttpUrl(urls[0], urls[1].toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String js) {
            if(js != null) {
                mJs = js;
                mActionOpen.setVisible(true);
            }else {
                showError();
            }
        }
    }
}
