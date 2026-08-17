package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DocumentRenewalDetailResponse {
    private String requestId;
    private String requestNo;
    private String mobileUserUuid;
    private String documentCode;
    private String documentName;
    private DocumentRenewalSummaryStatusResponse status;
    private String submittedAt;
    private BigDecimal amount;
    private Boolean isResubmit;
    private List<DocumentRenewalDetailItemResponse> items;
    private DocumentRenewalDeptSubmissionResponse deptSubmission;
    private DocumentRenewalDeliveryResponse delivery;
}
