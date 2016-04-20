package com.looseboxes.idisc.common.util;

import android.content.Context;
import com.bc.net.ConnectionManager;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.io.IOWrapper;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import java.util.List;

public class RemoteSession extends ConnectionManager {
    private final transient IOWrapper<List<String>> sessionCookies;

    public RemoteSession(Context context) {
        this.sessionCookies = IOWrapper.getObjectInstance();
        this.sessionCookies.setContext(context);
        this.sessionCookies.setFilename(FileIO.getSessionCookiesFilename());
        setAddCookies(true);
        setGetCookies(true);
        PropertiesManager props = App.getPropertiesManager(context);
        setReadTimeout(props.getInt(PropertyName.readTimeoutMillis));
        setConnectTimeout(props.getInt(PropertyName.connectTimeoutMillis));
    }

    public void setCookies(List<String> cookies) {
        this.sessionCookies.setTarget(cookies);
    }

    public List<String> getCookies() {
        return (List) this.sessionCookies.getTarget();
    }
}
