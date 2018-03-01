package com.sergeyrodin.onlinelifeviewer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 07.05.16.
 */
public class ResultsRetainedFragment extends Fragment {
    private static final String TAG = "ResultsRetainedFragment";
    public List<Result> mRetainedData;
    public LruCache<String, Bitmap> mRetainedCache;

    public static ResultsRetainedFragment findOrCreateRetainedFragment(FragmentManager fm) {
        ResultsRetainedFragment fragment = (ResultsRetainedFragment)fm.findFragmentByTag(TAG);
        if(fragment == null) {
            fragment = new ResultsRetainedFragment();
            fm.beginTransaction().add(fragment, TAG).commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
