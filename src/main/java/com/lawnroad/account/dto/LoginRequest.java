package com.lawnroad.account.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    private String clientId;
    private String password;
    private String type;

}
