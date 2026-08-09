package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentRequestDetailRs {
    private String requestNo;
    private Date dateOfSubmission;
    private String documentName;
    private Map<String, Object> documentStatus;
    private DocumentRequestStepperRs stepper;
    private List<DocumentAttachment> documentAttachments;
    private Map<String, Object> profile;
    private Map<String, Object> deptSubmission;
    private Map<String, Object> deptResult;
    private Map<String, Object> deliveryInfo;
    private Map<String, Object> deliverAddress;
}