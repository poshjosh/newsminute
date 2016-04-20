package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.activities.AboutUsActivity;
import com.looseboxes.idisc.common.activities.AbstractSingleTopActivity;
import com.looseboxes.idisc.common.activities.ForgotPasswordActivity;
import com.looseboxes.idisc.common.activities.LoginActivity;
import com.looseboxes.idisc.common.activities.SettingsActivity;
import com.looseboxes.idisc.common.activities.SignupActivity;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Util;

public abstract class AbstractMenuHandler implements MenuHandler {
    private Activity activity;
    private boolean appCompat;
    private String mShareText;
    private SearchView searchView;
    private android.support.v7.widget.SearchView searchViewAppCompat;

    public abstract int getAboutActionViewId();

    public abstract int getActivateActionViewId();

    public abstract int getForgotpasswordActionViewId();

    public abstract int getInviteActionViewId();

    public abstract int getLoginActionViewId();

    public abstract int getMenuResourceId();

    public abstract int getSearchActionViewId();

    public abstract int getSettingsActionViewId();

    public abstract int getShareActionViewId();

    public abstract int getSignupActionViewId();

    public AbstractMenuHandler() {
        this.appCompat = true;
    }

    public void onCreateOptionsMenu(Menu menu, Bundle bundle) {
        init(menu, false);
        int searchActionViewId = getSearchActionViewId();
        if (searchActionViewId != -1 && App.isAcceptableVersion(this.activity, 11)) {
            MenuItem menuItem = menu.findItem(searchActionViewId);
            if (isAppCompat()) {
                initSearchViewAppCompat((android.support.v7.widget.SearchView) MenuItemCompat.getActionView(menuItem));
            } else {
                initSearchView((SearchView) menuItem.getActionView());
            }
        }
    }

    public SearchView getSearchView() {
        return this.searchView;
    }

    private void initSearchView(SearchView searchView) {
        this.searchView = searchView;
        if (searchView != null) {
            if (this.activity instanceof OnQueryTextListener) {
                searchView.setOnQueryTextListener((OnQueryTextListener) this.activity);
            }
            searchView.setSearchableInfo(((SearchManager) this.activity.getSystemService(Context.SEARCH_SERVICE)).getSearchableInfo(this.activity.getComponentName()));
            searchView.setIconifiedByDefault(true);
        }
    }

    public android.support.v7.widget.SearchView getSearchViewAppCompat() {
        return this.searchViewAppCompat;
    }

    private void initSearchViewAppCompat(android.support.v7.widget.SearchView searchViewAppCompat) {
        this.searchViewAppCompat = searchViewAppCompat;
        if (searchViewAppCompat != null) {
            if (this.activity instanceof android.support.v7.widget.SearchView.OnQueryTextListener) {
                searchViewAppCompat.setOnQueryTextListener((android.support.v7.widget.SearchView.OnQueryTextListener) this.activity);
            }
            searchViewAppCompat.setSearchableInfo(((SearchManager) this.activity.getSystemService(Context.SEARCH_SERVICE)).getSearchableInfo(this.activity.getComponentName()));
            searchViewAppCompat.setIconifiedByDefault(true);
        }
    }

    public boolean isMenuItemToEnable(int id) {
        if (id == getSignupActionViewId() || id == getForgotpasswordActionViewId()) {
            return !User.getInstance().isLoggedIn(this.activity);
        } else {
            if (id == getActivateActionViewId() || id == getInviteActionViewId()) {
                return false;
            }
            return true;
        }
    }

    public void onPrepareOptionsMenu(Menu menu, Bundle bundle) {
        init(menu, true);
        initMenus(menu);
        initShareText(menu, bundle);
        initLoginMenuItem(menu);
    }

    private MenuInflater init(Menu menu, boolean clearPrevious) {
        int menuResource = getMenuResourceId();
        if (menuResource == -1) {
            return null;
        }
        if (clearPrevious) {
            menu.clear();
        }
        MenuInflater inflater = this.activity.getMenuInflater();
        inflater.inflate(menuResource, menu);
        return inflater;
    }

