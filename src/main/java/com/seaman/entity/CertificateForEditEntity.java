package com.seaman.entity;

import lombok.Data;

import java.util.Date;

@Data
public class CertificateForEditEntity {
    private Date createDate;
    private String createBy;
    private Date updateDate;
    private String updateBy;
    private String certFile;
    private String originalFileName;
    private String certStatus;
    private Date certStartDate;
    private Date certEndDate;
    private String certId;
    private String certMobileUuid;
    private String certDocumentCode;
}
