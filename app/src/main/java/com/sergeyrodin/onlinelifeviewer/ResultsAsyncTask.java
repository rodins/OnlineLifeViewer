package com.sergeyrodin.onlinelifeviewer;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by root on 10.05.16.
 */
class ResultsAsyncTask extends AsyncTask<String, Void, String> {
    private ListActivity activity;
    private String tag;

    ResultsAsyncTask(ListActivity activity, String tag) {
        this.activity = activity;
        this.tag = tag;
    }
    protected String doInBackground(String... params) {
        try {
            //TODO: get and parse only useful html page part
            return new Curl().getPageString(params[0]);
        }catch(IOException e){
            System.err.println(e.toString());
            return null;
        }
    }

    protected void onPostExecute(String page) {
        if(activity instanceof MainActivity) {
            ((MainActivity) activity).setPage(page);
        }

        // Why am I doing this? Why am I saving something here?
        FragmentManager fm = activity.getFragmentManager();
        RetainedFragment saveResults = (RetainedFragment)fm.findFragmentByTag(tag);
        if(saveResults == null) {
            saveResults = new RetainedFragment();
            //TODO: fix exception here
            if(!activity.isFinishing()) {
                fm.beginTransaction().add(saveResults, tag).commit();
            }
        }

        if(page == null) {
            ProgressBar progressBar = (ProgressBar)activity.getListView().getEmptyView();
            if(progressBar != null) {
                progressBar.setVisibility(View.INVISIBLE);
            }
            saveResults.setData(new ArrayList<Result>());//save empty list to RetainedFragment
            Toast.makeText(activity, R.string.network_problem, Toast.LENGTH_SHORT).show();
            return;
        }

        ResultsParser parser = new ResultsParser(page);
        ArrayList<Result> results =  parser.getItems();

        if(results.isEmpty()) {
            ProgressBar progressBar = (ProgressBar)activity.getListView().getEmptyView();
            if(progressBar != null) {
                progressBar.setVisibility(View.INVISIBLE);
            }
            saveResults.setData(results);
            Toast.makeText(activity, R.string.nothing_found, Toast.LENGTH_SHORT).show();
            return;
        }

        //Updating current ListView
        //Saving data
        saveResults.setData(results);
        activity.setListAdapter(new ResultsAdapter(activity, results));
        if(activity instanceof ResultsActivity) {
            parser.navigationInfo();
            ((ResultsActivity) activity)
                    .setupPagerFromAsyncTask(parser.getPrevLink(),
                            parser.getNextLink(), parser.getPageNumber(),
                            parser.getPrevPage(), parser.getNextPage());
        }
    }
}