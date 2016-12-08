package com.looseboxes.idisc.common.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;

public class AboutUsActivity extends AbstractSingleTopActivity {

    @Override
    protected void doCreate(Bundle savedInstanceState) {

        super.doCreate(savedInstanceState);

        try{

//            final int versionCode = App.getVersionCode(this);
            final String versionName = App.getVersionName(this);
            final String text = this.getString(R.string.app_label) + " (v" + versionName + ')';

            TextView view = (TextView)this.findViewById(R.id.about_appname);

            view.setText(text);

        }catch(Exception e) {
            Logx.getInstance().log(this.getClass(), e);
        }
    }

    public int getContentViewId() {
        return R.layout.about;
    }
}
