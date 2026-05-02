package com.seaman.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class FormEntity extends CommonEntity implements Serializable {
    private Integer formId;
    private String formName;
    private String fromFileId;
    private String formFileName;
    private Integer formNum;
    private String fileBase64;
}
