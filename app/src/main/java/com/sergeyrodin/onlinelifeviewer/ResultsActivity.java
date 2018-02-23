package com.sergeyrodin.onlinelifeviewer;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sergeyrodin.onlinelifeviewer.utilities.CategoriesParser;
import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import org.w3c.dom.Text;

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
    private final String STATE_PREVLINK = "com.sergeyrodin.PREVLINK";
    private final String STATE_NEXTLINK = "com.sergeyrodin.NEXTLINK";
    private final String STATE_CURRENTLINK = "com.sergeyrodin.CURRENTLINK";
    private final String STATE_PAGE = "com.sergeyrodin.PAGE";
    private final String TAG = ResultsActivity.class.getSimpleName();
    private String title;

    private MenuItem prev, next;
    private URL prevLink, nextLink, currentLink;
    private int page = 0;
    private ResultsRetainedFragment mSaveResults;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageTextView;
    private RecyclerView mResultsView;
    private List<Result> mResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mResultsView = (RecyclerView)findViewById(R.id.rv_results);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mResultsView.setLayoutManager(layoutManager);

        mResultsView.setHasFixedSize(false);

        mLoadingIndicator = (ProgressBar)findViewById(R.id.results_loading_indicator);
        mErrorMessageTextView = (TextView)findViewById(R.id.results_loading_error);

        Intent intent = getIntent();
        title = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        if(title == null) {
            title = getString(R.string.results);
        }

        if(savedInstanceState != null) {
            try {
                String strPrevLink = savedInstanceState.getString(STATE_PREVLINK);
                String strNextLink = savedInstanceState.getString(STATE_NEXTLINK);
                String strCurrentLink = savedInstanceState.getString(STATE_CURRENTLINK);
                if(strPrevLink != null) {
                    prevLink = new URL(strPrevLink);
                }
                if(strNextLink != null) {
                    nextLink = new URL(strNextLink);
                }
                if(strCurrentLink != null) {
                    currentLink = new URL(strCurrentLink);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            page = savedInstanceState.getInt(STATE_PAGE);

            if(page > 0) {
                setTitle(title + ": " + page);
            }
        }else {
            setTitle(title);
        }

        FragmentManager fm = getFragmentManager();
        String tag = "saveResultsData";
        mSaveResults = (ResultsRetainedFragment)fm.findFragmentByTag(tag);
        if(mSaveResults == null) { //getting new results list
            mSaveResults = new ResultsRetainedFragment();
            fm.beginTransaction().add(mSaveResults, tag).commit();

            String link = intent.getStringExtra(MainActivity.EXTRA_LINK);
            if(link != null) {
                // Getting results from site, putting them to ListView and save them
                refresh(link);
            }else if(Intent.ACTION_SEARCH.equals(intent.getAction())) { //Called by SearchView
                String query = getIntent().getStringExtra(SearchManager.QUERY);
                refresh(NetworkUtils.buildSearchUrl(query));
            }
        }else { //using saved results list
            mResults = mSaveResults.getData();
            if(mResults != null) {
                mResultsView.setAdapter(new ResultsAdapter(mResults, this));
            }else {
                //if ResultsRetainedFragment is outdated refresh data
                if(currentLink != null) {
                    refresh(currentLink);
                }
            }
        }
    }

    private URL getSearchLink(int page) {
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        return NetworkUtils.buildSearchUrl(query, page);
    }

    @Override
    public void onListItemClick(int position) {
        Result result = mResults.get(position);
        new ItemClickAsyncTask(this).execute(result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.results_menu, menu);

        prev = menu.findItem(R.id.action_prev);
        next = menu.findItem(R.id.action_next);
        if(prevLink == null) {
            prev.setVisible(false);
        }
        if(nextLink == null) {
            next.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void setupPagerFromAsyncTask(String pl, String nl, int prevPage, int nextPage) {
        prevLink = null;
        nextLink = null;
        try {
            if(pl != null) {
                prevLink = new URL(pl);
            }
            if(nl != null) {
                nextLink = new URL(nl);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(page > 0) {
            setTitle(title + ": " + page);
        }
        if(prev != null && prevLink != null) {
            prev.setVisible(true);
        }else {
            if(prevPage == 0) {
                prev.setVisible(false);
            }else { // search page pager
                prev.setVisible(true);
                prevLink = getSearchLink(prevPage); // forming prev search link
            }
        }
        if(next != null && nextLink != null) {
            next.setVisible(true);
        }else {
            if(nextPage == 0) {
                next.setVisible(false);
            }else {
                next.setVisible(true);
                nextLink = getSearchLink(nextPage); //forming next search link
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_prev:
                refresh(prevLink);
                return true;
            case R.id.action_next:
                refresh(nextLink);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(prevLink != null) {
            String strPrevLink = prevLink.toString();
            outState.putString(STATE_PREVLINK, strPrevLink);
        }
        if(nextLink != null) {
            String strNextLink = nextLink.toString();
            outState.putString(STATE_NEXTLINK, strNextLink);
        }
        if(currentLink != null) {
            outState.putString(STATE_CURRENTLINK, currentLink.toString());
        }
        outState.putInt(STATE_PAGE, page);

        super.onSaveInstanceState(outState);
    }

    private void refresh(String link) {
        mResultsView.setAdapter(null);
        try {
            currentLink = new URL(link);
            URL url = new URL(link);
            new ResultsAsyncTask().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void refresh(URL url) {
        mResultsView.setAdapter(null);
        currentLink = url;
        new ResultsAsyncTask().execute(url);
    }

    private void showErrorMessage(int id){
        mErrorMessageTextView.setText(id);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
        mResultsView.setVisibility(View.INVISIBLE);
    }

    private void showLoadingIndicator() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mResultsView.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void showData() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mResultsView.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private Result divToResult(String div) {
        Matcher m = Pattern
                .compile("<a\\s+href=\"http://www.online-life.club/(\\d+?)-.*?html\"\\s*?>\\n\\s*<img\\s+src=\"(.*?)\"\\s+/>(.+?)\\n?\\s*</a>")
                .matcher(div);
        if(m.find()) {
            int id = Integer.parseInt(m.group(1));
            String image = m.group(2);
            image = image.substring(0, image.indexOf("&"));
            String title = Html.unescape(m.group(3));
            return new Result(title, image, id);
        }
        return null;
    }

    private void parseNavigation(String nav) {
        String pl = null, nl = null;
        int prevPage = 0, nextPage = 0;

        Matcher m;
        //find current page
        m = Pattern.compile("<span>(.+?)</span>").matcher(nav);
        while(m.find()) {
            if(m.group(1).length() < 5) {
                page = Integer.parseInt(m.group(1));
            }
        }

        // non-search page navigation links
        m = Pattern.compile("<a\\s+href=\"(.+?)\">(.+?)</a>").matcher(nav);
        while(m.find()) {
            if(m.group(2).length() == 5) {
                pl = m.group(1);
            }

            if(m.group(2).length() == 6) {
                nl = m.group(1);
            }
        }

        // search page navigation links
        m = Pattern.compile("<a.+?onclick=\".+?(\\d+).+?\">(.+?)</a>").matcher(nav);
        while(m.find()) {
            if(m.group(2).length() == 5) {
                prevPage = Integer.parseInt(m.group(1));
            }

            if(m.group(2).length() == 6) {
                nextPage = Integer.parseInt(m.group(1));
            }
        }
        setupPagerFromAsyncTask(pl, nl, prevPage, nextPage);
    }

    @Override
    protected void onDestroy() {
        //Saving data
        if(mSaveResults != null) {
            mSaveResults.setData(mResults);
        }
        super.onDestroy();
    }

    public class ResultsAsyncTask extends AsyncTask<URL, Result, String> {
        private ResultsAdapter adapter;

        @Override
        protected void onPreExecute() {
            showLoadingIndicator();

            mResults = new ArrayList<>();
            adapter = new ResultsAdapter(mResults, ResultsActivity.this);
            mResultsView.setAdapter(adapter);
        }

        protected String doInBackground(URL... params) {
            try {
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
                            //Log.d(TAG, "Line: " + line);
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
                return new Curl().getPageString(params[0]);
            }catch(IOException e){
                System.err.println(e.toString());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Result... values) {
            showData();
            Result result = values[0];
            mResults.add(result);
            adapter.notifyItemInserted(mResults.size()-1);
        }

        protected void onPostExecute(String navigation) {
            if(navigation == null) {
                showErrorMessage(R.string.network_problem);
                mSaveResults.setData(null);//save null to ResultsRetainedFragment to erase prev results
                return;
            }

            if(mResults.isEmpty()) {
                showErrorMessage(R.string.nothing_found);
                mSaveResults.setData(null);
                return;
            }

            if(!navigation.isEmpty()) {
                parseNavigation(navigation);
            }
        }
    }
}
