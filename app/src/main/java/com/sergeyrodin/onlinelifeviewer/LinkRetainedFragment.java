package com.sergeyrodin.onlinelifeviewer;

import android.app.Fragment;
import android.os.Bundle;

import java.util.List;

/**
 * Created by sergey on 20.02.18.
 */

public class LinkRetainedFragment extends Fragment {
    private List<Link> mData;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(List<Link> data) {
        mData = data;
    }

    public List<Link> getData() {
        return mData;
    }
}
