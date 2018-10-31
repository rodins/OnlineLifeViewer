package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

class ActorsDataSource {
    private MutableLiveData<ActorsData> actorsData;
    private MutableLiveData<State> state;
    private ActorsParser parser;

    ActorsDataSource() {
        actorsData = new MutableLiveData<>();
        state = new MutableLiveData<>();
        parser = new ActorsParser();
    }

    LiveData<ActorsData> getActorsData(String link) {
        getActorsDataFromNet(link);
        return actorsData;
    }

    LiveData<State> getState() {
        return state;
    }

    void getActorsDataFromNet(final String link) {
        state.setValue(State.LOADING_INIT);
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(link);
                    HttpURLConnection connection = null;
                    BufferedReader in = null;
                    try {
                        connection = (HttpURLConnection) url.openConnection();
                        InputStream stream = connection.getInputStream();
                        in = new BufferedReader(new InputStreamReader(stream, Charset.forName("windows-1251")));
                        ActorsData data = parser.parse(in, link);
                        if (data.getActors().isEmpty()) {
                            state.postValue(State.EMPTY);
                        }else {
                            actorsData.postValue(data);
                            state.postValue(State.DONE);
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
                    state.postValue(State.ERROR_INIT);
                }
            }
        });
    }
}
