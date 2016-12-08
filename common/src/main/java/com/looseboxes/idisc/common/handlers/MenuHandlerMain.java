package com.looseboxes.idisc.common.handlers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.activities.DeveloperActivity;

public class MenuHandlerMain extends AbstractMenuHandler {

    @Override
    public int getMenuResourceId() {
        return R.menu.main_actions;
    }

    public boolean isMenuItemToEnable(int id) {
        if (id == R.id.main_actions_developer) {
            return User.getInstance().isAdmin(getActivity()) || Logx.getInstance().isDebugMode();
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
