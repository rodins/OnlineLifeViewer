package com.sergeyrodin.onlinelifeviewer;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sergeyrodin.onlinelifeviewer.utilities.Html;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsActivity extends AppCompatActivity implements ResultsAdapter.ListItemClickListener {
    private final String STATE_NEXTLINK = "com.sergeyrodin.NEXTLINK";
    private final String STATE_IS_ON_POST_EXECUTE = "com.sergeyrodin.IS_ON_POST_EXECUTE";
    private final String STATE_TITLE = "com.sergeyrodin.TITLE";
    private final String TAG = ResultsActivity.class.getSimpleName();

    private URL nextLink;
    private ResultsRetainedFragment mSaveResults;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageTextView;
    private RecyclerView mResultsView;
    private List<Result> mResults;
    private boolean mIsOnPostExecute = false;
    private boolean mIsPage = false;
    private String mTitle;
    private int mSpanCount;
    private Set<String> mNextLinks = new HashSet<>();

    private MenuItem mActorsMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mResultsView = findViewById(R.id.rv_results);

        Configuration configuration = getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp;
        int RESULT_WIDTH = 190;
        mSpanCount = screenWidthDp/ RESULT_WIDTH;

        if(mSpanCount <= 2) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mResultsView.setLayoutManager(layoutManager);
        }else {
            GridLayoutManager layoutManager = new GridLayoutManager(this, mSpanCount);
            mResultsView.setLayoutManager(layoutManager);
        }

        mResultsView.setHasFixedSize(false);

        mResultsView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(!recyclerView.canScrollVertically(1)){
                    // React to scrolling only when list is totally loaded
                    if(mIsOnPostExecute) {
                        mIsPage = true;
                        if(nextLink != null) {
                            refresh(nextLink);
                        }
                    }
                }
            }
        });

        mLoadingIndicator = findViewById(R.id.results_loading_indicator);
        mErrorMessageTextView = findViewById(R.id.results_loading_error);

        mTitle = getString(R.string.results);

        if(savedInstanceState != null) {
            try {
                String strNextLink = savedInstanceState.getString(STATE_NEXTLINK);
                mIsOnPostExecute = savedInstanceState.getBoolean(STATE_IS_ON_POST_EXECUTE);
                mTitle = savedInstanceState.getString(STATE_TITLE);
                if(strNextLink != null) {
                    nextLink = new URL(strNextLink);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        mSaveResults = ResultsRetainedFragment.findOrCreateRetainedFragment(getFragmentManager());

        mResults = mSaveResults.mRetainedData;
        if(mResults == null) {
            Intent intent = getIntent();
            if(intent.hasExtra(MainActivity.EXTRA_TITLE)) {
                mTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE);
            }
            // No saved data, find new info
            if(intent.hasExtra(MainActivity.EXTRA_LINK)) {
                String link = intent.getStringExtra(MainActivity.EXTRA_LINK);
                refresh(link);
            }else if(Intent.ACTION_SEARCH.equals(intent.getAction())) { //Called by SearchView
                String query = getIntent().getStringExtra(SearchManager.QUERY);
                mTitle = query;
                refresh(NetworkUtils.buildSearchUrl(query));
            }
        }else {
            mResultsView.setAdapter(new ResultsAdapter(mResults, this, this, mSpanCount));
        }

        setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.results_menu, menu);

        mActorsMenuItem = menu.findItem(R.id.action_actors);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_actors) {
            mActorsMenuItem.setChecked(!mActorsMenuItem.isChecked());
        }
        return super.onOptionsItemSelected(item);
    }

    private URL getSearchLink(int page) {
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        return NetworkUtils.buildSearchUrl(query, page);
    }

    @Override
    public void onListItemClick(int position) {
        Result result = mResults.get(position);
        if(mActorsMenuItem.isChecked()) { // Use actors links
            Intent intent = new Intent(this, ActorsActivity.class);
            intent.putExtra(MainActivity.EXTRA_TITLE, result.title);
            intent.putExtra(MainActivity.EXTRA_LINK, result.link);
            startActivity(intent);
        }else { // Use constant links
            try {
                URL url = new URL(result.link);
                new JsAsyncTask().execute(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(nextLink != null) {
            String strNextLink = nextLink.toString();
            outState.putString(STATE_NEXTLINK, strNextLink);
        }
        outState.putBoolean(STATE_IS_ON_POST_EXECUTE, mIsOnPostExecute);
        outState.putString(STATE_TITLE, mTitle);

        super.onSaveInstanceState(outState);
    }

    private void refresh(String link) {
        try {
            URL url = new URL(link);
            new ResultsAsyncTask().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void refresh(URL url) {
        new ResultsAsyncTask().execute(url);
    }

    private void moveUpResultsViewBottom() {
        // Increase bottom padding to show spinner
        //TODO: use int padding = getResources().getDimensionPixelOffset(R.dimen.padding);
        int paddingDp = 50;
        float density = this.getResources().getDisplayMetrics().density;
        int paddingPixel = (int)(paddingDp * density);
        mResultsView.setPadding(0, 0, 0, paddingPixel);
    }

    private void moveLoadingIndicatorToBottom() {
        // Place spinner to the bottom
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
        mLoadingIndicator.setLayoutParams(params);
    }

    private void moveErrorMessageToBottom() {
        // Place textView to the bottom
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
        mErrorMessageTextView.setLayoutParams(params);
    }

    private void showErrorMessage(int id){
        mErrorMessageTextView.setText(id);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if(mIsPage) {
            mResultsView.setVisibility(View.VISIBLE);
            moveUpResultsViewBottom();
            moveErrorMessageToBottom();
        }else {
            mResultsView.setVisibility(View.INVISIBLE);
        }
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    private void showLoadingIndicator() {
        if(mIsPage) {
            mResultsView.setVisibility(View.VISIBLE);
            moveUpResultsViewBottom();
            moveLoadingIndicatorToBottom();
        }else {
            mResultsView.setVisibility(View.INVISIBLE);
        }
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void showData() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mResultsView.setPadding(0, 0, 0, 0);
        mResultsView.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private Result divToResult(String div) {
        //TODO: regexp should be domain independent
        Matcher m = Pattern
                .compile("<a\\s+href=\"(http://www.online-life.club/\\d+?-.*?html)\"\\s*?>\\n\\s*<img\\s+src=\"(.*?)\"\\s+/>(.+?)\\n?\\s*</a>")
                .matcher(div);
        if(m.find()) {
            String link = m.group(1);
            String image = m.group(2);
            image = image.substring(0, image.indexOf("&"));
            String title = Html.unescape(m.group(3));
            return new Result(title, image, link);
        }
        return null;
    }

    private Result divToMobileResult(String div) {
        Matcher m = Pattern
                .compile("<a\\s+href=\"(.*?)\".*?src=\"(.*?)\".*?\">(.+?)</span>")
                .matcher(div);
        if(m.find()) {
            String link = m.group(1);
            String image = m.group(2);
            image = image.substring(0, image.indexOf("&"));
            String title = Html.unescape(m.group(3));
            return new Result(title, image, link);
        }
        return null;
    }

    private void parseNavigation(String nav) {
        nextLink = null;
        String nl = "";
        int nextPage = 0;

        Matcher m;
        // non-search page navigation links
        m = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(nav);
        while(m.find()) {
            nl = m.group(1);// iterating to get last element
        }

        try {
            if(!nl.isEmpty()) {
                if(!mNextLinks.contains(nl)) {
                    mNextLinks.add(nl);
                    nextLink = new URL(nl);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // search page navigation links
        m = Pattern.compile("<a.+?onclick=\".+?(\\d+).+?\">(.+?)</a>").matcher(nav);
        while(m.find()) {
            nl = m.group(1);
        }

        if(!nl.isEmpty()) {
            if(!mNextLinks.contains(nl)) {
                mNextLinks.add(nl);
                nextPage = Integer.parseInt(nl);
                nextLink = getSearchLink(nextPage); //forming next search link
            }
        }
    }

    public class ResultsAsyncTask extends AsyncTask<URL, Result, String> {
        private ResultsAdapter adapter;

        @Override
        protected void onPreExecute() {
            showLoadingIndicator();

            mIsOnPostExecute = false;

            if(!mIsPage) {
                mResults = new ArrayList<>();
                adapter = new ResultsAdapter(mResults,
                        ResultsActivity.this,
                        ResultsActivity.this,
                         mSpanCount);
                mResultsView.setAdapter(adapter);
                mNextLinks.clear();
            }else {
                adapter = (ResultsAdapter)mResultsView.getAdapter();
            }
        }

        protected String doInBackground(URL... params) {
            try {
                URL url = params[0];
                HttpURLConnection connection = null;
                BufferedReader in = null;
                try {
                    connection = (HttpURLConnection)url.openConnection();
                    InputStream stream = connection.getInputStream();
                    in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
                    String line;
                    String div = "";
                    boolean div_found = false;
                    boolean div_mobile_found = false;
                    boolean div_nav_found = false;
                    while((line = in.readLine()) != null){
                        if(line.contains("class=\"custom-poster\"") && !div_found) {
                            div_found = true;
                        }
                        if(line.contains("</a>") && div_found) {
                            div_found = false;
                            div += line;
                            Result result = divToResult(div);
                            if(result != null) {
                                publishProgress(result);
                            }
                            div = "";
                        }
                        if(div_found) {
                            div += line + "\n";
                        }

                        if(line.contains("class=\"slider-item\"")) {
                            div_mobile_found = true;
                            div = "";
                            continue;
                        }

                        if(line.contains("</a>") && div_mobile_found) {
                            div_mobile_found = false;
                            Result result = divToMobileResult(div);
                            if(result != null) {
                                publishProgress(result);
                            }
                        }

                        if(div_mobile_found) {
                            if(!line.contains("<div") && !line.contains("</div>")) {
                                div += line;
                            }
                        }

                        if(line.contains("class=\"navigation\"")) {
                            div_nav_found = true;
                            div = "";
                            continue;
                        }

                        if(line.contains("</div>") && div_nav_found) {
                            div += line.trim();
                            return div;
                        }

                        if(div_nav_found) {
                            div += line.trim();
                        }
                    }
                    return "";
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
        protected void onProgressUpdate(Result... values) {
            showData();
            Result result = values[0];
            mResults.add(result);
            int index = mResults.indexOf(result);
            adapter.notifyItemInserted(index);
        }

        protected void onPostExecute(String navigation) {
            if(navigation == null) {
                showErrorMessage(R.string.network_problem);
                mSaveResults.mRetainedData = null; //save null to ResultsRetainedFragment to erase prev results
                return;
            }

            if(mResults.isEmpty()) {
                showErrorMessage(R.string.nothing_found);
                mSaveResults.mRetainedData = null;
                return;
            }else {
                mSaveResults.mRetainedData = mResults;
            }

            mIsOnPostExecute = true;

            if(!navigation.isEmpty()) {
                parseNavigation(navigation);
            }
        }
    }

    class JsAsyncTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            try {
                return NetworkUtils.getConstantLinksJs(urls[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String js) {
            if(js != null) {
                PlaylistItem psItem = new PlaylistItemParser().getItem(js);
                if(psItem.getComment() != null) {
                    // Trailer title
                    if(psItem.getComment().length() == 1) {
                        Toast.makeText(ResultsActivity.this,
                                       "Using with trailers is not recommended",
                                        Toast.LENGTH_SHORT).show();
                    }
                    //Start process item dialog: select play or download item
                    ProcessPlaylistItem.process(ResultsActivity.this, psItem);
                }else {
                    // Process activity_playlists in PlaylistsActivity
                    Intent intent = new Intent(ResultsActivity.this, PlaylistsActivity.class);
                    intent.putExtra(MainActivity.EXTRA_JS, js);
                    startActivity(intent);
                }
            }else {
                Toast.makeText(ResultsActivity.this, R.string.network_problem, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
