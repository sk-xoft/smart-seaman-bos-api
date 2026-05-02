package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponse {
    private String token;
    private String refToken;
    private String username;
    private String lastLoginDateTime;
}
