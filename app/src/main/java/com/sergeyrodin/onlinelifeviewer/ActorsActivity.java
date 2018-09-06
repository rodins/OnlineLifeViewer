package com.sergeyrodin.onlinelifeviewer;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.utilities.Html;
import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActorsActivity extends AppCompatActivity implements ActorsAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<ActorsActivity.ActorsResult>{
    private final static String TAG = ActorsActivity.class.getSimpleName();
    private final static String ACTORS_URL_EXTRA = "actors";

    private RecyclerView mRvActors;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorTextView;

    private List<Link> mActors = new ArrayList<>();
    private String mJs;
    private String mTitle, mResultTitle, mResultLink;
    private boolean mIsOnLoadFinishedCalled = false;
    private FloatingActionButton mFabButtonLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actors);

        // Add back button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvActors = findViewById(R.id.rv_actors);
        mRvActors.setLayoutManager(layoutManager);
        mLoadingIndicator = findViewById(R.id.actors_loading_indicator);
        mErrorTextView = findViewById(R.id.actors_loading_error);

        Intent intent = getIntent();
        if(intent.hasExtra(MainActivity.EXTRA_TITLE)) {
            mResultTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE);
            setTitle(mResultTitle);
        }else {
            setTitle(R.string.actors_title);
        }

        if(intent.hasExtra(MainActivity.EXTRA_LINK)) {
            mResultLink = intent.getStringExtra(MainActivity.EXTRA_LINK);
            Bundle actorsBundle = new Bundle();
            actorsBundle.putString(ACTORS_URL_EXTRA, mResultLink);
            showLoadingIndicator();
            LoaderManager loaderManager = getSupportLoaderManager();
            int ACTORS_LOADER = 23;
            loaderManager.initLoader(ACTORS_LOADER, actorsBundle, this);
        }

        mFabButtonLinks = findViewById(R.id.fab_links);

        mFabButtonLinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionOpenClicked();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actors_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void actionOpenClicked() {
        if(mJs != null) {
            VideoItem psItem = new VideoItemParser().getItem(mJs);
            if(psItem.getComment() != null) {
                // Trailer title
                if(psItem.getComment().trim().isEmpty()) {
                    psItem.setComment(mTitle);
                }
                //Start process item dialog: select play or download item
                ProcessVideoItem.process(this, psItem);
            }else {
                // Process seasons in LinksActivity
                Intent intent = new Intent(this, LinksActivity.class);
                intent.putExtra(MainActivity.EXTRA_JS, mJs);
                startActivity(intent);
            }
        }else { // Start LinksActivity in constant links mode
            Intent intent = new Intent(this, LinksActivity.class);
            intent.putExtra(MainActivity.EXTRA_TITLE, mResultTitle);
            intent.putExtra(MainActivity.EXTRA_LINK, mResultLink);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == android.R.id.home) {
            onBackPressed();
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
        mErrorTextView.setText(R.string.network_problem);
    }

    private void showEmpty() {
        mRvActors.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.setText(R.string.no_actors);
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

    @NonNull
    @Override
    public Loader<ActorsActivity.ActorsResult> onCreateLoader(int id, @Nullable Bundle args) {
        return new ActorsAsyncTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ActorsActivity.ActorsResult> loader, ActorsActivity.ActorsResult data) {
        if(!mIsOnLoadFinishedCalled) {
            mIsOnLoadFinishedCalled = true;
            if(data == null) {
                showError();
            }else {
                if(data.actors.isEmpty()) {
                    showEmpty();
                }else {
                    for(Actor actor: data.actors) {
                        String title = actor.title + " " + (actor.isDirector?"(" + getString(R.string.director) + ")":"");
                        mActors.add(new Link(title, actor.href));
                    }
                    ActorsAdapter adapter = new ActorsAdapter(mActors, this);
                    mRvActors.setAdapter(adapter);
                    showData();
                }

                if(data.country != null && data.year != null) {
                    mTitle = mResultTitle + " - " + data.country + " - " + data.year;
                    setTitle(mTitle);
                }

                if(data.js != null) {
                    mJs = data.js;
                }

                mFabButtonLinks.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ActorsActivity.ActorsResult> loader) {

    }

    static class ActorsAsyncTaskLoader extends AsyncTaskLoader<ActorsResult> {
        private Bundle args;

        ActorsAsyncTaskLoader(Context context, Bundle args) {
            super(context);
            this.args = args;
        }

        @Override
        protected void onStartLoading() {
            if(args == null) {
                return;
            }
            forceLoad();
        }

        private List<Actor> parseAnchors(String line, boolean isDirector) {
            List<Actor> actors = new ArrayList<>();
            Matcher m = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(line);
            while(m.find()) {
                actors.add(new Actor(Html.unescape(m.group(2)),
                                     isDirector,
                                     m.group(1)));
            }
            return actors;
        }

        private String parseIframe(String line) {
            int linkBegin = line.indexOf("src=");
            int linkEnd = line.indexOf("'", linkBegin+6);
            if(linkBegin != -1 && linkEnd != -1) {
                return line.substring(linkBegin+5, linkEnd);
            }
            return null;
        }

        private String parseYear(String line) {
            int yearBegin = line.indexOf("\">");
            int yearEnd = line.indexOf("<", yearBegin);
            if(yearBegin != -1 && yearEnd != -1) {
                return line.substring(yearBegin+2, yearEnd);
            }
            return null;
        }

        private String parseJsLink(String line) {
            int begin = line.indexOf("src=");
            int end = line.indexOf("\"", begin+6);
            if(begin != -1 && end != -1) {
                return line.substring(begin+5, end);
            }
            return null;
        }

        private String loadJsLink(String playerLink) {
            try {
                URL url = new URL(playerLink);
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

        private String loadJs(String jsLink, String referer) {
            try {
                URL url = new URL(jsLink);
                return NetworkUtils.getResponseFromHttpUrl(url, referer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public ActorsActivity.ActorsResult loadInBackground() {
            try {
                String actorsUrl = args.getString(ACTORS_URL_EXTRA);
                ActorsActivity.ActorsResult result = new ActorsActivity.ActorsResult();
                URL url = new URL(actorsUrl);
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
                            result.actors.addAll(parseAnchors(line, isDirector));
                        }
                        if(line.contains("</span>") && spanFound) {
                            spanFound = false;
                            isDirector = false;
                        }

                        if(line.contains("info_data")) {

                            if(line.contains("</span>")) {
                                if(line.contains("<li>")) {
                                    result.year = parseYear(line);
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
                                    result.country = line.trim();
                                    countryFound = true;
                                }
                            }
                        }

                        if(line.contains("<iframe")) {
                            String playerLink = parseIframe(line);
                            if(playerLink != null) {
                                String jsLink = loadJsLink(playerLink);
                                if(jsLink != null) {
                                    result.js = loadJs(jsLink, playerLink);
                                }
                            }
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
    }

    static class Actor {
        String title;
        boolean isDirector;
        String href;
        Actor(String title, boolean isDirector, String href) {
            this.title = title;
            this.isDirector = isDirector;
            this.href = href;
        }
    }

    static class ActorsResult {
        String country;
        String year;
        String js;
        List<Actor> actors = new ArrayList<>();
    }
}
