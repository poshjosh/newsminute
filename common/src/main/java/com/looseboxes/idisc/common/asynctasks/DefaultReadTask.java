package com.looseboxes.idisc.common.asynctasks;

import android.util.Log;

import com.looseboxes.idisc.common.io.ServerException;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.RemoteSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class DefaultReadTask<T> extends BaseReadTask {

    public interface ResponseParser<I, O> {
        O parse(I i) throws ParseException;
    }

    private class ServerResponseParser extends JSONParser implements ResponseParser<String, T> {
        private ServerResponseParser() {
        }

        public boolean isPositiveCompletion() {
            RemoteSession session = DefaultReadTask.this.getSession();
            if (session == null) {
                return true;
            }
            int code = session.getResponseCode();
            if (code <= 0 || code >= 300) {
                return false;
            }
            return true;
        }

        public T parse(String readString) throws ParseException, ClassCastException {
            Class cls = getClass();
            Logx.log(Log.DEBUG, cls, "Downloaded: {0} chars", readString==null?"null":readString.length());

            if (readString != null) {
                Logx.log(Log.VERBOSE, getClass(), "Read:\n{0}", readString);
            }
            if (readString != null) {
                Object read;
                Object obj = super.parse(readString);
                if (obj instanceof JSONObject) {
                    read = ((JSONObject)obj).get(DefaultReadTask.this.getOutputKey());
                } else {
                    read = obj;
                }
                if (read != null) {
                    return (T)read;
                }
                throw new ParseException(ParseException.ERROR_UNEXPECTED_EXCEPTION);
            }
            throw new ParseException(0, readString);
        }
    }

    public abstract void onSuccess(T t);

    public ResponseParser<String, T> getResponseParser() {
        return new ServerResponseParser();
    }

    protected void processRead(String download) throws ParseException, ServerException {
        if (isPositiveCompletion()) {
            onSuccess(getResponseParser().parse(download));
            return;
        }
        throw new ServerException(download);
    }
}
