package com.sergeyrodin.onlinelifeviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by root on 08.05.16.
 */
public class ProcessPlaylistItem {
    public static void show(Context activity, String link) {
        if(link != null) {
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
        if (psItem != null) { //file found
            //start play/download dialog
            if(psItem.getDownload() == null) {// no download link, use file link
                show(activity, psItem.getFile());
            }else {// select file or download link with PlayDialog
                Bundle args = new Bundle();
                args.putSerializable(MainActivity.EXTRA_PSITEM, psItem);
                PlayDialogFragment playDialog = new PlayDialogFragment();
                playDialog.setArguments(args);
                playDialog.show(activity.getFragmentManager(), "playdialog");
            }
        }
    }
}
