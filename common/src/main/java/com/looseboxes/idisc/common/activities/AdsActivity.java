package com.looseboxes.idisc.common.activities;

import android.os.Bundle;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.util.AdvertManager;
import com.looseboxes.idisc.common.util.Logx;

public abstract class AdsActivity extends AbstractSingleTopActivity {
    private AdvertManager _am;

    protected void doCreate(Bundle savedInstanceState) {
        try {
            super.doCreate(savedInstanceState);
            Logx.debug(getClass(), "Activity: {0}", getClass().getName());
            getAdvertManager(true).onCreate(savedInstanceState);
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    protected void onResume() {
        try {
            super.onResume();
            if (getAdvertManager(false) != null) {
                getAdvertManager(false).onResume();
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    protected void onPause() {
        try {
            super.onPause();
            if (getAdvertManager(false) != null) {
                getAdvertManager(false).onPause();
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    protected void onDestroy() {
        try{
            // Destroy the AdvertManager before calling super
            if(this.getAdvertManager(false) != null) {
                this.getAdvertManager(false).onDestroy();
            }
            super.onDestroy();
        }catch(Exception e) {
            Logx.log(this.getClass(), e);
        }
    }

    public AdvertManager getAdvertManager(boolean createIfNotExists) {
        if (this._am == null && createIfNotExists) {
            this._am = ((DefaultApplication) getApplication()).createAdvertManager(this);
        }
        return this._am;
    }
}
