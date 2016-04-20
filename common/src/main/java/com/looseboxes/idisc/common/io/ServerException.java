package com.looseboxes.idisc.common.io;

public class ServerException extends Exception {
    private Object serverResponse;

    public ServerException(Object serverResponse) {
        this.serverResponse = serverResponse;
    }

    public ServerException(String msg, Object serverResponse) {
        super(msg);
        this.serverResponse = serverResponse;
    }

    public Object getServerResponse() {
        return this.serverResponse;
    }

    public void setServerResponse(Object serverResponse) {
        this.serverResponse = serverResponse;
    }
}
