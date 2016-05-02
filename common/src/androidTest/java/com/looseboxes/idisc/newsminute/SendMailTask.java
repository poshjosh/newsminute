package com.looseboxes.idisc.newsminute;

/**
 * Created by poshjosh on 4/28/2016.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bc.util.XLogger;
import com.looseboxes.idisc.common.util.Logx;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


/**
 * @(#)SendMailTask.java   09-Mar-2015 00:34:58
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
public abstract class SendMailTask extends AsyncTask<String, Object, Void> {

    public SendMailTask() { }

    public abstract String getUser();

    public abstract char[] getPassword();

    public abstract String getSubject();

    public abstract String getMessage();

    public abstract String getContentType();

    public abstract Set<String> getAttachments();

    public abstract Context getContext();

    @Override
    protected Void doInBackground(String... recipients) {
        try{
            this.publishProgress("Sending message");
            this.sendMessage(this.getUser(), new String(this.getPassword()), recipients,
                    this.getSubject(), this.getMessage(), this.getContentType(), this.getAttachments());
            this.publishProgress("Done sending message");
        }catch(MessagingException e) {
            this.publishProgress(e);
        }catch(RuntimeException e) {
            this.publishProgress(e);
        }finally{
            Arrays.fill(this.getPassword(), '\0');
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        if(values == null || values.length == 0) {
            return;
        }
        if(values[0] instanceof Exception) {
            Logx.log(this.getClass(), (Exception)values[0]);
        }else{
            Logx.log(Log.DEBUG, this.getClass(), values[0]);
        }
    }

    public void sendMessage(String user, String password, String recipients[],
                            String subject, String message, String contentType,
                            Set<String> attachments) throws MessagingException {

        sendMessage(user, password, ((Address []) (toAddresses(recipients))), subject, message, contentType, attachments);
    }

    public InternetAddress [] toAddresses(String [] recipients) throws javax.mail.internet.AddressException {
        InternetAddress[] addresses = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addresses[i] = new InternetAddress(recipients[i]);
        }
        return addresses;
    }

    public void sendMessage(String user, String password, Address recipients[],
                            String subject, String message, String contentType,
                            Set<String> attachments) throws MessagingException {


        Session session = this.getSession(user, password);

        sendMessage(session, user, recipients,subject, message, contentType, attachments);
    }

    private Session getSession(final String username, final String password) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        return Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendMessage(Session session, String user, String recipients[],
                            String subject, String message, String contentType,
                            Set<String> attachments) throws MessagingException {

        sendMessage(session, user, ((Address []) (toAddresses(recipients))), subject, message, contentType, attachments);
    }

    public void sendMessage(Session session, String user, Address [] addressTo,
                            String subject, String message, String contentType,
                            Set<String> attachments) throws MessagingException {

        MimeMessage mimeMessage;

        synchronized (this) {
            mimeMessage = new MimeMessage(session);
        }

        InternetAddress addressFrom = new InternetAddress(user);

        mimeMessage.setFrom(addressFrom);

        mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, addressTo);

        // Set the Subject
        mimeMessage.setSubject(subject);

        boolean hasAttachments = (attachments != null && !attachments.isEmpty());

        if(!hasAttachments) {

            mimeMessage.setContent(message, contentType);

        }else{

            Multipart multipart = new MimeMultipart("related");

            // first part  - the html
            BodyPart messageBodyPart = new MimeBodyPart();

            messageBodyPart.setContent(message, contentType);

            multipart.addBodyPart(messageBodyPart);

            // second part  - the image attachment
            this.addBodyParts(multipart, attachments);

            // put everything together
            mimeMessage.setContent(multipart);
        }

        sendMessage(mimeMessage);
    }

    public void sendMessage(javax.mail.Message message) throws MessagingException {

        Transport.send(message);
    }

    public int addBodyParts(Multipart multipart, Set<String> links) {

        if(links == null || links.isEmpty()) {
            return -1;
        }

        int added = 0;

        Iterator<String> iter = links.iterator();

        while(iter.hasNext()) {

            String link = iter.next();

            try{
                MimeBodyPart mimeBodyPart = this.getBodyPart(link);

                if(mimeBodyPart == null) {

                    multipart.addBodyPart(mimeBodyPart);

                    ++added;
                }
            }catch(MessagingException e) {
                XLogger.getInstance().logSimple(Level.WARNING, this.getClass(), e);
            }
        }

        return added;
    }

    public MimeBodyPart getBodyPart(String link) throws MessagingException {
        DataSource ds = this.getDataSource(link);
        if(ds == null) {
            return null;
        }
        DataHandler dh = new DataHandler(ds);
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setDataHandler(dh);
        mimeBodyPart.setHeader("Content-ID", "<"+this.getFileName(link) +">");
        return mimeBodyPart;
    }

    public DataSource getDataSource(String source) {
        try{
            DataSource ds;
            if(this.isURL(source)) {
                ds = new URLDataSource(new URL(source));
            }else{
                ds = new FileDataSource(source);
            }
            return ds;
        }catch(MalformedURLException e) {
            XLogger.getInstance().logSimple(Level.WARNING, this.getClass(), e);
            return null;
        }
    }

    public String getSrcAttribute(String imageLink, boolean useCID) {

        if(imageLink == null) {
            return null;
        }

        String src;
        if(useCID) {
            src = "cid:"+this.getFileName(imageLink);
        }else{
            src = imageLink;
        }

        return src;
    }

    public boolean isURL(String link) {
        return link.startsWith("http://");
    }

    /**
     * Mirrors logic of method {@link File#getName()}.
     * Use this method if its not necessary to create a new File object.
     * @param path The path to the file whose name is required
     * @return The name of the file at the specified path
     */
    private String getFileName(String path) {
        String output = getFileName(path, File.separatorChar);
        if(output == null) {
            output = getFileName(path, '/');
            if(output == null) {
                output = getFileName(path, '\\');
            }
        }
        return output;
    }

    private String getFileName(String path, char separatorChar) {
        int index = path.lastIndexOf(separatorChar);
        if (index == -1 || index == 0) return null;
        return path.substring(index + 1);
    }
}
