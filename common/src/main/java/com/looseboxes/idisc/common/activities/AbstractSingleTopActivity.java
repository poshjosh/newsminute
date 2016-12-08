package com.looseboxes.idisc.common.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.handlers.HasMenuHandler;
import com.looseboxes.idisc.common.handlers.MenuHandler;
import com.bc.android.core.util.Logx;

public abstract class AbstractSingleTopActivity extends AppCompatActivity
        implements HasMenuHandler, OnQueryTextListener, SearchView.OnQueryTextListener {

    private boolean toolbarBug;

    public abstract @LayoutRes int getContentViewId();

    public boolean onQueryTextSubmit(String query) {
        MenuHandler menuHandler = getMenuHandler(false);
        if (menuHandler != null) {
            View searchView;
            if (menuHandler.isAppCompat()) {
                searchView = menuHandler.getSearchViewAppCompat();
            } else {
                searchView = menuHandler.getSearchView();
            }
            searchView.clearFocus();
        }
        return false;
    }

    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public Bundle getBundleDataForSearchableActivity() {
        return null;
    }

    @Override
    public boolean onSearchRequested() {
        if ((getResources().getConfiguration().uiMode & 15) == 4) {
            return false;
        }
        startSearch(null, false, getBundleDataForSearchableActivity(), false);
        return true;
    }

    public String getShareText() {
        return null;
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {

//        final long tb4 = System.currentTimeMillis();
//        final long mb4 = Runtime.getRuntime().freeMemory();

        try {

            Logx.getInstance().log(Log.VERBOSE, getClass(), "onCreate");

            super.onCreate(savedInstanceState);

// Applicable only when using actionbar
//            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//            setProgressBarIndeterminateVisibility(true);

            if (!updateFragment(savedInstanceState)) {
                setContentView(getContentViewId());
            }

            doCreate(savedInstanceState);

            setupActionBar();

            handleIntent(getIntent());

//            Logx.getInstance().debug(this.getClass(), "Done onCreate(Bundle) consumed time: {0}, memory: {1}",
//                    (System.currentTimeMillis()-tb4), (mb4-Runtime.getRuntime().freeMemory()));

        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
    }

    protected boolean updateFragment(Bundle savedInstanceState) {
        return false;
    }

    protected void doCreate(Bundle savedInstanceState) { }

    @Override
    @CallSuper
    protected void onNewIntent(Intent intent) {

//@changed
        super.onNewIntent(intent);

        Logx.getInstance().log(Log.VERBOSE, getClass(), "#onNewIntent");
        setIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) { }

    @Override
    @CallSuper
    protected void onPause() {
        super.onPause();
        Logx.getInstance().log(Log.VERBOSE, getClass(), "On pause");
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        Logx.getInstance().log(Log.VERBOSE, getClass(), "#onResume");
        App.setVisible(true);
        try{
// Applicable only when using actionbar
//            setProgressBarIndeterminateVisibility(false);
        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
        }
    }

    @Override
    @CallSuper
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean output = super.onCreateOptionsMenu(menu);
        try {
            MenuHandler menuHandler = getMenuHandler(true);
            if (menuHandler != null) {
                menuHandler.onCreateOptionsMenu(menu, getOptionsMenuBundle());
                Logx.getInstance().log(Log.VERBOSE, this.getClass(),  "Done creating options menu, output: " + output);
            }
        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
        return output;
    }

    @Override
    @CallSuper
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean output = super.onPrepareOptionsMenu(menu);
        try {
            MenuHandler menuHandler = getMenuHandler(false);
            if (menuHandler != null) {
                menuHandler.onPrepareOptionsMenu(menu, getOptionsMenuBundle());
                Logx.getInstance().log(Log.VERBOSE, this.getClass(), "Done preparing options menu, output: " + output);
            }
        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
        return output;
    }

    private Bundle getOptionsMenuBundle() {
        String shareText = getShareText();
        if (shareText == null || shareText.isEmpty()) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString(MenuHandler.BUNDLE_STRING_SHARE_TEXT, shareText);
        return bundle;
    }

    @Override
    @CallSuper
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean output = super.onOptionsItemSelected(item);
        try {
            MenuHandler menuHandler = getMenuHandler(false);
            if (menuHandler != null) {
                menuHandler.onOptionsItemSelected(item, getOptionsMenuBundle());
                Logx.getInstance().debug(getClass(), "Done options item selected, output: " + output);
            }
        } catch (Exception e) {
            Logx.getInstance().debug(getClass(), e);
        }
        return output;
    }

    protected void setVisibility(final View view, final int visibility) {
        if(view == null || view.getVisibility() == visibility) {
            return;
        }else {
            if (this.isOnUIThread()) {
                this.doSetVisibility(view, visibility);
            } else {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        doSetVisibility(view, visibility);
                    }
                });
            }
        }
    }
    private void doSetVisibility(View view, int visibility) {
        try {
            view.setVisibility(visibility);
            Logx.getInstance().debug(this.getClass(), view.getClass().getSimpleName() + ' ' + this.getVisibilityName(visibility, "UNKNOWN_VISIBILITY"));
        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
        }
    }

    protected String getVisibilityName(int visibility, String valueIfUnknown) {
        final String visibilityName;
        switch(visibility) {
            case View.VISIBLE: visibilityName = "VISIBLE"; break;
            case View.INVISIBLE: visibilityName = "INVISIBLE"; break;
            case View.GONE: visibilityName = "GONE"; break;
            default: visibilityName = valueIfUnknown;
        }
        return visibilityName;
    }

    public final boolean isOnUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private void setupActionBar() {
        try {
            View v = findViewById(R.id.my_toolbar);
            if (v instanceof Toolbar) {
                Toolbar toolbar = (Toolbar)v;
//                toolbar.setTitle(R.string.app_label);
                setSupportActionBar(toolbar);
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.hide();
                }
            } else if (App.isAcceptableVersion(this, 21) && (v instanceof android.widget.Toolbar)) {
                setupActionBar_v21();
            }else{
                this.setActionBarBackground(this);
            }
        } catch (Exception bug) {
            this.toolbarBug = true;
            hideActionBar(this);
            Logx.getInstance().log(getClass(), bug);
        }
    }

    @TargetApi(21)
    private void setupActionBar_v21() {
        View v = findViewById(R.id.my_toolbar);
        if (v instanceof android.widget.Toolbar) {
            android.widget.Toolbar toolbar = (android.widget.Toolbar) v;
//                toolbar.setTitle(R.string.app_label);
            setActionBar(toolbar);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    }

    private void hideActionBar(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            android.support.v7.app.ActionBar appCompatActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (appCompatActionBar != null) {
                appCompatActionBar.hide();
            }
        }
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void setActionBarBackground(Activity activity) {
        final int color;
        if(App.isAcceptableVersion(activity, 23)) {
            color = this.getColor_v23(R.color.background, activity.getTheme());
        }else{
            color = this.getColor_v22_and_below(R.color.background);
        }
        Drawable drawable = new ColorDrawable(color);
        if (activity instanceof AppCompatActivity) {
            android.support.v7.app.ActionBar appCompatActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (appCompatActionBar != null) {
                appCompatActionBar.setBackgroundDrawable(drawable);
            }
        }
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(drawable);
        }
    }

    @TargetApi(23)
    public int getColor_v23(@ColorRes int id, @Nullable Resources.Theme theme) {
        return this.getResources().getColor(id, theme);
    }

    public int getColor_v22_and_below(@ColorRes int id) {
        return this.getResources().getColor(id);
//            return this.getColor(R.color.background); //  throws java.lang.NoSuchMethodError: com.looseboxes.idisc.common.activities.XXXActivity.getColor
    }

    private MenuHandler _mh_accessViaGetter;
    public final MenuHandler getMenuHandler(boolean create) {
        if (this.toolbarBug) {
            return null;
        }
        if (this._mh_accessViaGetter == null && create) {
            try {
                this._mh_accessViaGetter = ((DefaultApplication) getApplication()).createMenuHandler(this);
                if (this._mh_accessViaGetter != null) {
                    this._mh_accessViaGetter.setActivity(this);
                }else{
                    try {
                        hideActionBar(this);
                    } catch (Exception bug2) {
                        this.toolbarBug = true;
                        Logx.getInstance().log(getClass(), bug2);
                    }
                }
            } catch (Exception e) {
                Logx.getInstance().log(getClass(), e);
            }
        }
        return this._mh_accessViaGetter;
    }

    @UiThread
    protected void setText(final View view, final TextView textView, @StringRes final int text) {
        if(this.isOnUIThread()) {
            textView.setText(text);
        }else {
            view.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(text);
                }
            });
        }
    }

    @UiThread
    protected void setProgress(final View view, final ProgressBar progressBar, final int progress) {
        if(this.isOnUIThread()) {
            progressBar.setProgress(progress);
        }else {
            view.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(progress);
                }
            });
        }
    }

    private void setupActionBar_old() {
        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                try {
                    if (getMenuHandler(true) == null) {
                        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.hide();
                        }
                    }
                } catch (Exception bug) {
                    this.toolbarBug = true;
                    Logx.getInstance().log(getClass(), bug);
                }
            }
        } catch (Exception bug2) {
            this.toolbarBug = true;
            Logx.getInstance().log(getClass(), bug2);
        }
    }
}
