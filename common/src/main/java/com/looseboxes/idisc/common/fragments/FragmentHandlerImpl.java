package com.looseboxes.idisc.common.fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.Logx;

public class FragmentHandlerImpl implements FragmentHandler {
    private final Fragment fragment;

    public FragmentHandlerImpl(Fragment fragment) {
        this.fragment = fragment;
    }

    public void update(Intent intent, boolean clearPrevious) {
        if (intent != null) {
            update(intent.getExtras(), clearPrevious);
        }
    }

    public void update(Bundle bundle, boolean clearPrevious) {
        Bundle arguments = this.fragment.getArguments();
        Logx.debug(getClass(), "Before Update. {0}", arguments);
        if (arguments == null) {
            this.fragment.setArguments(bundle);
        } else {
            if (clearPrevious) {
                arguments.clear();
            }
            if (bundle != null) {
                arguments.putAll(bundle);
            }
        }
        Logx.debug(getClass(), "After Update. {0}", arguments);
    }

    @Nullable
    public final View findViewById(@IdRes int id) {
        return getFragment().getView().findViewById(id);
    }

    @TargetApi(23)
    public Context getContext() {
        Fragment fragment = getFragment();
        if (App.isAcceptableVersion(fragment.getActivity(), 23)) {
            return fragment.getContext();
        }
        return fragment.getActivity();
    }

    public Fragment getFragment() {
        return this.fragment;
    }
}
