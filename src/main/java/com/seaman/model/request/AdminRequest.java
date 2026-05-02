package com.seaman.model.request;

import lombok.Data;

@Data
public class AdminRequest {

    private Integer adminUserId;
    private Integer size;
    private Integer lastNum;
    private String adminUUID;
    private String groupId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String companyCode;
    private String positions;
    private String email;
    private String mobileNumber;
    private String displayType;
    private String displayName;
    private String profilePicture;
    private String userStatus;
    private String lastLogin;
    private String pictureFromFile;

}
