package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalDeptSubmissionResponse {
    private String submittedToDeptDate;
    private String availableFromDate;
    private String receivedFromDeptDate;
    private String recordedAt;
    private String action;
    private String actionedAt;
    private String actionedBy;
    private String actionedByUsername;
    private String actionedByFirstName;
    private String actionedByLastName;
    private String actionedByMobileNumber;
}
