package com.seaman.model.response;

import lombok.Data;

@Data
public class DocumentUpdateResponse {
    private String documentCode;
    private String certStartDate;
    private String certEndDateType;
    private String certEndDate;
    private String fileCertName;
}
