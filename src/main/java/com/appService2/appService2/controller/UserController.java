package com.appService2.appService2.controller;

import com.appService2.appService2.dto.BaseResponse;
import com.appService2.appService2.dto.PasswordRequest;
import com.appService2.appService2.dto.UserRequest;
import com.appService2.appService2.dto.UserResponse;
import com.appService2.appService2.entity.User;
import com.appService2.appService2.entity.VerificationToken;
import com.appService2.appService2.service.LinusPojoService;
import com.appService2.appService2.service.LoginService;
import com.appService2.appService2.service.UserService;
import com.appService2.appService2.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final LinusPojoService service;
    private final LoginService loginService;

    @GetMapping
    public BaseResponse<UserResponse> getUserProfile(){
        return userService.getUser();
    }

    @RolesAllowed("SUPER_ADMIN")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN')") // ROLE_SUPER_ADMIN
    @PostMapping("/register")
    public BaseResponse<UserResponse> createUserAccount(@RequestBody UserRequest userRequest, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<String> roles = Util.extractRolesFromJwt(token);

        if(!roles.contains("SUPER_ADMIN"))
            return new BaseResponse<>(HttpStatus.UNAUTHORIZED,"You are not authorized to perform this action",null);

        try {
            return userService.createUserAccount(userRequest, request);
        } catch (Exception e) {
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    @RolesAllowed("SUPER_ADMIN")
    @PutMapping("/{userId}")
    public BaseResponse<UserResponse> updateUser(@PathVariable("userId") Long userId, @RequestBody UserRequest userRequest, HttpServletRequest request){

        String token = request.getHeader("Authorization");
        List<String> roles = Util.extractRolesFromJwt(token);
        if(!roles.contains("SUPER_ADMIN"))
            return new BaseResponse<>(HttpStatus.UNAUTHORIZED,"You are not authorized to perform this action",null);
        return userService.updateUser(userId, userRequest);

    }

    @RolesAllowed("SUPER_ADMIN")
    @DeleteMapping(value = "/{userId}")
    public BaseResponse<UserResponse> deleteUser(@PathVariable("userId") Long userId, HttpServletRequest request){

        String token = request.getHeader("Authorization");
        List<String> roles = Util.extractRolesFromJwt(token);
        if(!roles.contains("SUPER_ADMIN"))
            return new BaseResponse<>(HttpStatus.UNAUTHORIZED,"You are not authorized to perform this action",null);

        log.info("delete user {}", userId);
        return userService.deleteUser(userId);
    }

    @RolesAllowed({"SUPER_ADMIN", "ADMIN"})
    @GetMapping ("/getAllUsers")
    public BaseResponse<List<UserResponse>> getUsers(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        List<String> roles = Util.extractRolesFromJwt(token);
        if(!roles.contains("SUPER_ADMIN") || !roles.contains("ADMIN"))
            return new BaseResponse<>(HttpStatus.UNAUTHORIZED,"You are not authorized to perform this action",null);

        return  userService.getUsers();
    }

    @PostMapping("/forgot-password")
    public BaseResponse<String> forgotPassword(@RequestBody PasswordRequest passwordRequest) throws MessagingException {
        return loginService.generateResetToken(passwordRequest);
    }

    @PostMapping("/resetPassword")
    public BaseResponse<String> resetPassword(@RequestBody PasswordRequest passwordRequest, @RequestParam("token") String token) {
        return loginService.resetPassword(passwordRequest, token);
    }


}
