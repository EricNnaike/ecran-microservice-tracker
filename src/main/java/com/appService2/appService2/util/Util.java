package com.appService2.appService2.util;


import com.appService2.appService2.dto.UserRequest;
import com.appService2.appService2.dto.UserResponse;
import com.appService2.appService2.entity.User;
import com.appService2.appService2.entity.VerificationToken;
import com.appService2.appService2.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class Util {


    private final JavaMailSender mailSender;

    private final EmailService emailService;

    public Util(JavaMailSender mailSender, EmailService emailService) {
        this.mailSender = mailSender;
        this.emailService = emailService;
    }


    public User requestToUser(UserRequest userRequest) {
        return User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .createdAt(LocalDateTime.now())
                .modifyAt(LocalDateTime.now())
                .build();
    }

    public UserResponse userToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    public boolean validatePassword(String password, String cpassword) {
        return password.equals(cpassword);
    }

    public void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) throws MessagingException {
        // send email to user
        String url = applicationUrl
                + "="
                + verificationToken.getToken();
        resendVerificationToken(user, null);
        log.info("Verification token: {} has been resend", url);
    }

    private void resendVerificationToken(User user, String url) throws MessagingException {
        String subject = "Verify your account";
        String senderName = "App Services";
        String mailContent = "<p> Dear "+ user.getLastName() +", </p>";
        mailContent += "<p> Please click the link to verify your account, </p>";
        mailContent += "<h3><a href=\""+ url + "\"> VERIFY ACCOUNT</a></h3>";
       // sendMail(user, subject, senderName, mailContent, mailSender);
        emailService.sendEmail(user.getFirstName(),user.getEmail(),null);
    }



    public static void sendMail(User user, String subject, String senderName, String mailContent, JavaMailSender mailSender) {
        mailContent += "<p>Best regards <br/> App Service team </p>";
        setMailCredentials(user, subject, senderName, mailContent, mailSender);
    }

    public static void setMailCredentials(User user, String subject, String senderName, String mailContent, JavaMailSender mailSender) {
        try{
             //send message;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom("ccanazodo@gmail.com",senderName);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(mailContent, true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String applicationUrl(HttpServletRequest request) {
        //return TEMP_APP_URL;
        return "http://localhost:3000" + request.getContextPath();


    }


    public static List<String> extractRolesFromJwt(String token){
        // decode jwt
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] chunks = token.split("\\.");
        String payload = new String(decoder.decode(chunks[1]));
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String,Object> map = mapper.readValue(payload, Map.class);

            List<String> roles = (List<String>) map.get("roles");
            return roles;


//            JWTRolesResponse jwtPayload = modelMapper.map(realmAccess, JWTRolesResponse.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


//    private List<String> extractRolesFromJwt(String token){
//        // decode jwt
//        Base64.Decoder decoder = Base64.getUrlDecoder();
//        String[] chunks = token.split("\\.");
//        String payload = new String(decoder.decode(chunks[1]));
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            Map<String,Object> map = mapper.readValue(payload, Map.class);
//            Object realmAccess = map.get("realm_access");
////            JWTRolesResponse jwtPayload = modelMapper.map(realmAccess, JWTRolesResponse.class);
//            return jwtPayload.getRoles();
//
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }

}
