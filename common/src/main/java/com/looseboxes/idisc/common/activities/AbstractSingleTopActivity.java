package com.looseboxes.idisc.common.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.handlers.HasMenuHandler;
import com.looseboxes.idisc.common.handlers.MenuHandler;
import com.looseboxes.idisc.common.util.Logx;

public abstract class AbstractSingleTopActivity extends AppCompatActivity implements HasMenuHandler, OnQueryTextListener, SearchView.OnQueryTextListener {
    private MenuHandler _mh_accessViaGetter;
    private boolean toolbarBug;

    public abstract int getContentView();

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

    protected void doCreate(Bundle savedInstanceState) {
    }

    protected final void onCreate(Bundle savedInstanceState) {
        try {
            Logx.log(Log.DEBUG, getClass(), "onCreate");
            super.onCreate(savedInstanceState);
            if (!updateFragment(savedInstanceState)) {
                setContentView(getContentView());
            }
            setupActionBar();
            doCreate(savedInstanceState);
            handleIntent(getIntent());
        } catch (Exception e) {
            Logx.debug(getClass(), e);
        }
    }

    protected boolean updateFragment(Bundle savedInstanceState) {
        return false;
    }

    @TargetApi(21)
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
                android.widget.Toolbar toolbar = (android.widget.Toolbar) v;
//                toolbar.setTitle(R.string.app_label);
                setActionBar(toolbar);
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.hide();
                }
            }else{
                Logx.log(Log.INFO, this.getClass(), " JESUS IS LORD ======================================================================");
                this.setActionBarBackground(this);
            }
        } catch (Exception bug) {
            this.toolbarBug = true;
            hideActionBar(this);
            Logx.log(getClass(), bug);
        }
        try {
            if (getMenuHandler(true) == null) {
                hideActionBar(this);
            }
        } catch (Exception bug2) {
            this.toolbarBug = true;
            Logx.log(getClass(), bug2);
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

    @TargetApi(23)
    private void setActionBarBackground(Activity activity) {
        final int color;
        if(App.isAcceptableVersion(activity, 23)) {
            color = this.getResources().getColor(R.color.background, activity.getTheme());
        }else{
            color = this.getResources().getColor(R.color.background);
//            color = this.getColor(R.color.background); //  throws java.lang.NoSuchMethodError: com.looseboxes.idisc.common.activities.XXXActivity.getColor
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
                    Logx.log(getClass(), bug);
                }
            }
        } catch (Exception bug2) {
            this.toolbarBug = true;
            Logx.log(getClass(), bug2);
        }
    }

    protected void onNewIntent(Intent intent) {
        Logx.log(Log.DEBUG, getClass(), "#onNewIntent");
        setIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
    }

    protected void onResume() {
        Logx.log(Log.VERBOSE, getClass(), "#onResume");
        super.onResume();
        App.setVisible(true);
    }

    protected void onPause() {
        Logx.log(Log.VERBOSE, getClass(), "On pause");
        super.onPause();
        App.setVisible(false);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        boolean output = super.onCreateOptionsMenu(menu);
        try {
            MenuHandler menuHandler = getMenuHandler(true);
            if (menuHandler != null) {
                menuHandler.onCreateOptionsMenu(menu, getOptionsMenuBundle());
                Logx.debug(getClass(), "Done creating options menu, output: " + output);
            }
        } catch (Exception e) {
            Logx.debug(getClass(), e);
        }
        return output;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean output = super.onPrepareOptionsMenu(menu);
        try {
            MenuHandler menuHandler = getMenuHandler(false);
            if (menuHandler != null) {
                menuHandler.onPrepareOptionsMenu(menu, getOptionsMenuBundle());
                Logx.debug(getClass(), "Done preparing options menu, output: " + output);
            }
        } catch (Exception e) {
            Logx.debug(getClass(), e);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean output = super.onOptionsItemSelected(item);
        try {
            MenuHandler menuHandler = getMenuHandler(false);
            if (menuHandler != null) {
                menuHandler.onOptionsItemSelected(item, getOptionsMenuBundle());
                Logx.debug(getClass(), "Done options item selected, output: " + output);
            }
        } catch (Exception e) {
            Logx.debug(getClass(), e);
        }
        return output;
    }

    public final MenuHandler getMenuHandler(boolean create) {
        if (this.toolbarBug) {
            return null;
        }
        if (this._mh_accessViaGetter == null && create) {
            try {
                this._mh_accessViaGetter = ((DefaultApplication) getApplication()).createMenuHandler(this);
                if (this._mh_accessViaGetter != null) {
                    this._mh_accessViaGetter.setActivity(this);
                }
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }
        return this._mh_accessViaGetter;
    }
}
