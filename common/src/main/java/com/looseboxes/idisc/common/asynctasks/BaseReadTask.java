package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.io.NoInternetException;
import com.looseboxes.idisc.common.io.ServerException;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.RemoteSession;
import com.looseboxes.idisc.common.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;

public abstract class BaseReadTask extends AsyncReadTask {
    private static int failedNetworkAttempts;
    private static long lastNetworkAccessTime;
    private Map<String, String> outputParameters;
    private RemoteSession ses_accessViaGetter;

    /* renamed from: com.looseboxes.idisc.common.asynctasks.BaseReadTask.1 */
    class AnonymousClass1 extends RemoteSession {
        AnonymousClass1(Context x0) {
            super(x0);
        }

        protected void log(String msg, Exception e) {
        }

        protected void log(Level level, String fmt, Object val_0) {
        }

        protected void log(Level level, String fmt, Object val_0, Object val_1) {
        }

        protected void log(Level level, String fmt, Object val_0, Object val_1, Object val_2) {
        }

        protected void log(Level level, String fmt, Object val_0, Object val_1, Object val_2, Object val_3) {
        }
    }

    public abstract String getLocalFilename();

    public abstract String getOutputKey();

    protected void onProgressUpdate(Object... values) {
        if (Logx.isLoggable(3)) {
            int popupRepeats = Logx.getLogSettings().getPopupRepeats();
            StringBuilder builder;
            int i;
            if (values[0] == ProgressStatus.beginningReadTask) {
                builder = new StringBuilder();
                builder.append(values[0]).append('\n');
                appendTaskParameters(builder);
                for (i = 0; i < popupRepeats; i++) {
                    displayMessage(builder, 0);
                }
            } else if (values[0] == ProgressStatus.completedReadTask) {
                builder = new StringBuilder();
                if (isPositiveCompletion()) {
                    builder.append("SUCCESS\n");
                } else {
                    builder.append("ERROR\n");
                    builder.append(getDownload());
                }
                appendTaskParameters(builder);
                for (i = 0; i < popupRepeats; i++) {
                    displayMessage(builder, 0);
                }
            }
        }
    }

    protected void appendTaskParameters(StringBuilder msg) {
        msg.append("\nTarget url was: ").append(getTarget());
        msg.append("\nOutput params: ").append(getOutputParameters());
    }

    public AsyncTask<String, Object, String> execute() {
        String target = getTarget();
        Logx.debug(getClass(), "Executing: {0}", target);
        return execute(new String[]{target});
    }

    public String getTarget() {
        if (!isRemote()) {
            return getLocalFilename();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(App.getPropertiesManager(getContext()).getString(PropertyName.appServiceUrl));
        builder.append('/').append(getOutputKey());
        return builder.toString();
    }

    protected void onPreExecute() {
        if (isRemote()) {
            try {
                checkNetwork();
            } catch (Exception e) {
                updateException(e, null);
            }
        }
    }

    protected InputStream openStream(String urlString) throws IOException {
        if (isRemote()) {
            return openRemoteStream(urlString);
        }
        return openLocalStream(urlString);
    }

    protected InputStream openRemoteStream(String urlString) throws IOException {
        try {
            checkNetwork();
            failedNetworkAttempts = 0;
            lastNetworkAccessTime = System.currentTimeMillis();
            return getInputStream(urlString);
        } catch (IOException e) {
            throw e;
        } catch (Exception e2) {
            throw new IOException(e2);
        }
    }

    protected InputStream getInputStream(String urlString) throws MalformedURLException, IOException {
        return getSession().getInputStreamForUrlEncodedForm(new URL(urlString), getOutputParameters(), true);
    }

    protected InputStream openLocalStream(String urlString) throws IOException {
        try {
            return getContext().openFileInput(urlString);
        } catch (IOException e) {
            throw e;
        } catch (Exception e2) {
            throw new IOException(e2);
        }
    }

    protected void appendMessageTitle(StringBuilder msg) {
        Object serverResponse;
        super.appendMessageTitle(msg);
        Exception e = getException();
        if (e instanceof ServerException) {
            serverResponse = ((ServerException) e).getServerResponse();
        } else {
            serverResponse = null;
        }
        if (serverResponse == null) {
            RemoteSession session = getSession();
            if (session != null) {
                String serverResponse2 = session.getResponseMessage();
                if (serverResponse2 == null || serverResponse2.length() <= 100) {
                    String response = serverResponse2;
                } else {
                    serverResponse = serverResponse2.substring(0, 100) + "...";
                }
            }
        }
        if (serverResponse != null) {
            msg.append('\n').append(serverResponse);
        }
    }

    public boolean isPositiveCompletion() {
        if (!isRemote()) {
            return true;
        }
        return getSession().getResponseCode() < 300;
    }

    private void checkNetwork() throws NoInternetException {
        if (!Util.isNetworkConnectedOrConnecting(getContext())) {
            failedNetworkAttempts++;
            throw new NoInternetException(getContext().getString(R.string.err_noconnection));
        }
    }

    public Map<String, String> getOutputParameters() {
        return this.outputParameters;
    }

    public void setOutputParameters(Map<String, String> outputParameters) {
        this.outputParameters = outputParameters;
    }

    public RemoteSession getSession() {
        if (isRemote() && this.ses_accessViaGetter == null) {
            this.ses_accessViaGetter = new AnonymousClass1(getContext());
        }
        return this.ses_accessViaGetter;
    }

    public boolean isRemote() {
        return true;
    }

    public static long getLastNetworkAccessTime() {
        return lastNetworkAccessTime;
    }

    public static int getFailedNetworkAttempts() {
        return failedNetworkAttempts;
    }
}
