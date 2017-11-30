package com.sergeyrodin.onlinelifeviewer;

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by root on 07.05.16.
 */
public class RetainedFragment extends Fragment {
    private ArrayList<Result> mResults;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(ArrayList<Result> results) {
        mResults = results;
    }

    public ArrayList<Result> getData() {
        return mResults;
    }
}
