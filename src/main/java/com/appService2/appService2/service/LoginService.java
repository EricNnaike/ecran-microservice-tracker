package com.appService2.appService2.service;



//import javax.mail.MessagingException;

import com.appService2.appService2.dto.BaseResponse;
import com.appService2.appService2.dto.LoginRequest;
import com.appService2.appService2.dto.PasswordRequest;
import com.appService2.appService2.security.JwtAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.mail.MessagingException;

public interface LoginService {
    BaseResponse<JwtAuthResponse> login(LoginRequest loginRequest) throws Exception;
//    BaseResponse<?> logout();

    BaseResponse<String> changePassword(PasswordRequest passwordRequest);

    BaseResponse<String> generateResetToken(PasswordRequest passwordRequest) throws MessagingException;
    BaseResponse<String> resetPassword(PasswordRequest passwordRequest, String token);

    BaseResponse<?> logout();
}
