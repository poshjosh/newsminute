package com.looseboxes.idisc.common.fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.looseboxes.idisc.common.App;

public abstract class BaseListFragment extends ListFragment implements HasFragmentHandler, FragmentHandler {
    private FragmentHandler _fh;
    private View root;

    public abstract int getContentView();

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
        this.root = super.onCreateView(inflater, container, savedInstanceState);
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

    @TargetApi(23)
    public Context getContext() {
        if (App.isAcceptableVersion(this.getActivity(), 23)) {
            return super.getContext();
        }
        return this.getActivity();
    }
}
