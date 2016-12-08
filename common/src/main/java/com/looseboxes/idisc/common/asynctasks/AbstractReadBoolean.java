package com.looseboxes.idisc.common.asynctasks;

import android.content.Context;

/**
 * Created by Josh on 9/23/2016.
 */
public abstract class AbstractReadBoolean extends AbstractReadTask<Boolean> {

    public AbstractReadBoolean(Context context, String errorMessage) {
        super(context, errorMessage);
    }

    @Override
    protected Boolean toSuccessFormat(Object read) {
        if(read instanceof Boolean) {
            return (Boolean)read;
        }else {
            return read.toString().toLowerCase().trim().equals("success");
        }
    }
}
