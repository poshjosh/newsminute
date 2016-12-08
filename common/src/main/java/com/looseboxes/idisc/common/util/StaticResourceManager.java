package com.looseboxes.idisc.common.util;

import android.content.Context;

import com.bc.android.core.io.IOWrapper;
import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.asynctasks.DownloadToLocalCache;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class StaticResourceManager<T> {

    private Lock lock;

    private DownloadInterval downloadInterval;

    private String outputkey;

    private boolean noUI;

    protected abstract IOWrapper<T> createIOWrapper(Context context);

    public StaticResourceManager(Context context, long updateIntervalMillis, boolean noUI,
                                 String lastDownloadTimePreferenceName, String outputkey) {
        Util.requireNonNull(context);
        this.downloadInterval = new DownloadInterval(context, lastDownloadTimePreferenceName, updateIntervalMillis);
        this.outputkey = outputkey;
        this.noUI = noUI;
        this.lock = new ReentrantLock();
    }

    public void destroy() {
        this.downloadInterval.destroy();
        this.downloadInterval = null;
        this.outputkey = null;
        this.lock = null;
    }

    public void update(boolean ignoreSchedule) {
        if (ignoreSchedule || this.downloadInterval.isNextDownloadDue()) {
            setLastDownloadTime(System.currentTimeMillis());
            IOWrapper ioWrapper = this.getIOManager();
            DownloadToLocalCache downloader = new DownloadToLocalCache(this.getContext(), this.outputkey, ioWrapper);
            downloader.setNoUI(this.noUI);
            downloader.execute();
        }
    }

    public void setTarget(T target) {
        try{
            lock.lock();
            this.getIOManager().setTarget(target);
        }finally{
            lock.unlock();
        }
    }

    public T getTarget() {
        T target;
        try {
            lock.lock();
            target = this.getIOManager().getTarget();
        }finally{
            lock.unlock();
        }
        return target;
    }

    private WeakReference<IOWrapper<T>> _$ioref;
    public IOWrapper<T> getIOManager() {
        IOWrapper<T> tgt;
        if(_$ioref == null || (tgt = _$ioref.get()) == null) {
            tgt = this.createIOWrapper(this.downloadInterval.getContext());
            _$ioref = new WeakReference<>(tgt);
        }
        return tgt;
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

    public boolean isNoUI() {
        return noUI;
    }

    public void setNoUI(boolean noUI) {
        this.noUI = noUI;
    }
}
