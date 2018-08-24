package com.sergeyrodin.onlinelifeviewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
        String fileSize = mPsItem.getFileSize()!=null?" (" +
                mPsItem.getFileSize() + " " + getString(R.string.mb) + ")":"";
        String downloadSize = mPsItem.getDownloadSize()!=null?" (" +
                mPsItem.getDownloadSize() + " " + getString(R.string.mb) + ")":"";

        if(fileSize.isEmpty() && downloadSize.isEmpty()) {
            builder.setMessage(R.string.no_links_found)
                    .setPositiveButton(R.string.play_dialog_ok, null);
        }else {
            List<String> itemsList = new ArrayList<>();

            if(!fileSize.isEmpty() && mPsItem.getFile().equals(mPsItem.getDownload())) {
                itemsList.add(getString(R.string.mp4) + downloadSize);
            }else {
                if(!fileSize.isEmpty()) {
                    itemsList.add(getString(R.string.flv) + fileSize);
                }

                if(!downloadSize.isEmpty()) {
                    itemsList.add(getString(R.string.mp4) + downloadSize);
                }
            }

            if(mPsItem.getInfoTitle() != null) {
                itemsList.add(getString(R.string.actors_title));
            }

            final String[] items = itemsList.toArray(new String[itemsList.size()]);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String selection = items[which];

                    if(selection.contains(getString(R.string.flv))) {
                        ProcessPlaylistItem.show(getActivity(), mPsItem.getFile());
                    }else if(selection.contains(getString(R.string.mp4))) {
                        ProcessPlaylistItem.show(getActivity(), mPsItem.getDownload());
                    }else if(selection.contains(getString(R.string.actors_title))) {
                        ProcessPlaylistItem.startActorsActivity(getActivity(),
                                mPsItem.getInfoTitle(),
                                mPsItem.getInfoLink());
                    }
                }
            });
        }

        return builder.create();
    }
}
