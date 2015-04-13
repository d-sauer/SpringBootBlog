package com.davorsauer.service;

import com.davorsauer.commons.Logger;
import com.davorsauer.config.BlogSendMailProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
@Lazy
public class SendMailService implements Logger {

    @Autowired
    private BlogSendMailProperties properties;


    public void postConstruct() {
        trace("init");
    }

    /**
     * Send mail over gmail
     *
     * @throws Exception
     */
    public void sendMail(String fromEmail, String fromName, String subject, String msg) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", properties.getHost());
        props.put("mail.smtp.port", properties.getPort());
        props.put("mail.debug", properties.isDebug());
        props.put("mail.smtp.auth", properties.isAuth());
        props.put("mail.smtp.starttls.enable", properties.isStarttls());
        props.put("mail.smtp.localhost", properties.getLocalhost());

        Session s = Session.getInstance(props, null);
        s.setDebug(true);

        MimeMessage message = new MimeMessage(s);

        InternetAddress from = new InternetAddress(fromEmail, fromName);
        InternetAddress to = new InternetAddress("davor.sauer@gmail.com");

        message.setSentDate(new Date());
        message.setFrom(from);
        message.addRecipient(Message.RecipientType.TO, to);

        message.setSubject("[portfolio] :: " + subject);
        message.setContent(msg, "text/plain");

        Transport tr = s.getTransport("smtp");
        tr.connect(properties.getHost(), properties.getUserFrom(), properties.getPasswordFrom());
        message.saveChanges();
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();
    }

}

