package com.looseboxes.idisc.common;

import android.content.Context;

import com.looseboxes.idisc.common.asynctasks.DefaultReadTask;

import java.util.Collections;

public class RemoteLog {

    /* renamed from: com.looseboxes.idisc.common.RemoteLog.1 */
    static class AnonymousClass1 extends DefaultReadTask<Boolean> {
        final /* synthetic */ Context val$context;

        AnonymousClass1(Context context) {
            this.val$context = context;
        }

        public void onSuccess(Boolean download) {
        }

        public String getOutputKey() {
            return "log";
        }

        public String getLocalFilename() {
            throw new UnsupportedOperationException();
        }

        public Context getContext() {
            return this.val$context;
        }

        public String getErrorMessage() {
            return "";
        }
    }

    public static void logInstallationError(Context context) {
        log(context, "1");
    }

    public static void logServiceUnavailable(Context context) {
        log(context, "2");
    }

    public static void log(Context context, String logTypeId) {
        DefaultReadTask<Boolean> task = new AnonymousClass1(context);
        task.setOutputParameters(Collections.singletonMap("id", logTypeId));
        task.setNoUI(true);
        task.execute();
    }
}
