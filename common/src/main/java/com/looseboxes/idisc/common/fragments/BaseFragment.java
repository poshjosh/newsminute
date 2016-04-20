package com.looseboxes.idisc.common.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment implements HasFragmentHandler, FragmentHandler {
    private FragmentHandler _fh;
    private View root;

    public abstract int getContentView();

    protected BaseFragment() {
    }

    public FragmentHandler getFragmentHandler() {
        if (this._fh == null) {
            this._fh = new FragmentHandlerImpl(this);
        }
        return this._fh;
    }

    public Fragment getFragment() {
        return this;
    }

    public void update(Bundle bundle, boolean clearPrevious) {
        getFragmentHandler().update(bundle, clearPrevious);
    }

    public void update(Intent intent, boolean clearPrevious) {
        getFragmentHandler().update(intent, clearPrevious);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(getContentView(), container, false);
        return this.root;
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.root = null;
    }

    @Nullable
    public View findViewById(@IdRes int id) {
        if (this.root != null) {
            return this.root.findViewById(id);
        }
        return getView() != null ? getView().findViewById(id) : null;
    }

    public Context getContext() {
        return getFragmentHandler().getContext();
    }
}
