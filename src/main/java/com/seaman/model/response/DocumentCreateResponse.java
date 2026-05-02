package com.seaman.model.response;

import lombok.Data;
import java.util.Date;

@Data
public class DocumentCreateResponse {
    private String documentCode;
    private Date certStartDate;
    private Date certEndDate;
    private String certEndDateType;
    private String fileCertName;
    private String fileBase64;
}
