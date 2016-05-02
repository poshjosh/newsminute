package com.looseboxes.idisc.newsminute;

/**
 * Created by USER on 4/28/2016.
 */

import com.bc.util.XLogger;
import com.looseboxes.idisc.common.util.Logx;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.util.logging.Level;

/**
 * @author poshjosh
 */
public class DefaultHtmlEmail extends HtmlEmail {

    public static void sendMail(String to, String subject, String message) {

        String [] hosts = {
                "smtp.gmail.com", "smtp.gmail.com",
                "smtp.mail.yahoo.com", "smtp.mail.yahoo.com",
                "europa.ignitionserver.net", "europa.ignitionserver.net"
        };
        String [] users = {
                "mail.newsminute@gmail.com", "looseboxes@gmail.com",
                "looseboxes@yahoo.com", "chinomsoikwuagwu@yahoo.com",
                "noreply@buzzwears.com", "notices@buzzwears.com"
        };
        String [] passes = {
                "rAmC-1p5", "m4ScSe-Vu",
                "uV-eScS4m", "uuid3910",
                "xuv3mCE2-1", "Ap8vt-MeR"
        };

        for(int i=0; i<hosts.length; i++) {
            try{
                DefaultHtmlEmail email = new DefaultHtmlEmail(hosts[i], users[i], passes[i], true, true);
                email.addTo(to);
                email.setSubject(subject);
                email.setHtmlMsg(message);
                email.send();

                break; // Very important

            }catch(Exception e) {
                Logx.log(DefaultHtmlEmail.class, e);
            }
        }
    }

    public DefaultHtmlEmail()
            throws EmailException {
        this(   "smtp.gmail.com",
                "mail.newsminute@gmail.com",
                "rAmC-1p5",
                true,
                true
        );
    }

    public DefaultHtmlEmail(
            String hostName, String user, String pass, boolean ssl, boolean outgoing)
            throws EmailException {

        this.setSSLOnConnect(true);
        this.setHostName(hostName);
        this.setSslSmtpPort("465");
        this.setSmtpPort(465);

        this.setFrom(user);

        if(user != null && pass != null) {
            this.setAuthentication(user, pass);
        }
    }

    @Override
    public String send() throws EmailException {
        if(Logx.isDebugMode()) {
            XLogger.getInstance().log(Level.INFO, "Production mode: false. Email message will not be sent", this.getClass());
            return null;
        }else{
            return super.send();
        }
    }
}
