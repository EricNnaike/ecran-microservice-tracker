package com.appService2.appService2.service.implimentation;


import com.appService2.appService2.dto.BaseResponse;
import com.appService2.appService2.dto.UserRequest;
import com.appService2.appService2.dto.UserResponse;
import com.appService2.appService2.entity.Role;
import com.appService2.appService2.entity.User;
import com.appService2.appService2.entity.VerificationToken;
import com.appService2.appService2.repository.RoleRpository;
import com.appService2.appService2.repository.UserRepository;
import com.appService2.appService2.repository.VerificationTokenRepository;
import com.appService2.appService2.service.EmailService;
import com.appService2.appService2.service.UserService;
import com.appService2.appService2.util.RegistrationCompleteEvent;
import com.appService2.appService2.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final RoleRpository roleRpository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;
    private final Util utility;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;


    //    @Transactional
    @Override
    public BaseResponse<UserResponse> createUserAccount(UserRequest userRequest, HttpServletRequest request) throws MessagingException {
        if(userRepository.existsByEmail(userRequest.getEmail()))
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "User already exist with this email", null);

        if (!utility.validatePassword(userRequest.getPassword(), userRequest.getConfirmPassword()))
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "Password not matched", null);

        Role role1 = roleRpository.findByRole(userRequest.getRoleName()).orElse(null);
        log.info("role1 ----> {}", role1);

        if (role1 == null)
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "invalid role", null);


        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setState("INACTIVE");
        user.setStatus(false);
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.getRoles().add(role1);

        User save = userRepository.save(user);

        // CALL EMAIL SERVICE
//        publisher.publishEvent(new RegistrationCompleteEvent(
//                user,
//                utility.applicationUrl(request)
//        ));
        emailService.sendEmail("test email",user.getEmail(), save);
        UserResponse userResponse = utility.userToResponse(user);
        return new BaseResponse<>(HttpStatus.CREATED, "Registration success", userResponse);
    }

    @Override
    public BaseResponse<UserResponse> getUser() {
        String loggedInEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (loggedInEmail.equals("anonymousUser")) {
            return new BaseResponse<>(HttpStatus.UNAUTHORIZED, "User not logged in", null);
        }
        User user= userRepository.findUserByEmail(loggedInEmail);

        UserResponse userResponse = utility.userToResponse(user);
        return new BaseResponse<>(HttpStatus.OK, "user profile", userResponse);
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return null;
    }

    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public Boolean validateRegistrationToken(String token) {
        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token);
        log.info("verificationToken ==> {}", verificationToken);
        if (verificationToken == null)
            return false;
        User user = verificationToken.getUser();
        log.info("verificationToken user ===> {}", user);
        Calendar cal = Calendar.getInstance();
        if((verificationToken.getExpirationTime().getTime() - cal.getTime().getTime()) <= 0
                && !user.isStatus()){
            verificationTokenRepository.delete(verificationToken);
            return false;
        }
        user.setStatus(true);
        user.setState("ACTIVE");
        userRepository.save(user);
        return true;
    }

    @Override
    public VerificationToken generateNewToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + 900000);
        verificationToken.setExpirationTime(expirationDate);
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public BaseResponse<UserResponse> updateUser(Long userId, UserRequest userRequest) {
        UserResponse userResponse = null;
        User user = userRepository.findById(userId).orElse(null);

        if(user == null) {
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "User not found",userResponse);
        }


        if(!userRequest.getFirstName().isEmpty())
            user.setFirstName(userRequest.getFirstName());

        if(!userRequest.getLastName().isEmpty())
            user.setLastName(userRequest.getLastName());

        if(!userRequest.getEmail().isEmpty())
            user.setEmail(userRequest.getEmail());

        User saved = userRepository.save(user);
        userResponse = UserResponse.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .build();

        return new BaseResponse<>(HttpStatus.OK, "User successfully updated",userResponse);
    }

    @Override
    public BaseResponse<UserResponse> deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user == null){
            return new BaseResponse<>(HttpStatus.BAD_REQUEST,"User not found", null);
        }

        if(user.getState().equals("DISABLE")){
            return new BaseResponse<>(HttpStatus.BAD_REQUEST,"User not found", null);
        }
        user.setState("DISABLE");
        userRepository.save(user);
        return new BaseResponse<>(HttpStatus.OK, "User successfully deleted",null);
    }

    @Override
    public BaseResponse<List<UserResponse>> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = new ArrayList<>();

        if(users.size() <= 0) {
            return  new BaseResponse<>(HttpStatus.OK, "Users  not found",null);
        }
        users.forEach(user -> {
            UserResponse userResponse = new UserResponse();
            userResponse.setEmail(user.getEmail());
            userResponse.setFirstName(user.getFirstName());
            userResponse.setLastName(user.getLastName());
            userResponse.setStatus(user.isStatus());
            userResponse.setId(user.getId());



            userResponses.add(userResponse);

        });
        return  new BaseResponse<>(HttpStatus.OK, "List of users",userResponses);
    }


}