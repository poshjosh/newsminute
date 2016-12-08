package com.looseboxes.idisc.common.fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.util.NewsminuteUtil;

public abstract class BaseFragment extends Fragment {

    private View root;

    public abstract int getContentViewId();

    protected BaseFragment() { }

    public void update(Bundle bundle, boolean clearPrevious) {
        NewsminuteUtil.update(this, bundle, clearPrevious);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(getContentViewId(), container, false);
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
        if (App.isAcceptableVersion(this.getActivity(), 23)) {
            return this.getContext_v23();
        }
        return this.getActivity();
    }

    @TargetApi(23)
    private Context getContext_v23() {
        return super.getContext();
    }
}
