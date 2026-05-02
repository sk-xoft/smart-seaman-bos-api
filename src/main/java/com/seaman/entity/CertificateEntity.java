package com.seaman.entity;

import lombok.Data;
import java.util.Date;

@Data
public class CertificateEntity {
    private Date createDate;
    private String createBy;
    private Date updateDate;
    private String updateBy;
    private String certFile;
    private String originalFileName;
    private String certStatus;
    private String certStartDate;
    private String certEndDate;
    private String certId;
    private String certMobileUuid;
    private String certDocumentCode;
}
