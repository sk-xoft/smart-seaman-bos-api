package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentAttachment {
    private String id;
    private String mobileUserUuid;
    private String documentName;
    private Integer sortOrder;
    private Boolean fileUploaded;
    private String filePath;
    private Date fileUploadedAt;
    private String checkResult;
    private String checkNote;
    private Boolean isUpdated;
    private Date checkedAt;
    private String checkedBy;
    private Date createdAt;
    private Date updatedAt;
}