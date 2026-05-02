package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CompanyEntity {
    private Integer companyId;
    private String companyCode;
    private String companyNameEn;
    private String companyNameTh;
    private String companyFullNameEn;
    private String companyFullNameTh;
    private String companyDescription;
    private String companyType;
    private String companyMobileFlag;
    private String companyLogo;
    private String companySeq;
    private String companyStatus;
}
