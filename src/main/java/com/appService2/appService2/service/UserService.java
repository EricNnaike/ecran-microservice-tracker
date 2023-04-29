package com.appService2.appService2.service;


import com.appService2.appService2.dto.BaseResponse;
import com.appService2.appService2.dto.UserRequest;
import com.appService2.appService2.dto.UserResponse;
import com.appService2.appService2.entity.User;
import com.appService2.appService2.entity.VerificationToken;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService {

    BaseResponse<UserResponse> createUserAccount(UserRequest userRequest, HttpServletRequest request) throws MessagingException;
    BaseResponse<UserResponse> getUser();
    void saveVerificationTokenForUser(String token, User user);
    Boolean validateRegistrationToken(String token);
    VerificationToken generateNewToken(String oldToken);


    BaseResponse<UserResponse> updateUser(Long userId, UserRequest userRequest);
    BaseResponse<UserResponse> deleteUser(Long userId);

    BaseResponse<List<UserResponse>> getUsers();

}
