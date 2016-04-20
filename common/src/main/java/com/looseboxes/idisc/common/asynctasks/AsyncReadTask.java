package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.io.NoInternetException;
import com.looseboxes.idisc.common.io.ServerException;
import com.looseboxes.idisc.common.io.StreamReader;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Util;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;

public abstract class AsyncReadTask extends AsyncTask<String, Object, String> {
    private static long lastDisplayTime;
    private Object download;
    private Exception exception;
    private boolean noUI;
    private ProgressStatus progressStatus;
    private Object status;

    // Modifier static is redundant for inner enums
    public enum ProgressStatus {
        beginningReadTask("Beginning read task"),
        preparingToRead("Preparing to read"),
        connectingToSource("Connecting to source"),
        doneConnectingToSource("Done connecting to source"),
        readingFromSource("Reading from source"),
        doneReadingFromSource("Done reading from source"),
        processingRead("Processing read"),
        doneProcessingRead("Done processing read"),
        completedReadTask("Completed read task");
        
        private final String LABEL;

        private ProgressStatus(String label) {
            this.LABEL = label;
        }

        public String toString() {
            return this.LABEL == null ? super.toString() : this.LABEL;
        }
    }

    public abstract Context getContext();

    public abstract String getErrorMessage();

    protected abstract InputStream openStream(String str) throws IOException;

    protected abstract void processRead(String str) throws ParseException, ServerException;

    public void reset() {
        this.progressStatus = null;
        this.status = null;
        this.exception = null;
        this.download = null;
    }

    public void displayMessage(Object message, int length) {
        Popup.show(getContext(), message, length);
    }

    public boolean isStarted() {
        return this.progressStatus != null;
    }

    public boolean isReadCompleted() {
        return this.progressStatus != null && this.progressStatus.compareTo(ProgressStatus.doneReadingFromSource) >= 0;
    }

    public boolean isProcessingReadCompleted() {
        return this.progressStatus != null && this.progressStatus.compareTo(ProgressStatus.completedReadTask) >= 0;
    }

    public boolean isCompleted() {
        return isProcessingReadCompleted();
    }

    protected void onPreExecute() {
        updateProgress(ProgressStatus.beginningReadTask);
    }

    protected java.lang.String doInBackground(java.lang.String... urls) {
        if(this.isCancelled()) {
            return null;
        }

        if(this.hasError()) {
            return null;
        }

        this.updateProgress(ProgressStatus.preparingToRead);

// Avoid putting UI code in doInBackground, including classes in the method
        StreamReader sr = new StreamReader();

        String download = null;
        InputStream in = null;
        try
        {

            this.updateProgress(ProgressStatus.connectingToSource);

            in = openStream(urls[0]);

            this.updateProgress(ProgressStatus.doneConnectingToSource);

            if(in != null) {

                this.updateProgress(ProgressStatus.readingFromSource);

                download = sr.readContents(in);
// Avoid putting UI code in doInBackground, including classes in the method
// Logx.log(null, this.getClass(), "Download:\n"+update);
                this.updateProgress(ProgressStatus.doneReadingFromSource);
            }
        }catch(Exception e) {
            this.updateException(e, download);
        }finally{
            sr.close(in);
        }

        return download;
    }

    public void updateProgress(Object status) {
        publishProgress(new Object[]{status});
        if (status instanceof ProgressStatus) {
            this.progressStatus = (ProgressStatus) status;
        } else {
            this.status = status;
        }
    }

    protected void onPostExecute(java.lang.String contents) {

        Logx.log(Log.VERBOSE, this.getClass(), "Progress: {0}\nContents:\n{1}", progressStatus, contents);

        try{

            if(this.hasError()) {

                this.processError();

            }else{

                if(accept(contents)) {

                    this.updateProgress(ProgressStatus.processingRead);

                    this.processRead(contents);

                    this.updateProgress(ProgressStatus.doneProcessingRead);

                }else{

                    throw new ParseException(ParseException.ERROR_UNEXPECTED_CHAR, contents);
                }
            }
        }catch(Exception e) {
            this.updateException(e, contents);
            this.processError();
        }finally {
            this.updateProgress(ProgressStatus.completedReadTask);
        }
    }

    public boolean accept(String contents) {
        return contents != null;
    }

    protected void updateException(Exception e, Object download) {
        if (e == null) {
            throw new NullPointerException();
        }
        this.exception = e;
        this.download = download;
    }

