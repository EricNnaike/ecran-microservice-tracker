package com.appService2.appService2.service;


import com.appService2.appService2.entity.User;
import com.appService2.appService2.entity.VerificationToken;
import com.appService2.appService2.repository.VerificationTokenRepository;
import com.appService2.appService2.security.JwtTokenProvider;
import com.appService2.appService2.util.Util;
import com.sun.mail.smtp.SMTPTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import static com.appService2.appService2.constants.EmailConstant.*;


@Service
public class EmailService {

    @Value("${token.expire}")
    private int expire;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    public void sendEmail(String firstName, String email, User user) throws MessagingException {
        Message message = createEmail(firstName, email, user);
        SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
    }

    private Message createEmail(String firstName, String email, User user) throws MessagingException {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        Date now = new Date();
        int jwtExpirationInMillis = 2 * 6 * expire;
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMillis);
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);
        message.setText("Hello " + firstName + ", \n\n You've successfully created an account " +
                "\n\n  Regards\n Please click the link below to activate your account" +
                "\n\n http://localhost:8081/api/v1/auth/verifyRegistration?token=" + token);
        verificationToken.setToken(token);
        verificationToken.setExpirationTime(expiryDate);
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);

        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    public void sendResetPasswordEmail(String firstName, String email, User user) throws MessagingException {
        Message message = createResetPasswodEmail(firstName, email, user);
        SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
    }

    private Message createResetPasswodEmail(String firstName, String email, User user) throws MessagingException {
        String token = jwtTokenProvider.generatePasswordResetToken(email);
        VerificationToken verificationToken = new VerificationToken();
        Date now = new Date();
        int jwtExpirationInMillis = 2 * 6 * expire;
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMillis);
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT2);
        message.setText("Hello " + firstName + ", \n\n This is the link to reset your password " +
                "\n\n  Regards\n Please click the link below to reset your password" +
                "\n\n http://localhost:8081/api/v1/auth/resetPassword?token=" + token);
        verificationToken.setToken(token);
        verificationToken.setExpirationTime(expiryDate);
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);

        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Session getEmailSession() {
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_ENABLE, true);
        properties.put(SMTP_STARTTLS_REQUIRED, true);
        return Session.getInstance(properties, null);
    }
}
