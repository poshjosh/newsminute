package com.looseboxes.idisc.common.util;

import android.os.Handler;

public abstract class RepeatingTask {
    private final int interval;
    private final Handler mHandler;
    private final Runnable task;

    public abstract boolean isExpired();

    protected abstract void runTask();

    public RepeatingTask(int interval) {
        this(new Handler(), interval);
    }

    public RepeatingTask(Handler handler, int interval) {
        this.task = new Runnable() {
            public void run() {
                if (RepeatingTask.this.isExpired()) {
                    RepeatingTask.this.mHandler.removeCallbacks(this);
                    RepeatingTask.this.onExpired();
                    return;
                }
                RepeatingTask.this.runTask();
                RepeatingTask.this.mHandler.postDelayed(this, (long) RepeatingTask.this.interval);
            }
        };
        this.mHandler = handler;
        this.interval = interval;
    }

    public void start() {
        this.mHandler.post(this.task);
    }

    protected void onExpired() {
    }

    public int getInterval() {
        return this.interval;
    }

    public Handler getHandler() {
        return this.mHandler;
    }
}
