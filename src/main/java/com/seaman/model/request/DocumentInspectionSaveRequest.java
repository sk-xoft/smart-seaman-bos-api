package com.seaman.model.request;

import lombok.Data;

import java.util.List;

@Data
public class DocumentInspectionSaveRequest {
    private String requestNo;
    private List<DocumentInspectionItemRequest> inspections;
}
