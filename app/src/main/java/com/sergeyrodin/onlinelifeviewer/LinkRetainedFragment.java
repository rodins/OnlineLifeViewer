package com.sergeyrodin.onlinelifeviewer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import java.util.List;

/**
 * Created by sergey on 20.02.18.
 */

public class LinkRetainedFragment extends Fragment {
    private static final String TAG = "LinkRetainedFragment";
    public List<Link> Data;

    public static LinkRetainedFragment findOrCreateRetainedFragment(FragmentManager fm) {
        LinkRetainedFragment fragment = (LinkRetainedFragment)fm.findFragmentByTag(TAG);
        if(fragment == null) {
            fragment = new LinkRetainedFragment();
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
