package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.PageKeyedDataSource;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class ResultsDataSource extends PageKeyedDataSource<String, Result> {
    private String startLink;
    private ResultsParser parser;
    MutableLiveData<State> state;

    public ResultsDataSource(String startLink) {
        this.startLink = startLink;
        parser = new ResultsParser();
        parser.setStartLink(startLink);
        state = new MutableLiveData<>();
        updateState(State.LOADING);
    }

    private void getDataFromNet(String link) throws IOException {
        parser.init();
        URL url = new URL(link);
        HttpURLConnection connection = null;
        BufferedReader in = null;
        try {
            connection = (HttpURLConnection)url.openConnection();
            InputStream stream = connection.getInputStream();
            in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
            parser.parse(in);
        }finally {
            if(in != null) {
                in.close();
            }
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<String, Result> callback) {
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    updateState(State.LOADING);
                    getDataFromNet(startLink);
                    updateState(State.DONE);
                    List<Result> data = parser.getData();
                    String nextLink = parser.getNextLink();
                    callback.onResult(data, null, nextLink);
                } catch (IOException e) {
                    e.printStackTrace();
                    updateState(State.ERROR);
                    invalidate();
                }
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Result> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Result> callback) {
        try {
            updateState(State.LOADING);
            getDataFromNet(params.key);
            updateState(State.DONE);
            List<Result> data = parser.getData();
            String nextLink = parser.getNextLink();
            callback.onResult(data, nextLink);
        }catch(IOException e) {
            e.printStackTrace();
            updateState(State.ERROR);
            invalidate();
        }
    }

    private void updateState(State state) {
        this.state.postValue(state);
    }
}
