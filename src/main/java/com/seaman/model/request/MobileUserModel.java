package com.seaman.model.request;

import lombok.Data;

@Data
public class MobileUserModel {

    private Integer count;
    private Integer certId;
    private String certMobileUuid;
    private String smartSeamanId;
    private String firstName;
    private String lastName;
    private String companyNameEn;
    private String documentNameTh;
    private String certEndDate;
    private String certStartDate;
    private Integer days;
    private Integer num;
    private String positionNameEn;
    private String mobileNumber;
    private String mobileUuid;
    private String email;

}
