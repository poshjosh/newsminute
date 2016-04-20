package com.looseboxes.idisc.common.util;

import android.content.Context;
import com.looseboxes.idisc.common.io.IOWrapper;

public abstract class StaticResourceManager<T> {

    private final DownloadInterval downloadInterval;

    protected abstract IOWrapper<T> createIOWrapper(Context context);

    protected abstract void update(IOWrapper<T> iOWrapper);

    public StaticResourceManager(Context context, long updateIntervalMillis, String lastDownloadTimePreferenceName) {
        if (context == null) {
            throw new NullPointerException();
        }
        this.downloadInterval = new DownloadInterval(context, lastDownloadTimePreferenceName, updateIntervalMillis);
    }

    private IOWrapper<T> _iom;
    public IOWrapper<T> getIOManager() {
        if(_iom == null) {
            _iom = this.createIOWrapper(this.getContext());
        }
        return _iom;
    }

    protected Object getTargetLock() {
        return this.getIOManager();
    }

    public void update(boolean ignoreSchedule) {
        if (ignoreSchedule || this.downloadInterval.isNextDownloadDue()) {
            setLastDownloadTime(System.currentTimeMillis());
            update(this.getIOManager());
        }
    }

    public void setTarget(T target) {
        synchronized (getTargetLock()) {
            this.getIOManager().setTarget(target);
        }
    }

    public T getTarget() {
        T target;
        synchronized (getTargetLock()) {
            target = this.getIOManager().getTarget();
        }
        return target;
    }

    public void setLastDownloadTime(long lastDownloadTime) {
        this.downloadInterval.setLastDownloadTime(lastDownloadTime);
    }

    public long getLastDownloadTime() {
        return this.downloadInterval.getLastDownloadTime();
    }

    public Context getContext() {
        return this.downloadInterval.getContext();
    }

    public long getUpdateIntervalMillis() {
        return this.downloadInterval.getUpdateIntervalMillis();
    }
}
