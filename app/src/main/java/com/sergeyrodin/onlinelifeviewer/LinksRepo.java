package com.sergeyrodin.onlinelifeviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class LinksRepo {
    private String link, js;
    private MutableLiveData<LinkData> linkData;

    LinksRepo(String link, String js) {
        this.link = link;
        this.js = js;
        linkData = new MutableLiveData<>();
    }

    public LiveData<LinkData> getLinkData() {
        getLinksFromNet();
        return linkData;
    }

    private void getLinksFromNet() {
        // TODO: fix trailers error
        linkData.setValue( // Show loading indicator
                new LinkData(true,
                        null,
                        null,
                        null,
                        false));
        AppExecutors.getInstance().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                URL url;
                try{
                    if(link != null) {
                        url = new URL(link);
                        js = NetworkUtils.getConstantLinksJs(url); // js from constant links
                    }
                    if(js != null) {
                        VideoItem videoItem = new VideoItemParser().getItem(js);
                        if(videoItem.getComment() != null) { // Film found
                            // Set film data
                            linkData.postValue(new LinkData(false,
                                    null,
                                    null,
                                    videoItem,
                                    false));
                        }else {
                            String seasonsJson = new SeasonsJsonParser().getSeasonsJson(js);
                            if(seasonsJson != null) {
                                List<Season> seasons = new SeasonsParser().getItems(seasonsJson);
                                if (seasons.size() == 0) { // episodes parsed
                                    //Add episodes to ListView
                                    Season season = new EpisodesParser().getItem(seasonsJson);
                                    // Set season data
                                    linkData.postValue(new LinkData(false,
                                            null,
                                            season,
                                            null,
                                            false));
                                } else { // seasons parsed
                                    // Set seasons data
                                    linkData.postValue(new LinkData(false,
                                            seasons,
                                            null,
                                            null,
                                            false));
                                }
                            }
                        }
                    }
                }catch (IOException e) {
                    // Set error data
                    linkData.postValue(new LinkData(false,
                            null,
                            null,
                            null,
                            true));
                }
            }
        });
    }
}
