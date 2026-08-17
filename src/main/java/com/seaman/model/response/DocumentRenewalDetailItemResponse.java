package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentRenewalDetailItemResponse {
    private String itemId;
    private String documentRequestItemCode;
    private String storageScope;
    private String documentName;
    private Integer sortOrder;
    private Boolean fileUploaded;
    private String checkResult;
    private String checkNote;
    private Boolean isUpdated;
    private List<DocumentRenewalDetailFileResponse> files;
}
