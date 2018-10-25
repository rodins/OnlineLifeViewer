package com.sergeyrodin.onlinelifeviewer;

import android.arch.core.util.Function;
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
    private Function<Void, Void> retryFunction;

    public ResultsDataSource(String startLink) {
        this.startLink = startLink;
        parser = new ResultsParser();
        parser.setStartLink(startLink);
        state = new MutableLiveData<>();
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
    public void loadInitial(@NonNull final LoadInitialParams<String> params, @NonNull final LoadInitialCallback<String, Result> callback) {
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    updateState(State.LOADING_INIT);
                    getDataFromNet(startLink);
                    updateState(State.DONE);
                    List<Result> data = parser.getData();
                    String nextLink = parser.getNextLink();
                    callback.onResult(data, null, nextLink);
                } catch (IOException e) {
                    e.printStackTrace();
                    updateState(State.ERROR_INIT);
                    retryFunction = new Function<Void, Void>() {
                        @Override
                        public Void apply(Void input) {
                            loadInitial(params, callback);
                            return null;
                        }
                    };
                }
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Result> callback) {

    }

    @Override
    public void loadAfter(@NonNull final LoadParams<String> params, @NonNull final LoadCallback<String, Result> callback) {
        try {
            updateState(State.LOADING_AFTER);
            getDataFromNet(params.key);
            updateState(State.DONE);
            List<Result> data = parser.getData();
            String nextLink = parser.getNextLink();
            callback.onResult(data, nextLink);
        }catch(IOException e) {
            e.printStackTrace();
            updateState(State.ERROR_AFTER);
            retryFunction = new Function<Void, Void>() {

                @Override
                public Void apply(Void input) {
                    AppExecutors.getInstance().networkIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            loadAfter(params, callback);
                        }
                    });
                    return null;
                }
            };
        }
    }

    public void retry() {
        if(state != null) {
            State stateValue = state.getValue();
            if(stateValue != null) {
                switch (stateValue) {
                    case ERROR_INIT:
                    case ERROR_AFTER:
                        retryFunction.apply(null);
                }
            }
        }
    }

    private void updateState(State state) {
        this.state.postValue(state);
    }
}
