package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalDetailFileResponse {
    private String fileId;
    private String documentType;
    private String slotCode;
    private String originalFileName;
    private String mimeType;
    private Long fileSize;
    private String fileUploadedAt;
    private String fileUrl;
    private Boolean isUpdated;
}
