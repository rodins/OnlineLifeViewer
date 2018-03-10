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
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsActivity extends AppCompatActivity implements ResultsAdapter.ListItemClickListener {
    private final String STATE_NEXTLINK = "com.sergeyrodin.NEXTLINK";
    private final String STATE_CURRENTLINK = "com.sergeyrodin.CURRENTLINK";
    private final String STATE_IS_ON_POST_EXECUTE = "com.sergeyrodin.IS_ON_POST_EXECUTE";
    private final String STATE_TITLE = "com.sergeyrodin.TITLE";
    private final String TAG = ResultsActivity.class.getSimpleName();
    private final int RESULT_WIDTH = 190;

    private URL nextLink, currentLink;
    private ResultsRetainedFragment mSaveResults;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageTextView;
    private RecyclerView mResultsView;
    private List<Result> mResults;
    private boolean mIsOnPostExecute = false;
    private boolean mIsPage = false;
    private String mTitle;
    private int mNewWidthDp;

    // Memory cache
    private LruCache<String, Bitmap> mMemoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mResultsView = (RecyclerView)findViewById(R.id.rv_results);

        Configuration configuration = getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp;
        int spanCount = screenWidthDp/RESULT_WIDTH;

        mNewWidthDp = screenWidthDp/spanCount;

        //LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        mResultsView.setLayoutManager(layoutManager);

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

        mLoadingIndicator = (ProgressBar)findViewById(R.id.results_loading_indicator);
        mErrorMessageTextView = (TextView)findViewById(R.id.results_loading_error);

        //TODO: fix intent logic
        Intent intent = getIntent();
        mTitle = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        if(mTitle == null) {
            mTitle = getString(R.string.results);
        }

        if(savedInstanceState != null) {
            try {
                String strNextLink = savedInstanceState.getString(STATE_NEXTLINK);
                String strCurrentLink = savedInstanceState.getString(STATE_CURRENTLINK);
                mIsOnPostExecute = savedInstanceState.getBoolean(STATE_IS_ON_POST_EXECUTE);
                mTitle = savedInstanceState.getString(STATE_TITLE);
                if(strNextLink != null) {
                    nextLink = new URL(strNextLink);
                }
                if(strCurrentLink != null) {
                    currentLink = new URL(strCurrentLink);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        setTitle(mTitle);

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;

        mSaveResults = ResultsRetainedFragment.findOrCreateRetainedFragment(getFragmentManager());
        mMemoryCache = mSaveResults.mRetainedCache;
        if(mMemoryCache == null) {
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
            mSaveResults.mRetainedCache = mMemoryCache;
        }

        mResults = mSaveResults.mRetainedData;
        if(mResults == null) {
            // No saved data, find new info
            String link = intent.getStringExtra(MainActivity.EXTRA_LINK);
            if(link != null) {
                // Getting results from site, putting them to ListView and save them
                refresh(link);
            }else if(Intent.ACTION_SEARCH.equals(intent.getAction())) { //Called by SearchView
                String query = getIntent().getStringExtra(SearchManager.QUERY);
                refresh(NetworkUtils.buildSearchUrl(query));
            }else if(currentLink != null) {
                refresh(currentLink);
            }
        }else {
            mResultsView.setAdapter(new ResultsAdapter(mResults, this, this, mNewWidthDp));
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private URL getSearchLink(int page) {
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        return NetworkUtils.buildSearchUrl(query, page);
    }

    @Override
    public void onListItemClick(int position) {
        Result result = mResults.get(position);
        Intent intent = new Intent(this, ActorsActivity.class);
        intent.putExtra(MainActivity.EXTRA_TITLE, result.title);
        intent.putExtra(MainActivity.EXTRA_LINK, result.link);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(nextLink != null) {
            String strNextLink = nextLink.toString();
            outState.putString(STATE_NEXTLINK, strNextLink);
        }
        if(currentLink != null) {
            outState.putString(STATE_CURRENTLINK, currentLink.toString());
        }
        outState.putBoolean(STATE_IS_ON_POST_EXECUTE, mIsOnPostExecute);
        outState.putString(STATE_TITLE, mTitle);

        super.onSaveInstanceState(outState);
    }

    private void refresh(String link) {
        try {
            currentLink = new URL(link);
            URL url = new URL(link);
            new ResultsAsyncTask().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void refresh(URL url) {
        currentLink = url;
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

    private void parseNavigation(String nav) {
        nextLink = null;
        String nl = null;
        int nextPage = 0;

        Matcher m;
        // non-search page navigation links
        m = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(nav);
        while(m.find()) {
            if(m.group(2).length() == 6) {
                nl = m.group(1);
                try {
                    if(nl != null) {
                        nextLink = new URL(nl);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        // search page navigation links
        m = Pattern.compile("<a.+?onclick=\".+?(\\d+).+?\">(.+?)</a>").matcher(nav);
        while(m.find()) {
            if(m.group(2).length() == 6) {
                nextPage = Integer.parseInt(m.group(1));
                if(nextPage != 0) {
                    nextLink = getSearchLink(nextPage); //forming next search link
                }
                break;
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
                        mNewWidthDp);
                mResultsView.setAdapter(adapter);
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
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:43.0) Gecko/20100101 Firefox/43.0 SeaMonkey/2.40");
                    InputStream stream = connection.getInputStream();
                    in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
                    String line;
                    String div = "";
                    boolean div_found = false;
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

                        if(line.contains("class=\"navigation\"")) {
                            return line;
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

}
