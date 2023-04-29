package com.appService2.appService2.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String roleName;
    private String password;
    private String confirmPassword;

}
