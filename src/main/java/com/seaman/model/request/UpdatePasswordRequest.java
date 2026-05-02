package com.seaman.model.request;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String username;
    private String adminId;
    private String newPassword;
    private String confirmPassword;
    private String oldPassword;
}
