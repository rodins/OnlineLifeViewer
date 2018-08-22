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
        String fileSize = mPsItem.getFileSize()!=null?" (" + mPsItem.getFileSize() + " " + getString(R.string.mb) + ")":"";
        String downloadSize = mPsItem.getDownloadSize()!=null?" (" + mPsItem.getDownloadSize() + " " + getString(R.string.mb) + ")":"";

        if(!fileSize.isEmpty() && !downloadSize.isEmpty()) {
            String[] items = new String[] {
                    getString(R.string.flv) + fileSize,
                    getString(R.string.mp4) + downloadSize,
                    getString(R.string.actors_title)
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
                        case 2://Info
                            ProcessPlaylistItem.startActorsActivity(getActivity(),
                                                                    mPsItem.getInfoTitle(),
                                                                    mPsItem.getInfoLink());
                    }
                }
            });
        }else if(!fileSize.isEmpty()) {
            String[] items = new String[] {
                    getString(R.string.flv) + fileSize,
                    getString(R.string.actors_title)
            };

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0:// Flv
                            ProcessPlaylistItem.show(getActivity(), mPsItem.getFile());
                            break;
                        case 1://Info
                            ProcessPlaylistItem.startActorsActivity(getActivity(),
                                    mPsItem.getInfoTitle(),
                                    mPsItem.getInfoLink());
                    }
                }
            });
        }else if(!downloadSize.isEmpty()) {
            String[] items = new String[] {
                    getString(R.string.mp4) + downloadSize,
                    getString(R.string.actors_title)
            };

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0://Download
                            ProcessPlaylistItem.show(getActivity(), mPsItem.getDownload());
                            break;
                        case 1://Info
                            ProcessPlaylistItem.startActorsActivity(getActivity(),
                                    mPsItem.getInfoTitle(),
                                    mPsItem.getInfoLink());
                    }
                }
            });
        }else {
            builder.setTitle(R.string.no_links_found); //TODO: this should be Toast, not part of dialog
        }

        return builder.create();
    }
}
