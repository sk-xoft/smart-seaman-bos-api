package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    private String resubmittedAt;
    private BigDecimal amount;
    private Boolean isResubmit;
    private ProfileResponse profile;
    private Map<String, Object> deliverAddress;
    private List<DocumentRenewalDetailItemResponse> items;
    private DocumentRenewalDeptSubmissionResponse deptSubmission;
    private Map<String, Object> deptResult;
    private DocumentRenewalDeliveryResponse delivery;
}
