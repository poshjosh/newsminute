package com.looseboxes.idisc.googleservicesextras;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.looseboxes.idisc.common.handlers.MenuHandlerMain;

public class MenuHandlerMainWithAppInvite extends MenuHandlerMain {
    public boolean isMenuItemToEnable(int id) {
        if (id == R.id.main_actions_invite) {
            return true;
        }
        return super.isMenuItemToEnable(id);
    }

    public void onOptionsItemSelected(MenuItem item, Bundle bundle) {
        if (item.getItemId() == R.id.main_actions_invite) {
            Activity activity = getActivity();
            activity.startActivity(new Intent(activity, AppInviteActivity.class));
            return;
        }
        super.onOptionsItemSelected(item, bundle);
    }
}
