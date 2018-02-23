package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by root on 11.05.16.
 */
public class ItemClickAsyncTask extends AsyncTask<Result, Void, String> {
    private Activity mActivity;
    public ItemClickAsyncTask(Activity activity) {
        mActivity = activity;
    }
    @Override
    protected String doInBackground(Result... params) {
        Result result = params[0];
        if(result != null) {
            try{
                return new Curl().getJsString(result.id);
            }catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String js) {
        if(js != null) {
            PlaylistItem psItem = new PlaylistItemParser().getItem(js);
            if(psItem.getComment() != null) {
                //Start process item dialog: select play or download item
                ProcessPlaylistItem.process(mActivity, psItem);
            }else {
                // Process activity_playlists in PlaylistsActivity
                Intent intent = new Intent(mActivity, PlaylistsActivity.class);
                intent.putExtra(MainActivity.EXTRA_JS, js);
                mActivity.startActivity(intent);
            }
        }else {
            Toast.makeText(mActivity, R.string.network_problem, Toast.LENGTH_SHORT).show();
        }
    }
}
