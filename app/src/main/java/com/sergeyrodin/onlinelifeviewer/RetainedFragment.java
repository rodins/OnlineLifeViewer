package com.sergeyrodin.onlinelifeviewer;

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 07.05.16.
 */
public class RetainedFragment extends Fragment {
    private List<Result> mResults;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(List<Result> results) {
        mResults = results;
    }

    public List<Result> getData() {
        return mResults;
    }
}
