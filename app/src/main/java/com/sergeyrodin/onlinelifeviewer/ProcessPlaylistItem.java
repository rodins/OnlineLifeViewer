package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.sergeyrodin.onlinelifeviewer.utilities.NetworkUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by root on 08.05.16.
 */
public class ProcessPlaylistItem {
    private static final String TAG = ProcessPlaylistItem.class.getSimpleName();
    public static void show(Context activity, String link) {
        if(link != null && !link.isEmpty()) {
            Uri uri = Uri.parse(link);
            Intent viewMediaIntent = new Intent();
            viewMediaIntent.setAction(Intent.ACTION_VIEW);
            // Set data type to use android system media player
            if(link.endsWith(".flv") || link.endsWith(".mp4")) {
                viewMediaIntent.setDataAndType(uri, "video/*");
            }else {
                viewMediaIntent.setData(uri);
            }
            viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if(viewMediaIntent.resolveActivity(activity.getPackageManager()) != null){
                activity.startActivity(viewMediaIntent);
            }else {
                Toast.makeText(activity, R.string.no_app_found, Toast.LENGTH_SHORT);
            }
        }
    }

    public static void process(Activity activity, PlaylistItem psItem) {
        if (psItem != null) {
            new SizeAsyncTask(activity.getFragmentManager()).execute(psItem);
            //start play/download dialog
            /*if(psItem.getDownload() == null) {// no download link, use file link
                show(activity, psItem.getFile());
            }else {// select file or download link with PlayDialog
                Bundle args = new Bundle();
                args.putSerializable(MainActivity.EXTRA_PSITEM, psItem);
                PlayDialogFragment playDialog = new PlayDialogFragment();
                playDialog.setArguments(args);
                playDialog.show(activity.getFragmentManager(), "playdialog");
            }*/
        }
    }

    static class SizeAsyncTask extends AsyncTask<PlaylistItem, Void, PlaylistItem> {
        private FragmentManager mFragmentManager;

        public SizeAsyncTask(FragmentManager fm) {
            mFragmentManager = fm;
        }

        private String toMegabytes(String size) {
            if(size == null) return null;
            int bytes = Integer.parseInt(size);
            int megabytes = bytes/1024/1024;
            return Integer.toString(megabytes);
        }

        @Override
        protected PlaylistItem doInBackground(PlaylistItem... items) {
            PlaylistItem psItem = items[0];
            try {
                URL file = new URL(psItem.getFile());
                String size = toMegabytes(NetworkUtils.getLinkSize(file));
                psItem.setFileSize(size);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                URL download = new URL(psItem.getDownload());
                String size = toMegabytes(NetworkUtils.getLinkSize(download));
                psItem.setDownloadSize(size);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return psItem;
        }

        @Override
        protected void onPostExecute(PlaylistItem psItem) {
            Bundle args = new Bundle();
            args.putSerializable(MainActivity.EXTRA_PSITEM, psItem);
            PlayDialogFragment playDialog = new PlayDialogFragment();
            playDialog.setArguments(args);
            playDialog.show(mFragmentManager, "playdialog");
        }
    }
}
