package com.appService2.appService2.service.implimentation;

import com.appService2.appService2.dto.BaseResponse;
import com.appService2.appService2.dto.LoginRequest;
import com.appService2.appService2.dto.PasswordRequest;
import com.appService2.appService2.entity.User;
import com.appService2.appService2.entity.VerificationToken;
import com.appService2.appService2.repository.UserRepository;
import com.appService2.appService2.repository.VerificationTokenRepository;
import com.appService2.appService2.security.JwtAuthResponse;
import com.appService2.appService2.security.JwtTokenProvider;
import com.appService2.appService2.service.EmailService;
import com.appService2.appService2.service.LoginService;
import com.appService2.appService2.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import static com.appService2.appService2.constants.EmailConstant.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final AuthenticationManager authenticationManager;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;


    private final JwtTokenProvider jwtTokenProvider;


    private final HttpServletResponse httpServletResponse;

    private final HttpServletRequest httpServletRequest;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    @Autowired
    private EmailService emailService;




    @Override
    public BaseResponse<JwtAuthResponse> login(LoginRequest loginRequest) throws Exception {
        Authentication authentication;
        String token;
        String message = "Login Successful";

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

        if(!user.isStatus()){
            return new BaseResponse<>(HttpStatus.OK,"User profile not activated",null);
        }

        if(!user.getState().equals("ACTIVE")){
            return new BaseResponse<>(HttpStatus.OK,"cannot login as user profile is inactive",null);
        }

        try{
            Authentication auth =  new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),loginRequest.getPassword());

            authentication = authenticationManager.authenticate(auth);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenProvider.generateToken(authentication);
            httpServletResponse.setHeader("Authorization", token);
        }
        catch (BadCredentialsException ex){
            throw new Exception("incorrect user credentials", ex);
        }
        return new BaseResponse<>(HttpStatus.OK, message, new JwtAuthResponse(token));
    }

    @Override
    public BaseResponse<?> logout() {

        httpServletRequest.setAttribute("Authorization", "");

        return new BaseResponse<>(HttpStatus.OK, "Logout Successful", null);

    }

    @Override
    public BaseResponse<String> changePassword(PasswordRequest passwordRequest) {

        if(!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())){
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "new password must be the same with confirm password", null);
        }

        String loggedInUsername =  SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findUserByEmail(loggedInUsername);

        if (user == null) {
            return new BaseResponse<>(HttpStatus.UNAUTHORIZED, "User not logged In", null);
        }

        boolean matchPasswordWithOldPassword = passwordEncoder.matches(passwordRequest.getOldPassword(), user.getPassword());

        if(!matchPasswordWithOldPassword){
           return new BaseResponse<>(HttpStatus.BAD_REQUEST, "old password is not correct", null);
        }

        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));

        userRepository.save(user);
        return new BaseResponse<>(HttpStatus.OK, "password changed successfully", null);
    }



    @Override
    public BaseResponse<String> generateResetToken(PasswordRequest passwordRequest) throws MessagingException {
        String email = passwordRequest.getEmail();
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            return new BaseResponse<>(HttpStatus.NOT_FOUND, "User with email not found", null);
        }

        String token = jwtTokenProvider.generatePasswordResetToken(email);

        emailService.sendResetPasswordEmail(user.getFirstName(), user.getEmail(), user);
        return new BaseResponse<>(HttpStatus.OK,"Check Your Email to Reset Your Password",null);
    }

    @Override
    public BaseResponse<String> resetPassword(PasswordRequest passwordRequest, String token) {
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "Passwords don't match.", null);
        }
        String email = jwtTokenProvider.getUsernameFromJwt(token);

        User user = userRepository.findUserByEmail(email);

        if (user == null) {
            return new BaseResponse<>(HttpStatus.NOT_FOUND, "User with email " + email + " not found", null);
        }

        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);
        return new BaseResponse<>(HttpStatus.OK,"Password Reset Successfully",null);
    }







}