    protected boolean hasError() {
        return this.exception != null;
    }

    protected void processError() {
        try {
            _processError();
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
    }

    private void _processError() {
        if (hasError()) {
            Object toDisplay;
            Object obj = null;
            if (this.exception instanceof NoInternetException) {
                if (System.currentTimeMillis() - lastDisplayTime > 30000) {
                    toDisplay = getContext().getString(R.string.err_noconnection);
                    lastDisplayTime = System.currentTimeMillis();
                } else {
                    toDisplay = null;
                }

            } else {
                StringBuilder msg = new StringBuilder();
                toDisplay = msg.subSequence(0, appendExceptionMessage(msg));
                StringBuilder toLog = msg;
                if (Logx.isLoggable(3)) {
                    int popupRepeats = Logx.getLogSettings().getPopupRepeats();
                    for (int i = 0; i < popupRepeats; i++) {
                        displayMessage(msg, 1);
                    }
                }
            }
            if (!(toDisplay == null || isNoUI() || toDisplay.toString().isEmpty())) {
                displayMessage(toDisplay, 0);
            }
            if (obj != null) {
                Logx.log(5, getClass(), obj);
            }
        }
    }

    public int appendExceptionMessage(StringBuilder msg) {
        appendMessageTitle(msg);
        int titleLen = msg.length();
        appendTaskParameters(msg);
        appendExceptionToMessage(msg);
        appendDownloadToMessage(msg);
        return titleLen;
    }

    protected void appendMessageTitle(StringBuilder msg) {
        msg.append(getErrorMessage());
    }

    protected void appendTaskParameters(StringBuilder msg) {
    }

    protected void appendExceptionToMessage(StringBuilder msg) {
        if (this.exception != null) {
            msg.append('\n');
            Util.appendStackTrace(this.exception, "\n", msg);
        }
    }

    protected void appendDownloadToMessage(StringBuilder msg) {
        if (this.download != null) {
            int len;
            if (Logx.isLoggable(Log.VERBOSE)) {
                len = Integer.MAX_VALUE;
            } else if (Logx.isLoggable(Log.DEBUG)) {
                len = 1000;
            } else {
                len = 200;
            }
            String downloadStr = this.download.toString();
            if (len > downloadStr.length()) {
                len = downloadStr.length();
            }
            msg.append("\n");
            msg.append(downloadStr, 0, len);
        }
    }

    public boolean isNoUI() {
        return this.noUI;
    }

    public void setNoUI(boolean noUI) {
        this.noUI = noUI;
    }

    public ProgressStatus getProgressStatus() {
        return this.progressStatus;
    }

    public Object getStatusObject() {
        return this.status;
    }

    public int getProgressPercent() {
        return this.progressStatus == null ? 0 : getProgressPercent(this.progressStatus);
    }

    public int getProgressPercent(ProgressStatus status) {
        int percent;
        if(status != null) {
            switch(status) {
                case beginningReadTask: percent = 0; break;
                case preparingToRead: percent = 5; break;
                case connectingToSource: percent = 10; break;
                case doneConnectingToSource:
                case readingFromSource: percent = 70; break;
                case doneReadingFromSource: percent = 90; break;
                case processingRead: percent = 93; break;
                case doneProcessingRead: percent = 95; break;
                case completedReadTask: percent = 100; break;
                default:
                    throw new UnsupportedOperationException("Unexpected value for "+ProgressStatus.class.getName()+": "+progressStatus);
            }
        }else{
            percent = 0;
        }
        return percent;
    }

    public String getMessageFor(ProgressStatus status) {
        String msg;
        switch(status) {
            case beginningReadTask:
                msg = "Unexpected error"; break;
            case preparingToRead:
                msg = "Unexpected error"; break;
            case connectingToSource:
                msg = "Error connecting to source"; break;
            case doneConnectingToSource:
                msg = "Failed to read from source"; break;
            case readingFromSource:
                msg = "Error reading from source"; break;
            case doneReadingFromSource:
            case processingRead:
                msg = "Unexpected contents from source"; break;
            case doneProcessingRead:
                msg = "Unexpected error"; break;
            case completedReadTask:
                msg = "Unexpected error"; break;
            default:
                throw new UnsupportedOperationException("Unexpected value for "+ProgressStatus.class.getName()+": "+progressStatus);
        }
        return msg;
    }

    public Exception getException() {
        return this.exception;
    }

    public Object getDownload() {
        return this.download;
    }
}
