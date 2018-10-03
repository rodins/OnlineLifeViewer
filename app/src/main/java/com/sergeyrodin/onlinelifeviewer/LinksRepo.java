package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.text.TextUtils;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

class LinksRepo {
    private String link, js;
    private MutableLiveData<LinkData> linkData;

    LinksRepo(String link, String js) {
        this.link = link;
        this.js = js;
        linkData = new MutableLiveData<>();
    }

    LiveData<LinkData> getLinkData() {
        getLinksFromNet();
        return linkData;
    }

    private void getLinksFromNet() {
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                LinkData data = new LinkData();
                URL url;
                try{
                    if(link != null) {
                        url = new URL(link);
                        js = NetworkUtils.getConstantLinksJs(url); // js from constant links
                    }
                    if(js != null && !TextUtils.isEmpty(js.trim())) {
                        VideoItem videoItem = new VideoItemParser().getItem(js);
                        if(videoItem.getComment() != null) { // Film found
                            data.setVideoItem(videoItem);
                        }else {
                            String seasonsJson = new SeasonsJsonParser().getSeasonsJson(js);
                            if(seasonsJson != null) {
                                List<Season> seasons = new SeasonsParser().getItems(seasonsJson);
                                if (seasons.size() == 0) { // episodes parsed
                                    Season season = new EpisodesParser().getItem(seasonsJson);
                                    data.setSeason(season);
                                } else { // seasons parsed
                                    data.setSeasons(seasons);
                                }
                            }
                        }
                    }else {
                        data.setError();
                    }
                }catch (IOException e) {
                    data.setError();
                }
                linkData.postValue(data);
            }
        });
    }
}
