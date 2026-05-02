package com.seaman.model.response;

import lombok.Data;

@Data
public class UserAdminProfileResponse {
    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private String companyCode;
    private String companyDescription;
    private String positionCode;
    private String positionDescription;
    private String shortName;
    private String roleName;
    private Integer groupId;
}
