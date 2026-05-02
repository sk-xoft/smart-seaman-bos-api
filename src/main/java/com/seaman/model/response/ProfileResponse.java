package com.seaman.model.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ProfileResponse {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String age;
    private String mobile;
    private String email;
    private String companyCode;
    private String companyDescription;
    private String positionCode;
    private String positionDescription;
    private String smartSeamanId;
    private String shortName;
}