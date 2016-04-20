package com.looseboxes.idisc.common.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadManager;
import com.looseboxes.idisc.common.util.AliasesManager;
import com.looseboxes.idisc.common.util.AliasesManager.AliasType;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.Util;

public class DownloadService extends IntentService {

    private WakeLock mWakeLock;

    public DownloadService() {
        super(DownloadService.class.getName());
    }

    public DownloadService(String name) {
        super(name);
    }

    public void onDestroy() {
        try {
            log("#onDestroy");
            super.onDestroy();
            if (this.mWakeLock != null) {
                this.mWakeLock.release();
                this.mWakeLock = null;
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    protected void onHandleIntent(Intent workIntent) {
        try {
            doHandleIntent();
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    private void acquireWakeLock() {
        log("Acquiring wake lock");
        try {
            if (this.mWakeLock == null) {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    this.mWakeLock = pm.newWakeLock(1, DownloadService.class.getName());
                }
            }
            if (this.mWakeLock != null) {
                this.mWakeLock.acquire();
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    private void doHandleIntent() {
        log("#doHandleIntent entering");
        if (Util.isNetworkConnectedOrConnecting(this)) {
            acquireWakeLock();
            try {
                FeedDownloadManager dm = new FeedDownloadManager(this);
                dm.setNoUI(true);
                log("Downloading feeds");
                dm.execute();
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
            try {
                PropertiesManager pm = App.getPropertiesManager(this);
                pm.setNoUI(true);
                log("Updating properties");
                pm.update(false);
            } catch (Exception e2) {
                Logx.log(getClass(), e2);
            }
            for (AliasType aliasType : AliasType.values()) {
                try {
                    AliasesManager am = App.getAliasesManager(this, aliasType);
                    am.setNoUI(true);
                    log("Updating " + aliasType);
                    am.update(false);
                } catch (Exception e22) {
                    Logx.log(getClass(), e22);
                }
            }
            if (User.getInstance().isLoggedIn(this)) {
                for (PreferenceType preferenceType : PreferenceType.values()) {
                    try {
                        PreferenceFeedsManager pfm = new PreferenceFeedsManager(this, preferenceType);
                        pfm.setNoUI(true);
                        log("Updating " + preferenceType);
                        pfm.update(false);
                    } catch (Exception e222) {
                        Logx.log(getClass(), e222);
                    }
                }
            }
            log("#doHandleIntent exiting");
        }
    }

    private void log(String msg) {
        Logx.log(Log.VERBOSE, getClass(), msg);
    }
}
