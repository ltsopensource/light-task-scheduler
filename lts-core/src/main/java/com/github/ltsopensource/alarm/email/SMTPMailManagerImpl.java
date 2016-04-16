package com.github.ltsopensource.alarm.email;

import com.github.ltsopensource.core.commons.utils.StringUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SMTPMailManagerImpl implements MailManager {

    private String userName;

    private String password;

    private String adminAddress;

    private Properties properties;

    public SMTPMailManagerImpl(String host, String userName, String password,
                               String adminAddress, boolean sslEnabled) {
        this(host, "", userName, password, adminAddress, sslEnabled);
    }

    public SMTPMailManagerImpl(String host, String port, String userName, String password,
                               String adminAddress, boolean sslEnabled) {
        this.userName = userName;
        this.password = password;
        this.adminAddress = adminAddress;

        properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        if (!StringUtils.isEmpty(host)) {
            properties.setProperty("mail.smtp.host", host);
        }
        if (!StringUtils.isEmpty(port)) {
            properties.setProperty("mail.smtp.port", port);
        }
        if (!StringUtils.isEmpty(userName)) {
            properties.setProperty("mail.smtp.security", "true");
        } else {
            properties.setProperty("mail.smtp.security", "false");
        }

        if (sslEnabled) {
            properties.setProperty("mail.smtp.starttls.enable", "true");
        }
    }

    private Authenticator getAuthenticator() {
        if (!StringUtils.isEmpty(userName)) {
            return new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password);
                }
            };
        } else {
            return null;
        }
    }

    public void send(String to, String title, String message) throws Exception {
        Session session = Session.getDefaultInstance(properties, getAuthenticator());
        // Create a default MimeMessage object.
        MimeMessage mimeMessage = new MimeMessage(session);
        // Set From: header field of the header.
        mimeMessage.setFrom(new InternetAddress(adminAddress));
        // Set To: header field of the header.
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        // Set Subject: header field
        mimeMessage.setSubject(title);
        // Now set the actual message
        mimeMessage.setText(message);
        // Send message
        Transport.send(mimeMessage);
    }
}