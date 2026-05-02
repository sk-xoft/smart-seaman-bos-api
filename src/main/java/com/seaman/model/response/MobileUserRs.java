package com.seaman.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class MobileUserRs {
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
