package com.sergeyrodin.onlinelifeviewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by root on 08.05.16.
 */
public class PlayDialogFragment extends DialogFragment {
    private PlaylistItem mPsItem;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mPsItem = (PlaylistItem)getArguments().getSerializable(MainActivity.EXTRA_PSITEM);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mPsItem.getComment());
        String fileSize = mPsItem.getFileSize()!=null?" (" + mPsItem.getFileSize() + " Mb)":"";
        String downloadSize = mPsItem.getDownloadSize()!=null?" (" + mPsItem.getDownloadSize() + " Mb)":"";

        if(!fileSize.isEmpty() && !downloadSize.isEmpty()) {
            String[] items = new String[] {
                    getString(R.string.flv) + fileSize,
                    getString(R.string.mp4) + downloadSize,
            };

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0://Flv
                            ProcessPlaylistItem.show(getActivity(), mPsItem.getFile());
                            break;
                        case 1://Mp4
                            ProcessPlaylistItem.show(getActivity(), mPsItem.getDownload());
                            break;
                    }
                }
            });
        }else if(!fileSize.isEmpty() && downloadSize.isEmpty()) {
            String[] items = new String[] {
                    getString(R.string.flv) + fileSize,
            };

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0:// Flv
                            ProcessPlaylistItem.show(getActivity(), mPsItem.getFile());
                            break;
                    }
                }
            });
        }else if(!downloadSize.isEmpty() && fileSize.isEmpty()) {
            String[] items = new String[] {
                    getString(R.string.mp4) + downloadSize,
            };

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0://Download
                            ProcessPlaylistItem.show(getActivity(), mPsItem.getDownload());
                            break;
                    }
                }
            });
        }else if(fileSize.isEmpty() && downloadSize.isEmpty()) {
            builder.setTitle(R.string.no_links_found);
        }

        return builder.create();
    }
}
