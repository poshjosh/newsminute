package com.looseboxes.idisc.newsminute;

/**
 * Created by poshjosh on 4/28/2016.
 */

import android.content.Context;
import android.util.Log;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.asynctasks.DefaultReadTask;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.Util;

import java.util.List;
import java.util.Set;


/**
 * @(#)DeveloperUrgentNotice.java   18-Mar-2015 18:02:23
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class DeveloperNotice extends SendMailTask {

    private final Context context;

    private final String subject;

    private final String message;

    private final String contentType;

    private final Set<String> attachments;

    public DeveloperNotice(Context context, String msg, Exception e, DefaultReadTask task, Object download) {

        StringBuilder builder = new StringBuilder();
        builder.append("Message: ").append(msg);
        builder.append("<br/><br/>"); // Content type = text/html
        if(e != null) {
            builder.append("Exception trace:<br/>"); // Content type = text/html
            Util.appendStackTrace(e, "<br/>", builder);
        }

        builder.append("<br/><br/>Request URL: ").append(task.getTarget());
        builder.append("<br/><br/>Output parameters: ").append(task.getOutputParameters());
        builder.append("<br/><br/>Server response:<br/><pre><code>").append(download).append("</code></pre>");

        this.context = context;
        this.subject = "News Minute URGENT Notice";
        this.message = builder.toString();
        this.contentType = "text/html";
        this.attachments = null;
    }

    public DeveloperNotice(Context context, Exception e) {
        this(context, "text/plain", "Error ("+e+") in News Minute App", Util.getStackTrace(e).toString(), null);
    }

    public DeveloperNotice(Context context, String title, Exception e, Set<String> attachments) {
        this(context, "text/plain", title, Util.getStackTrace(e).toString(), attachments);
    }

    public DeveloperNotice(Context context, String contentType, String title, String msg, Set<String> attachments) {
        this.context = context;
        this.contentType = contentType;
        this.subject = title;
        this.message = msg;
        this.attachments = attachments;
    }

    public DeveloperNotice(Context context, String subject, String message) {
        this(context, "text/html", subject, message, null);
    }

    public void execute() {
        List<String> list = App.getPropertiesManager(this.getContext()).getList(PropertiesManager.PropertyName.developerEmails);
        if(list == null || list.isEmpty()) {
            Logx.log(Log.WARN, this.getClass(), "Failed to access developer email address list. Following Email will not be sent:\n"+this.message);
        }else{
            this.execute(list.toArray(new String[0]));
        }
    }

    @Override
    public String getUser() {
        return (String)App.getPropertiesManager(this.getContext()).getList(PropertiesManager.PropertyName.developerEmails).get(0);
    }

    @Override
    public char[] getPassword() {
        return ((String)App.getPropertiesManager(this.getContext()).getList(PropertiesManager.PropertyName.developerPasswords).get(0)).toCharArray();
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Set<String> getAttachments() {
        return attachments;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

