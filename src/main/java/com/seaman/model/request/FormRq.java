package com.seaman.model.request;

import lombok.Data;

@Data
public class FormRq {
    private Integer formId;
    private String formName;
    private String fromFileId;
    private String formFileName;
    private String formFile;
    private Integer size;
    private Integer lastNum;
}