    private void initMenus(Menu menu) {
        int size = menu.size();
        Logx.debug(getClass(), "Menu size: {0}", Integer.valueOf(size));
        for (int i = 0; i < size; i++) {
            MenuItem menuItem = menu.getItem(i);
            boolean enabled = isMenuItemToEnable(menuItem.getItemId());
            menuItem.setEnabled(enabled);
            menuItem.setVisible(enabled);
        }
    }

    private void initShareText(Menu menu, Bundle bundle) {
        int shareActionViewId = getSearchActionViewId();
        if (shareActionViewId != -1 && menu.findItem(shareActionViewId) != null) {
            this.mShareText = bundle == null ? null : bundle.getString(BUNDLE_STRING_SHARE_TEXT);
        }
    }

    private void initLoginMenuItem(Menu menu) {
        int loginActionViewId = getLoginActionViewId();
        if (loginActionViewId != -1) {
            String userId;
            User user = User.getInstance();
            if (user.isLoggedIn(this.activity)) {
                userId = user.getUsername(this.activity) == null ? user.getEmailAddress(this.activity) : user.getUsername(this.activity);
                if (userId.length() > 18) {
                    userId = userId.substring(0, 15) + "...";
                }
            } else {
                userId = null;
            }
            menu.findItem(loginActionViewId).setTitle(!user.isLoggedIn(this.activity) ? getActivity().getString(R.string.msg_login) : getActivity().getString(R.string.msg_logout) + " (" + userId + ")");
        }
    }

    public void onOptionsItemSelected(MenuItem item, Bundle bundle) {
        int itemId = item.getItemId();
        if (itemId == getSearchActionViewId()) {
            Logx.log(Log.VERBOSE, getClass(), "Search clicked");
            this.activity.onSearchRequested();
        } else if (itemId == getAboutActionViewId()) {
            this.activity.startActivity(new Intent(this.activity, AboutUsActivity.class));
        } else if (itemId == getShareActionViewId()) {
            if (this.mShareText == null && (this.activity instanceof AbstractSingleTopActivity)) {
                this.mShareText = ((AbstractSingleTopActivity) this.activity).getShareText();
            }
            if (this.mShareText != null && !this.mShareText.isEmpty()) {
                this.activity.startActivity(Intent.createChooser(Util.createShareIntent(this.activity, this.mShareText, "android.intent.extra.TEXT"), "Share via"));
            }
        } else if (itemId == getLoginActionViewId()) {
            if (User.getInstance().isLoggedIn(this.activity)) {
                Popup.show((View) null, (Object) "...Loging out", 0);
                User.getInstance().logout(this.activity);
                return;
            }
            this.activity.startActivity(new Intent(this.activity, LoginActivity.class));
        } else if (itemId == getSettingsActionViewId()) {
            this.activity.startActivity(new Intent(this.activity, SettingsActivity.class));
        } else if (itemId == getSignupActionViewId()) {
            if (User.getInstance().isLoggedIn(this.activity)) {
                Popup.show(this.activity, R.string.msg_already_loggedin, 0);
            } else {
                this.activity.startActivity(new Intent(this.activity, SignupActivity.class));
            }
        } else if (itemId != getForgotpasswordActionViewId()) {
            CharSequence itemTitle = item == null ? null : item.getTitle();
            StringBuilder append = new StringBuilder().append("Unexpected MenuItem: ");
            if (itemTitle == null) {
                itemTitle = Integer.toString(itemId);
            }
            throw new IllegalArgumentException(append.append(itemTitle).toString());
        } else if (User.getInstance().isLoggedIn(this.activity)) {
            Popup.show(this.activity, R.string.msg_already_loggedin, 0);
        } else {
            this.activity.startActivity(new Intent(this.activity, ForgotPasswordActivity.class));
        }
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return this.activity;
    }

    public boolean isAppCompat() {
        return this.appCompat;
    }

    public void setAppCompat(boolean appCompat) {
        this.appCompat = appCompat;
    }
}
