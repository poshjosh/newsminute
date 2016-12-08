package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

import com.bc.android.core.asynctasks.ReadJsonObjectWithSingleEntry;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.util.PropertiesManager;

/**
 * Created by Josh on 7/5/2016.
 */
public abstract class AbstractReadTask<T> extends ReadJsonObjectWithSingleEntry<T> {

    public AbstractReadTask(Context context, String errorMessage) {

        super(context, errorMessage,
                App.getPropertiesManager(context).getInt(PropertiesManager.PropertyName.connectTimeoutMillis),
                App.getPropertiesManager(context).getInt(PropertiesManager.PropertyName.readTimeoutMillis));

        final String iso2countrycode = User.getInstance().getIso2CountryCode(context, null);

        if(iso2countrycode != null) {
            final String accept_language = "en-"+iso2countrycode+", en";
            this.addRequestProperty("Accept-Language", accept_language);
            Logx.getInstance().debug(this.getClass(), "Accept-Language: {0}", accept_language);
        }
    }

    @Override
    public String getServiceUrl() {
        return App.getPropertiesManager(getContext()).getString(PropertiesManager.PropertyName.appServiceUrl);
    }
}
