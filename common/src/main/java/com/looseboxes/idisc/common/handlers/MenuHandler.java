package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public interface MenuHandler {
    public static final String BUNDLE_STRING_SHARE_TEXT = MenuHandler.class.getName() + ".shareText";

    Activity getActivity();

    SearchView getSearchView();

    android.support.v7.widget.SearchView getSearchViewAppCompat();

    boolean isAppCompat();

    boolean isMenuItemToEnable(int i);

    void onCreateOptionsMenu(Menu menu, Bundle bundle);

    void onOptionsItemSelected(MenuItem menuItem, Bundle bundle);

    void onPrepareOptionsMenu(Menu menu, Bundle bundle);

    void setActivity(Activity activity);

    void setAppCompat(boolean z);
}
