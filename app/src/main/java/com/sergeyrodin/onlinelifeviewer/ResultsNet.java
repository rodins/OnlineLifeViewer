package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

class ResultsNet {
    private String nextLink;
    private MutableLiveData<ResultsData> resultsData;
    private ResultsParser parser;
    ResultsNet() {
        resultsData = new MutableLiveData<>();
        parser = new ResultsParser();
    }

    LiveData<ResultsData> getResultsData(String link) {
        getResultsFromNet(link);
        parser.setStartLink(link);
        return resultsData;
    }

    void loadNextPage() {
        getResultsFromNet(nextLink);
    }

    private void getResultsFromNet(final String link) {
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(link);
                    HttpURLConnection connection = null;
                    BufferedReader in = null;
                    try {
                        connection = (HttpURLConnection)url.openConnection();
                        InputStream stream = connection.getInputStream();
                        in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
                        parser.parse(in);
                        ResultsData data = parser.getData();
                        resultsData.postValue(data);
                        nextLink = parser.getNextLink();
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
            }
        });
    }
}
