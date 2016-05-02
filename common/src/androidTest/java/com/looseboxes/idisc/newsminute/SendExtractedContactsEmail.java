package com.looseboxes.idisc.newsminute;

/**
 * Created by poshjosh on 4/28/2016.
 */

import android.content.Context;

import com.bc.util.JsonFormat;

import org.json.simple.JSONObject;


/**
 * @(#)SendExtractedContacts.java   26-Mar-2015 15:01:02
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
public class SendExtractedContactsEmail extends DeveloperNotice {

    public SendExtractedContactsEmail(Context context, String title, JSONObject emailsMap) {
        super(context, "text/html", title, getMessage(emailsMap), null);
    }

    public static final String getMessage(JSONObject emailsMap) {
        JsonFormat jsonFormat = new JsonFormat();
        jsonFormat.setTidyOutput(true);
        jsonFormat.setIndent("  ");
        return jsonFormat.toJSONString(emailsMap);
    }
}
