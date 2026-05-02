package com.seaman.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class AdminUserEntity extends CommonEntity implements Serializable {
    private Integer adminNum;
    private String adminUserId;
    private String adminUuid;
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
    private String pictureFromFile;
    private String userStatus;
    private Date lastLogon;
    private String adminRoleName;
    private String adminCompany;
    private String adminStatus;
    private String groupName;
}
