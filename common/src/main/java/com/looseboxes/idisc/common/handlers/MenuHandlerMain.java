package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.activities.DeveloperActivity;
import com.looseboxes.idisc.common.util.Logx;

public class MenuHandlerMain extends AbstractMenuHandler {
    public int getMenuResourceId() {
        return R.menu.main_actions;
    }

    public int getShareActionViewId() {
        return R.id.main_actions_share;
    }

    public int getLoginActionViewId() {
        return R.id.main_actions_login;
    }

    public int getSearchActionViewId() {
        return R.id.main_actions_search;
    }

    public int getAboutActionViewId() {
        return R.id.main_actions_about;
    }

    public int getSettingsActionViewId() {
        return R.id.main_actions_settings;
    }

    public int getForgotpasswordActionViewId() {
        return R.id.main_actions_forgotpassword;
    }

    public int getSignupActionViewId() {
        return R.id.main_actions_signup;
    }

    public int getInviteActionViewId() {
        return R.id.main_actions_invite;
    }

    public int getActivateActionViewId() {
        return R.id.main_actions_activate;
    }

    public boolean isMenuItemToEnable(int id) {
        if (id == R.id.main_actions_developer) {
            return User.getInstance().isAdmin(getActivity()) || Logx.isDebugMode();
        } else {
            return super.isMenuItemToEnable(id);
        }
    }

    public void onOptionsItemSelected(MenuItem item, Bundle bundle) {
        Activity activity = getActivity();
        if (item.getItemId() == R.id.main_actions_developer) {
            activity.startActivity(new Intent(activity, DeveloperActivity.class));
        } else {
            super.onOptionsItemSelected(item, bundle);
        }
    }
}
