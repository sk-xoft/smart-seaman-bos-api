package com.seaman.model.request;

import lombok.Data;

@Data
public class DocumentInspectionItemRequest {
    private Integer sortOrder;
    private String checkResult;
    private String checkNote;
}
