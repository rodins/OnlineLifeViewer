package com.sergeyrodin.onlinelifeviewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

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

        String[] items = new String[] {
          getString(R.string.flv),
          getString(R.string.mp4),
        };

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case 0://Play
                        ProcessPlaylistItem.show(getActivity(), mPsItem.getFile());
                        break;
                    case 1://Download
                        ProcessPlaylistItem.show(getActivity(), mPsItem.getDownload());
                        break;
                }
            }
        });

        return builder.create();
    }
}
