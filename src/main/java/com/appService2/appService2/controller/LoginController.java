package com.appService2.appService2.controller;


import com.appService2.appService2.dto.*;
import com.appService2.appService2.entity.User;
import com.appService2.appService2.entity.VerificationToken;
import com.appService2.appService2.security.JwtAuthResponse;
import com.appService2.appService2.service.LoginService;
import com.appService2.appService2.service.UserService;
import com.appService2.appService2.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final UserService userService;
    private final Util utility;



    // http://localhost:8080/api/v1/user/verifyRegistration


    @GetMapping("/verifyRegistration")
    public BaseResponse<?> validateRegistrationToken(@RequestParam("token") String token){
        boolean isValid = userService.validateRegistrationToken(token);
        return isValid ? new BaseResponse<>(HttpStatus.OK, "User verified successfully", null)
                : new BaseResponse<>(HttpStatus.BAD_REQUEST, "User verification failed", null);
    }

    @GetMapping("/resendVerificationToken")
    public BaseResponse<?> resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request) throws MessagingException {
        VerificationToken verificationToken = userService.generateNewToken(oldToken);
        User user = verificationToken.getUser();
        utility.resendVerificationTokenMail(user, utility.applicationUrl(request), verificationToken);
        return new BaseResponse<>(HttpStatus.OK, "verification link sent", null);
    }

    @PostMapping("/login")
    public BaseResponse<JwtAuthResponse> login(@RequestBody LoginRequest loginRequest) throws Exception {
        log.info("UserRequest ==> {}", loginRequest );
        return loginService.login(loginRequest);
    }

    @PostMapping("/logout")
    public BaseResponse<?> logout() {
        return loginService.logout();
    }


    @PostMapping("user/changePassword")
    public BaseResponse<String> changePassword(@RequestBody PasswordRequest passwordRequest) {
        return loginService.changePassword(passwordRequest);
    }

}
