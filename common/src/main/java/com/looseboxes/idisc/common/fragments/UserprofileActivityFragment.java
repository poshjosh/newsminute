package com.looseboxes.idisc.common.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.looseboxes.idisc.common.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class UserprofileActivityFragment extends Fragment {

    public UserprofileActivityFragment() { }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_userprofile, container, false);
    }
}
